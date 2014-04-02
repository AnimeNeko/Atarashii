package net.somethingdreadful.MAL;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.dialog.SyncDialogFragment;

import org.holoeverywhere.preference.PreferenceActivity;
import org.holoeverywhere.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    Context context;
    PrefManager Prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = getApplicationContext();
        Prefs = new PrefManager(context);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.settings);

        NfcHelper.disableBeam(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String Auth = AccountService.getauth(context);
        Bundle bundle = new Bundle();
        int interval = Prefs.getsync_time() * 60;
        if (key.equals("synchronisation_time")) {
            ContentResolver.removePeriodicSync(AccountService.getAccount(context), Auth, bundle);
            ContentResolver.addPeriodicSync(AccountService.getAccount(context), Auth, bundle, interval);
        } else if (key.equals("synchronisation")) {
            if (Prefs.getsynchronisationEnabled()) {
                ContentResolver.addPeriodicSync(AccountService.getAccount(context), Auth, bundle, interval);
                FragmentManager fm = getSupportFragmentManager();
                SyncDialogFragment sdf = new SyncDialogFragment();
                sdf.show(fm, "fragment_Sync");
            } else {
                ContentResolver.removePeriodicSync(AccountService.getAccount(context), Auth, bundle);
            }
        }
    }
}
