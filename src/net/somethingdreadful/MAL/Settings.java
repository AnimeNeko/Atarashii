package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;
import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class Settings extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.title_activity_settings);

		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName("prefs");


		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);

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
