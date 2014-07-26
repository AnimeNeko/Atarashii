package net.somethingdreadful.MAL;

import android.content.Context;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

public class PrefManager {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefeditor;
    private static Context context;


    public PrefManager(Context mContext) {
        context = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefeditor = prefs.edit();
    }

    public String getUser() {
        return prefs.getString("user", "failed");
    }

    public void setUser(String newUser) {
        prefeditor.putString("user", newUser);
    }

    public String getPass() {
        return prefs.getString("pass", "failed");
    }

    public void setPass(String newPass) {
        prefeditor.putString("pass", newPass);
    }

    public String getCustomShareText() {
        return prefs.getString("customShareText", context.getString(R.string.preference_default_customShareText));
    }

    public boolean getInit() {
        return prefs.getBoolean("init", false);
    }

    public void setInit(boolean newInit) {
        prefeditor.putBoolean("init", newInit);
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

    public void setsync_time_last(int time) { //Home, set the last auto-sync interval
        prefeditor.putInt("synchronisation_time_last", time);
    }

    public void commitChanges() {
        prefeditor.commit();
    }

    public int getDefaultList() {
        return Integer.parseInt(prefs.getString("defList", "1"));
    }
}
