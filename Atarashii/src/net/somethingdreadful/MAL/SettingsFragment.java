package net.somethingdreadful.MAL;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
        switch (preference.getKey()) {
            case "IGFcolumnsportrait":
                makeNumberpicker(R.string.preference_list_columns_portrait, R.string.preference_list_columns_portrait,  PrefManager.getIGFColumns(true), IGF.getMaxColumns(true), 2);
                break;
            case "IGFcolumnslandscape":
                makeNumberpicker(R.string.preference_list_columns_landscape, R.string.preference_list_columns_landscape, PrefManager.getIGFColumns(false), IGF.getMaxColumns(false), 2);
                break;
            case "backup_length":
                makeNumberpicker(R.string.preference_backuplength, R.string.preference_backuplength, PrefManager.getBackupLength(), 50, 1);
                break;
            case "backup":
                requestStoragePermission();
                break;
            case "reset":
                PrefManager.clear();
                startActivity(new Intent(context, Home.class));
                System.exit(0);
                break;
        }
        return false;
    }

    private void requestStoragePermission() {
        // Check for staorage permission to store the account.
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Check if user marked to show never the permission dialog
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.dialog_title_permission)
                        .setMessage(R.string.dialog_message_permission)
                        .setPositiveButton(android.R.string.ok, null);
                alertDialog.create().show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            }
        } else {
            Intent firstRunInit = new Intent(context, BackupActivity.class);
            startActivity(firstRunInit);
        }
    }

    private void makeNumberpicker(int id, int title, int current, int max, int min) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("title", getString(title));
        bundle.putInt("current", current);
        bundle.putInt("max", max);
        bundle.putInt("min", min);
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
