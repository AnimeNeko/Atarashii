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

    /**
     * Returns the custom share text.
     *
     * @return String The custom share text that the app should use.
     * @see DetailView
     */
    public String getCustomShareText() {
        return prefs.getString("customShareText", context.getString(R.string.preference_default_customShareText));
    }

    /**
     * Returns if we should use a Listview.
     *
     * @return boolean If true the app will use a Listview
     * @see IGF
     */
    public boolean getTraditionalListEnabled() {
        return prefs.getBoolean("traditionalList", false);
    }

    /**
     * Returns if the app should use Volumes instead of Chapters.
     *
     * @return boolean If it is true the app will use Volumes
     * @see IGF
     */
    public boolean getUseSecondaryAmountsEnabled() {
        return prefs.getBoolean("displayVolumes", false);
    }

    /**
     * Returns if auto synchronisation is enabled.
     *
     * @return boolean This returns true when auto synchronisation is enabled
     * @see Settings
     */
    public boolean getSyncEnabled() {
        return prefs.getBoolean("synchronisation", false);
    }

    /**
     * Returns the auto synchronisation interval.
     *
     * @return int The auto synchronisation interval in seconds
     * @see Settings
     */
    public int getSyncTime() {
        return Integer.parseInt(prefs.getString("synchronisation_time", "60"));
    }

    /**
     * Returns the locale of the language that this app should use.
     *
     * @return Locale The locale of the language that the app should use
     * @see Theme
     */
    public Locale getLocale() {
        String localeName = prefs.getString("locale", Locale.getDefault().toString());
        Locale locale;
        if (localeName.equals("pt-br"))
            locale = new Locale("pt", "PT");
        else if (localeName.equals("pt-pt"))
            locale = new Locale("pt", "BR");
        else
            locale = new Locale(localeName);
        return locale;
    }

    /**
     * Returns if the app should synchronise the Anime/Manga records.
     *
     * @return boolean If all the records should be synced
     * @see Home
     */
    public boolean getForceSync() {
        return prefs.getBoolean("ForceSync", false);
    }

    /**
     * Set the force synchronisation, if true all the records will be synchronised.
     *
     * @param force This will determine if all the records should be synchronised
     * @see FirstTimeInit
     * @see Home
     */
    public void setForceSync(boolean force) {
        prefeditor.putBoolean("ForceSync", force);
    }

    /**
     * Returns if the app automatically should hide the card if it is empty.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public boolean getHideAnimeManga() {
        return prefs.getBoolean("a_mhide", false);
    }

    /**
     * Returns if the app should disable the coloured text and use black.
     *
     * @return boolean If it should be black
     * @see ProfileActivity
     */
    public boolean getTextColor() {
        return prefs.getBoolean("text_colours", false);
    }

    /**
     * Returns if the app should hide the anime stats in all profiles.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public boolean getHideAnime() {
        return prefs.getBoolean("A_hide", false);
    }

    /**
     * Returns if the app should hide the manga stats in all profiles.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public boolean getHideManga() {
        return prefs.getBoolean("M_hide", false);
    }

    /**
     * Returns the default list that will open on start.
     *
     * @return int The number of the list that should get loaded.
     * @see Home
     */
    public int getDefaultList() {
        return Integer.parseInt(prefs.getString("defList", "1"));
    }

    /**
     * Commit all the changed made.
     */
    public void commitChanges() {
        prefeditor.commit();
    }
}
