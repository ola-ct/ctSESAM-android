package de.pinyto.ctSESAM;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public abstract class SyncServiceEnabledActivity extends AppCompatActivity
        implements SyncResponseHandler.OnSyncFinishedListener {
    protected Messenger syncServiceMessenger = null;
    protected boolean syncServiceBound;
    protected KgkManager kgkManager;
    protected PasswordSettingsManager settingsManager;
    protected PasswordGenerator passwordGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(UnlockActivity.KEYIVKEY)) {
            kgkManager = new KgkManager(this,
                    savedInstanceState.getByteArray(UnlockActivity.KEYIVKEY));
        }
        Intent intent = getIntent();
        if (intent.hasExtra(UnlockActivity.KEYIVKEY)) {
            kgkManager = new KgkManager(this,
                    intent.getByteArrayExtra(UnlockActivity.KEYIVKEY));
        }
        settingsManager = new PasswordSettingsManager(getBaseContext());
        try {
            settingsManager.loadLocalSettings(kgkManager);
        } catch (WrongPasswordException e) {
            Log.e("Wrong password?", "This should not happen at this point!");
            Log.e("WrongPasswordException", e.toString());
            Intent newIntent = new Intent(this, UnlockActivity.class);
            startActivity(newIntent);
        }
        setToNotGenerated();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kgkManager == null || !kgkManager.hasKgk() || settingsManager == null) {
            Intent intent = getIntent();
            if (intent.hasExtra(UnlockActivity.KEYIVKEY)) {
                kgkManager = new KgkManager(this,
                        intent.getByteArrayExtra(UnlockActivity.KEYIVKEY));
                settingsManager = new PasswordSettingsManager(getBaseContext());
                try {
                    settingsManager.loadLocalSettings(kgkManager);
                } catch (WrongPasswordException e) {
                    Log.e("Wrong password?", "This should not happen at this point!");
                    Log.e("WrongPasswordException", e.toString());
                    Intent newIntent = new Intent(this, UnlockActivity.class);
                    startActivity(newIntent);
                }
                setToNotGenerated();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isAppInstalled(SyncResponseHandler.syncAppName)) {
            // Bind to the service
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    SyncResponseHandler.syncAppName,
                    SyncResponseHandler.syncAppName + "." +
                            SyncResponseHandler.syncServiceName));
            bindService(intent, syncServiceConnection, Context.BIND_AUTO_CREATE);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putByteArray(UnlockActivity.KEYIVKEY, kgkManager.exportKeyIv());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        unbindService(syncServiceConnection);
        syncServiceBound = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        kgkManager.reset();
        super.onDestroy();
    }

    private ServiceConnection syncServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            syncServiceMessenger = new Messenger(service);
            syncServiceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            syncServiceMessenger = null;
            syncServiceBound = false;
        }
    };

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(isAppInstalled(SyncResponseHandler.syncAppName));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            if (!syncServiceBound) {
                Log.d("Sync error", "Sync service is not bound. This button should not be visible.");
                return true;
            }
            Message msg = Message.obtain(null, SyncResponseHandler.REQUEST_SYNC, 0, 0);
            msg.replyTo = new Messenger(new SyncResponseHandler(
                    this,
                    kgkManager,
                    settingsManager,
                    syncServiceMessenger,
                    syncServiceBound));
            try {
                syncServiceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.d("Sync error", "Could not send message to sync service.");
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSyncFinished(boolean success) {
        if (success) {
            Toast.makeText(getBaseContext(), R.string.sync_successful, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), R.string.sync_error, Toast.LENGTH_SHORT).show();
        }
    }

    protected void setToNotGenerated() {
        this.passwordGenerator = null;
        invalidateOptionsMenu();
    }
}
