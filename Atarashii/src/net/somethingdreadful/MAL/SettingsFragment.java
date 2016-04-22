package net.somethingdreadful.MAL;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.broadcasts.AutoSync;
import net.somethingdreadful.MAL.broadcasts.BackupSync;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, NumberPickerDialogFragment.onUpdateClickListener {
    private Context context;
    private AlarmManager alarmMgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        findPreference("backup").setOnPreferenceClickListener(this);
        findPreference("reset").setOnPreferenceClickListener(this);
        findPreference("IGFcolumnsportrait").setOnPreferenceClickListener(this);
        findPreference("IGFcolumnslandscape").setOnPreferenceClickListener(this);
        findPreference("backup_length").setOnPreferenceClickListener(this);

        context = getActivity().getApplicationContext();
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            // autosync
            Intent autosyncIntent = new Intent(context, AutoSync.class);
            PendingIntent autosyncalarmIntent = PendingIntent.getBroadcast(context, 0, autosyncIntent, 0);
            int intervalAutosync = PrefManager.getSyncTime() * 60 * 1000;

            // autobackup
            Intent autoBackupIntent = new Intent(context, BackupSync.class);
            PendingIntent autoBackupalarmIntent = PendingIntent.getBroadcast(context, 0, autoBackupIntent, 0);
            int intervalbackup = PrefManager.getBackupInterval() * 60 * 1000;

            switch (key) {
                case "synchronisation_time":
                    alarmMgr.cancel(autosyncalarmIntent);
                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalAutosync, intervalAutosync, autosyncalarmIntent);
                    break;
                case "synchronisation":
                    if (PrefManager.getSyncEnabled())
                        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalAutosync, intervalAutosync, autosyncalarmIntent);
                    else
                        alarmMgr.cancel(autosyncalarmIntent);
                    break;
                case "locale":
                    sharedPreferences.edit().commit();
                    startActivity(new Intent(context, Home.class));
                    System.exit(0);
                    break;
                case "autobackup":
                    if (PrefManager.getAutoBackup())
                        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalbackup, intervalbackup, autoBackupalarmIntent);
                    else
                        alarmMgr.cancel(autoBackupalarmIntent);
                    break;
                case "backup_time":
                    alarmMgr.cancel(autoBackupalarmIntent);
                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalbackup, intervalbackup, autoBackupalarmIntent);
                    break;
                case "darkTheme":
                    PrefManager.setTextColor(true);
                    PrefManager.commitChanges();
                    sharedPreferences.edit().commit();
                    startActivity(new Intent(context, Home.class));
                    System.exit(0);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "SettingsFragment.onSharedPreferenceChanged(): " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Bundle bundle = new Bundle();
        switch (preference.getKey()) {
            case "IGFcolumnsportrait":
                bundle.putInt("id", R.string.preference_list_columns_portrait);
                bundle.putString("title", getString(R.string.preference_list_columns_portrait));
                bundle.putInt("current", PrefManager.getIGFColumns(true));
                bundle.putInt("max", IGF.getMaxColumns(true));
                bundle.putInt("min", 2);
                makeNumberpicker(bundle);
                break;
            case "IGFcolumnslandscape":
                bundle.putInt("id", R.string.preference_list_columns_landscape);
                bundle.putString("title", getString(R.string.preference_list_columns_landscape));
                bundle.putInt("current", PrefManager.getIGFColumns(false));
                bundle.putInt("max", IGF.getMaxColumns(false));
                bundle.putInt("min", 2);
                makeNumberpicker(bundle);
                break;
            case "backup_length":
                bundle.putInt("id", R.string.preference_backuplength);
                bundle.putString("title", getString(R.string.preference_backuplength));
                bundle.putInt("current", PrefManager.getBackupLength());
                bundle.putInt("max", 50);
                bundle.putInt("min", 1);
                makeNumberpicker(bundle);
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

    private void makeNumberpicker(Bundle bundle) {
        NumberPickerDialogFragment numberPickerDialogFragment = new NumberPickerDialogFragment().setOnSendClickListener(this);
        numberPickerDialogFragment.setArguments(bundle);
        numberPickerDialogFragment.show(getActivity().getFragmentManager(), "numberPickerDialogFragment");
    }

    @Override
    public void onUpdated(int number, int id) {
        switch (id) {
            case R.string.preference_list_columns_portrait:
                PrefManager.setIGFColumns(number, true);
                PrefManager.commitChanges();
                startActivity(new Intent(context, Home.class));
                System.exit(0);
                break;
            case R.string.preference_list_columns_landscape:
                PrefManager.setIGFColumns(number, false);
                PrefManager.commitChanges();
                startActivity(new Intent(context, Home.class));
                System.exit(0);
                break;
            case R.string.preference_backuplength:
                PrefManager.setBackupLength(number);
                PrefManager.commitChanges();
                break;
        }
    }
}
