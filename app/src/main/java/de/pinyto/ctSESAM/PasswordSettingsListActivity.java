package de.pinyto.ctSESAM;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;

public class PasswordSettingsListActivity extends SyncServiceEnabledFragmentActivity
        implements PasswordSettingsListFragment.OnSettingSelected,
        PasswordSettingsListFragment.OnNewSetting {
    public static final String DOMAIN = "de.pinyto.ctsesam.DOMAIN";
    public static final String ISNEWSETTING = "de.pinyto.ctsesam.ISNEWSETTING";
    private PasswordSettingsListFragment listScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_settings_list);
        listScreen = (PasswordSettingsListFragment) getFragmentManager().findFragmentById(
                R.id.passwordSettingsListFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setDomainFieldFromClipboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listScreen.setKgkAndSettingsManager(kgkManager, settingsManager);
    }

    @Override
    public void onSettingSelected(PasswordSetting setting) {
        Intent intent = new Intent(this, DomainDetailsActivity.class);
        intent.putExtra(UnlockActivity.KEYIVKEY, kgkManager.exportKeyIvAndReset());
        intent.putExtra(DOMAIN, setting.getDomain());
        startActivity(intent);
    }

    @Override
    public void onNewSetting(PasswordSetting setting) {
        Intent intent = new Intent(this, DomainDetailsActivity.class);
        intent.putExtra(UnlockActivity.KEYIVKEY, kgkManager.exportKeyIvAndReset());
        intent.putExtra(DOMAIN, setting.getDomain());
        intent.putExtra(ISNEWSETTING, true);
        startActivity(intent);
    }

    private void setDomainFieldFromClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clipDataCurrent = clipboard.getPrimaryClip();
            CharSequence pasteData = clipDataCurrent.getItemAt(0).getText();
            if (pasteData != null) {
                listScreen.setDomainFilter(pasteData);
            }
        }
    }
}