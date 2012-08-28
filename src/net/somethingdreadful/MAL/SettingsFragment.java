package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle state)
		{
			super.onCreate(state);
			
			PreferenceManager prefMgr = getPreferenceManager();
			prefMgr.setSharedPreferencesName("prefs");
			
			addPreferencesFromResource(R.xml.settings);
		}
}
