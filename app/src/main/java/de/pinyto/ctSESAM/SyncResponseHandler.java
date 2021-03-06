package de.pinyto.ctSESAM;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Handles the responses from the sync service if the sync app is installed.
 */
class SyncResponseHandler extends Handler {
    public static final String syncAppName = "de.pinyto.ctSESAMsync";
    public static final String syncServiceName = "SyncService";
    static final int REQUEST_SYNC = 1;
    static final int SEND_UPDATE = 2;
    static final int SYNC_RESPONSE = 1;
    static final int SEND_UPDATE_RESPONSE = 2;
    private WeakReference<OnSyncFinishedListener> syncFinishedListenerWeakRef;
    private KgkManager kgkManager;
    private PasswordSettingsManager settingsManager;
    private Messenger mService;
    private boolean mBound;

    SyncResponseHandler(OnSyncFinishedListener listener,
                        KgkManager kgkManager,
                        PasswordSettingsManager settingsManager,
                        Messenger mService,
                        boolean mBound) {
        super();
        this.syncFinishedListenerWeakRef = new WeakReference<>(listener);
        this.kgkManager = kgkManager;
        this.settingsManager = settingsManager;
        this.mService = mService;
        this.mBound = mBound;
    }

    @Override
    public void handleMessage(Message msg) {
        OnSyncFinishedListener syncFinishedListener = syncFinishedListenerWeakRef.get();
        if (syncFinishedListener != null) {
            int respCode = msg.what;

            switch (respCode) {
                case SYNC_RESPONSE: {
                    String syncData = msg.getData().getString("respData");
                    try {
                        JSONObject syncDataObject = new JSONObject(syncData);
                        if (!syncDataObject.getString("status").equals("ok")) break;
                        boolean updateRemote = true;
                        if (syncDataObject.has("result")) {
                            //byte[] password = UTF8.encode(editTextMasterPassword.getText());
                            byte[] blob = Base64.decode(syncDataObject.getString("result"),
                                    Base64.DEFAULT);
                            //kgkManager.updateFromBlob(password, blob);
                            updateRemote = settingsManager.updateFromExportData(
                                    kgkManager, blob);
                            //Clearer.zero(password);
                        }
                        if (updateRemote) {
                            byte[] encryptedBlob = settingsManager.getExportData(kgkManager);
                            if (mService != null && mBound) {
                                Message updateMsg = Message.obtain(null, SEND_UPDATE, 0, 0);
                                updateMsg.replyTo = new Messenger(new SyncResponseHandler(
                                        syncFinishedListener,
                                        kgkManager,
                                        settingsManager,
                                        mService,
                                        mBound));
                                Bundle bUpdateMsg = new Bundle();
                                bUpdateMsg.putString("updatedData",
                                        Base64.encodeToString(encryptedBlob, Base64.DEFAULT));
                                updateMsg.setData(bUpdateMsg);
                                try {
                                    mService.send(updateMsg);
                                } catch (RemoteException e) {
                                    Log.e("Sync error",
                                            "Could not send update message to sync service.");
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("Sync error", "The response is not valid JSON.");
                        e.printStackTrace();
                    }
                    break;
                }
                case SEND_UPDATE_RESPONSE: {
                    String updateRequestAnswer = msg.getData().getString("respData");
                    try {
                        JSONObject syncDataObject = new JSONObject(updateRequestAnswer);
                        if (syncDataObject.getString("status").equals("ok")) {
                            settingsManager.setAllSettingsToSynced();
                            syncFinishedListener.onSyncFinished(true);
                        } else {
                            syncFinishedListener.onSyncFinished(false);
                        }
                    } catch (JSONException e) {
                        Log.e("update Settings error", "Server response is not JSON.");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } else {
            Log.e("Sync notification error", "No success listener.");
        }
    }

    public interface OnSyncFinishedListener {
        void onSyncFinished(boolean success);
    }
}
