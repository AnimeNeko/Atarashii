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
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, NumberPickerDialogFragment.onUpdateClickListener {
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        findPreference("backup").setOnPreferenceClickListener(this);
        findPreference("reset").setOnPreferenceClickListener(this);
        findPreference("IGFcolumnsportrait").setOnPreferenceClickListener(this);
        findPreference("IGFcolumnslandscape").setOnPreferenceClickListener(this);

        context = getActivity().getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AutoSync.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            int interval = PrefManager.getSyncTime() * 60 * 1000;
            switch (key) {
                case "synchronisation_time":
                    alarmMgr.cancel(alarmIntent);
                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, interval, interval, alarmIntent);
                    break;
                case "synchronisation":
                    if (PrefManager.getSyncEnabled())
                        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, interval, interval, alarmIntent);
                    else
                        alarmMgr.cancel(alarmIntent);
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

    public void makeNumberpicker(Bundle bundle){
        NumberPickerDialogFragment numberPickerDialogFragment = new NumberPickerDialogFragment().setOnSendClickListener(this);
        numberPickerDialogFragment.setArguments(bundle);
        numberPickerDialogFragment.show(getActivity().getFragmentManager(), "numberPickerDialogFragment");
    }

    @Override
    public void onUpdated(int number, int id) {
        PrefManager.setIGFColumns(number, id == R.string.preference_list_columns_portrait);
        PrefManager.commitChanges();
        startActivity(new Intent(context, Home.class));
        System.exit(0);
    }
}
