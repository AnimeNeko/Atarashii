package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class Settings extends SherlockPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_activity_settings);

        //        //		PreferenceManager prefMgr = getPreferenceManager();
        //        //		prefMgr.setSharedPreferencesName("prefs");
        //
        //        getSupportFragmentManager().beginTransaction()
        //        .add(android.R.id.content, new SettingsFragment())
        //        .commit();

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("prefs");

        addPreferencesFromResource(R.xml.settings);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
        }


        return true;
    }

}
