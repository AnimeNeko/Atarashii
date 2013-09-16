package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefeditor;
    private static Context context;

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
    
    public boolean autosync() {//profile activity, autosync the profile
        return prefs.getBoolean("PAutosync", true);
    }
    
    public boolean Wifisyncdisable() { //profile activity, if the setting use only wifi is turned on
        return prefs.getBoolean("Wifi_sync", false);
    }
    
    public String Getclickeduser() { //profile activity, Gets record of the clicked user
        return context.getSharedPreferences("Profile", 0).getString("Profileuser", "Error");
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
        int defList = Integer.parseInt(prefs.getString("defList", "1"));
        return defList;
    }
}
