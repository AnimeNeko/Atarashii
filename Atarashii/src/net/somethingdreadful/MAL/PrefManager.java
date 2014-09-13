package net.somethingdreadful.MAL;

import android.content.Context;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import java.util.Locale;

public class PrefManager {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefeditor;
    private static Context context;


    public PrefManager(Context mContext) {
        context = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefeditor = prefs.edit();
    }

    public String getCustomShareText() {
        return prefs.getString("customShareText", context.getString(R.string.preference_default_customShareText));
    }

    public boolean getUpgradeInit() {
        return prefs.getBoolean("upgradeInit", false);
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

    public Integer getsync_time() { //Home, get the auto-sync interval
        return Integer.parseInt(prefs.getString("synchronisation_time", "60"));
    }

    public String getlocale() {
        return prefs.getString("locale", Locale.getDefault().toString());
    }

    public boolean ForceSync() {
        return prefs.getBoolean("ForceSync", false);
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

    public void setForceSync(boolean force) {
        prefeditor.putBoolean("ForceSync", force);
    }

    public void commitChanges() {
        prefeditor.commit();
    }

    public int getDefaultList() {
        return Integer.parseInt(prefs.getString("defList", "1"));
    }
}
