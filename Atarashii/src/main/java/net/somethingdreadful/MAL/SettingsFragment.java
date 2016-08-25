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

import net.somethingdreadful.MAL.broadcasts.AutoSync;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, NumberPickerDialogFragment.onUpdateClickListener {
    private Context context;
    private AlarmManager alarmMgr;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        addPreferencesFromResource(R.xml.settings);
        findPreference("reset").setOnPreferenceClickListener(this);
        findPreference("IGFcolumnsportrait").setOnPreferenceClickListener(this);
        findPreference("IGFcolumnslandscape").setOnPreferenceClickListener(this);

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
                case "darkTheme":
                    PrefManager.setTextColor(true);
                    PrefManager.commitChanges();
                    sharedPreferences.edit().commit();
                    startActivity(new Intent(context, Home.class));
                    System.exit(0);
            }
        } catch (Exception e) {
            Theme.log(Log.ERROR, "Atarashii", "SettingsFragment.onSharedPreferenceChanged(): " + e.getMessage());
            Theme.logException(e);
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
            case "reset":
                PrefManager.clear();
                startActivity(new Intent(context, Home.class));
                System.exit(0);
                break;
        }
        return false;
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
        }
    }
}
