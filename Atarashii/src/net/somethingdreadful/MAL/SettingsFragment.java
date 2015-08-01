package net.somethingdreadful.MAL;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, NumberPickerDialogFragment.onUpdateClickListener {
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        findPreference("backup").setOnPreferenceClickListener(this);
        findPreference("IGFcolumns").setOnPreferenceClickListener(this);
        findPreference("reset").setOnPreferenceClickListener(this);

        context = getActivity().getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            String Auth = AccountService.getAuth();
            Bundle bundle = new Bundle();
            int interval = PrefManager.getSyncTime() * 60;
            switch (key) {
                case "synchronisation_time":
                    ContentResolver.removePeriodicSync(AccountService.getAccount(), Auth, bundle);
                    ContentResolver.addPeriodicSync(AccountService.getAccount(), Auth, bundle, interval);
                    break;
                case "synchronisation":
                    if (PrefManager.getSyncEnabled()) {
                        ContentResolver.setSyncAutomatically(AccountService.getAccount(), Auth, true);
                        ContentResolver.addPeriodicSync(AccountService.getAccount(), Auth, bundle, interval);
                    } else {
                        ContentResolver.removePeriodicSync(AccountService.getAccount(), Auth, bundle);
                        ContentResolver.setSyncAutomatically(AccountService.getAccount(), Auth, false);
                    }
                    break;
                case "locale":
                    sharedPreferences.edit().commit();
                    startActivity(new Intent(context, Home.class));
                    System.exit(0);
                    break;
                case "darkTheme":
                    PrefManager.setTextColor(true);
                    PrefManager.commitChanges();
                    sharedPreferences.edit().commit();
                    startActivity(new Intent(context, Home.class));
                    System.exit(0);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "SettingsFragment.onSharedPreferenceChanged(): " + e.getMessage());
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "IGFcolumns":
                Bundle bundle = new Bundle();
                bundle.putInt("id", R.string.preference_list_columns);
                bundle.putString("title", getString(R.string.preference_list_columns));
                bundle.putInt("current", PrefManager.getIGFColumns());
                bundle.putInt("max", IGF.getMaxColumns());
                bundle.putInt("min", 2);
                NumberPickerDialogFragment numberPickerDialogFragment = new NumberPickerDialogFragment().setOnSendClickListener(this);
                numberPickerDialogFragment.setArguments(bundle);
                numberPickerDialogFragment.show(getActivity().getFragmentManager(), "numberPickerDialogFragment");
                break;
            case "backup":
                Intent firstRunInit = new Intent(context, BackupActivity.class);
                startActivity(firstRunInit);
                break;
            case "reset":
                PrefManager.clear();
                startActivity(new Intent(context, Home.class));
                System.exit(0);
                break;
        }
        return false;
    }

    @Override
    public void onUpdated(int number, int id) {
        PrefManager.setIGFColumns(number);
        PrefManager.commitChanges();
        startActivity(new Intent(context, Home.class));
        System.exit(0);
    }
}
