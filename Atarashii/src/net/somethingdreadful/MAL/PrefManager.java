package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PrefManager {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefeditor;
    private static Context context;
    
    String THEME_DARK_BLUE = "dark-blue";
	String THEME_LIGHT_BLUE = "light-blue";

    public PrefManager(Context mContext)
    {
        context = mContext;
        prefs = context.getSharedPreferences("prefs", 0);
        prefeditor = prefs.edit();
    }

    public String getUser() {
        return prefs.getString("user", "failed");
    }

    public String getPass() {
        return prefs.getString("pass", "failed");
    }

    public String getCustomShareText() {
        return prefs.getString("customShareText", "Check out $title; on MyAnimeList!\n$link;");
    }

    public boolean getUpgradeInit() {
        return prefs.getBoolean("upgradeInit", false);
    }

    public boolean getInit() {
        return prefs.getBoolean("init", false);
    }

    public boolean getTraditionalListEnabled() {
        return prefs.getBoolean("traditionalList", false);
    }

    public boolean getUseSecondaryAmountsEnabled() {
        return prefs.getBoolean("displayVolumes", false);
    }

    public boolean getsynchronisationEnabled() {
        return prefs.getBoolean("synchronisation", false);
    }

    public boolean getonly_wifiEnabled() { //Home, if the setting sync only at wifi is turned on
        return prefs.getBoolean("Only_wifi", false);
    }

    public Integer getsync_time() { //Home, get the auto-sync interval
        return Integer.parseInt(prefs.getString("synchronisation_time", "5"));
    }

    public Integer getsync_time_last() { //Home, get the last auto-sync interval
        return prefs.getInt("synchronisation_time_last", 1);
    }

    public boolean anime_manga_zero() { //profile activity, if the card is empty setting
        return prefs.getBoolean("a_mhide", false);
    }

    public boolean Textcolordisable() { //profile activity, if the textcolors are turned off
        return prefs.getBoolean("text_colours", false);
    }

    public boolean animehide() { //profile activity, if the setting force hide is turned on
        return prefs.getBoolean("A_hide", false); //anime card
    }

    public boolean mangahide() {//profile activity, if the setting force hide is turned on
        return prefs.getBoolean("M_hide", false); //manga card
    }
    public void setUser(String newUser) {
        prefeditor.putString("user", newUser);
    }

    public void setPass(String newPass) {
        prefeditor.putString("pass", newPass);
    }

    public void setInit(boolean newInit) {
        prefeditor.putBoolean("init", newInit);
    }

    public void setUpgradeInit(boolean newUpgradeInit) {
        prefeditor.putBoolean("upgradeInit", newUpgradeInit);
    }

    public void setLastSyncTime(long lastsync) {
        prefeditor.putLong("lastSync", lastsync);
    }

    public void setsync_time_last(int time) { //Home, set the last auto-sync interval
        prefeditor.putInt("synchronisation_time_last", time);
    }

    public void commitChanges() {
        prefeditor.commit();
    }

    public long getSyncFrequency() {
        long syncFrequency = 0;
        syncFrequency = Long.parseLong(prefs.getString("syncFrequency", "604800000"));
        return syncFrequency;
    }

    public long getLastSyncTime() {
        long lastsync = 0;
        lastsync = prefs.getLong("lastSync", 0);
        return lastsync;
    }

    public int getDefaultList() {
        return Integer.parseInt(prefs.getString("defList", "1"));
    }
    
    public int getTheme() {
    	int selectedTheme = 0;
    	String theme = prefs.getString("theme", "dark-blue");

    	if (theme.equals(THEME_DARK_BLUE)) {
    		selectedTheme = 0;
    	}
    	if (theme.equals(THEME_LIGHT_BLUE)) {
    		selectedTheme = R.style.Theme_Light_blue;
    	}
    	
		return selectedTheme;
    }
    
    public int getListStyle() {
    	int listStyle = R.layout.fragment_animelist_dark_blue;
    	String theme = prefs.getString("theme", "dark-blue");
    	
    	if (theme.equals(THEME_DARK_BLUE)) {
    		listStyle = R.layout.fragment_animelist_dark_blue;
    	}
    	if (theme.equals(THEME_LIGHT_BLUE)) {
    		listStyle = R.layout.fragment_animelist_light_blue;
    	}
    	
    	return listStyle;
    }
    
    public int getListItemStyle(String style) {
    	int itemStyle = R.layout.fragment_animelist_dark_blue;
    	String theme = prefs.getString("theme", "dark-blue");
    	
    	if(style.equals("grid")) {
    		if (theme.equals(THEME_DARK_BLUE)) {
    			itemStyle = R.layout.grid_cover_with_text_item_dark;
    		}
    		if (theme.equals(THEME_LIGHT_BLUE)) {
    			itemStyle = R.layout.grid_cover_with_text_item_light;
    		}
    	} else if (style.equals("list")) {
    		if (theme.equals(THEME_DARK_BLUE)) {
    			itemStyle = R.layout.list_cover_with_text_item_dark;
    		}
    		if (theme.equals(THEME_LIGHT_BLUE)) {
    			itemStyle = R.layout.list_cover_with_text_item_light;
    		}
    	}
    	
    	return itemStyle;
    }
    
    public int getDrawerIndicator() {
    	int indicatorStyle = R.drawable.ic_navigation_drawer_dark;
    	String theme = prefs.getString("theme", "dark-blue");

    	if (theme.equals(THEME_DARK_BLUE)) {
    		indicatorStyle = R.drawable.ic_navigation_drawer_dark;
		}
		if (theme.equals(THEME_LIGHT_BLUE)) {
			indicatorStyle = R.drawable.ic_navigation_drawer_light;
		}
		
		return indicatorStyle;
    }
}
