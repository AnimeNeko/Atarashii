package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

public class PrefManager {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefEditor;
    private static Context context;


    public static void create(Context mContext) {
        context = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefEditor = prefs.edit();
    }

    /**
     * Remove the old account info.
     *
     * The username & password were saved in Atarashii! < 2.0.
     * Currently we are using this to remove the old username and password.
     *
     * @see net.somethingdreadful.MAL.account.AccountService
     */
    public static void deleteAccount() {
        if (prefs.getString("user", null) != null) {
            prefEditor.remove("user");
            prefEditor.remove("pass");
        }
    }

    /**
     * Returns the custom share text.
     *
     * @return String The custom share text that the app should use.
     * @see DetailView
     */
    public static String getCustomShareText() {
        return prefs.getString("customShareText", context.getString(R.string.preference_default_customShareText));
    }

    /**
     * Returns if we should use a Listview.
     *
     * @return boolean If true the app will use a Listview
     * @see IGF
     */
    public static boolean getTraditionalListEnabled() {
        return prefs.getBoolean("traditionalList", false);
    }

    /**
     * Returns if the app should use Volumes instead of Chapters.
     *
     * @return boolean If it is true the app will use Volumes
     * @see IGF
     */
    public static boolean getUseSecondaryAmountsEnabled() {
        return prefs.getBoolean("displayVolumes", false);
    }

    /**
     * Returns if auto synchronisation is enabled.
     *
     * @return boolean This returns true when auto synchronisation is enabled
     * @see Settings
     */
    public static boolean getSyncEnabled() {
        return prefs.getBoolean("synchronisation", false);
    }

    /**
     * Returns the auto synchronisation interval.
     *
     * @return int The auto synchronisation interval in seconds
     * @see Settings
     */
    public static int getSyncTime() {
        return Integer.parseInt(prefs.getString("synchronisation_time", "60"));
    }

    /**
     * Returns the locale of the language that this app should use.
     *
     * @return Locale The locale of the language that the app should use
     * @see Theme
     */
    public static Locale getLocale() {
        String localeName = prefs.getString("locale", Locale.getDefault().toString());
        Locale locale;
        switch (localeName) {
            case "pt-br":
                locale = new Locale("pt", "PT");
                break;
            case "pt-pt":
                locale = new Locale("pt", "BR");
                break;
            default:
                locale = new Locale(localeName);
                break;
        }
        return locale;
    }

    /**
     * Returns if the app should synchronise the Anime/Manga records.
     *
     * @return boolean If all the records should be synced
     * @see Home
     */
    public static boolean getForceSync() {
        return prefs.getBoolean("ForceSync", false);
    }

    /**
     * Set the force synchronisation, if true all the records will be synchronised.
     *
     * @param force This will determine if all the records should be synchronised
     * @see FirstTimeInit
     * @see Home
     */
    public static void setForceSync(boolean force) {
        prefEditor.putBoolean("ForceSync", force);
    }

    /**
     * Returns if the app should disable the coloured text and use black.
     *
     * @return boolean If it should be black
     * @see ProfileActivity
     */
    public static boolean getTextColor() {
        return prefs.getBoolean("text_colours", false);
    }

    /**
     * Returns if the app should hide the anime stats in all profiles.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public static boolean getHideAnime() {
        return prefs.getBoolean("A_hide", false);
    }

    /**
     * Returns if the app should hide the manga stats in all profiles.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public static boolean getHideManga() {
        return prefs.getBoolean("M_hide", false);
    }

    /**
     * Returns the default list that will open on start.
     *
     * @return int The number of the list that should get loaded.
     * @see Home
     */
    public static int getDefaultList() {
        return Integer.parseInt(prefs.getString("defList", "1"));
    }

    /**
     * Returns the url of the navigation drawer background.
     *
     * @return string The url of the image.
     * @see Home
     */
    public static String getNavigationBackground() {
        return prefs.getString("navigationDrawer_image", null);
    }

    /**
     * Set the navigation drawer background.
     *
     * @param image The URL
     */
    public static void setNavigationBackground(String image) {
        prefEditor.putString("navigationDrawer_image", image);
    }

    /**
     * Returns if the IGF to show airing records only.
     *
     * @return boolean If true than hide all the null airing dates.
     * @see Home
     */
    public static boolean getAiringOnly() {
        return prefs.getBoolean("IGF_airingOnly", false);
    }

    /**
     * Set toggle the IGF to show airing records only.
     *
     * @param airing If true than the airing records should only be shown
     */
    public static void setAiringOnly(boolean airing) {
        prefEditor.putBoolean("IGF_airingOnly", airing);
    }

    /**
     * Commit all the changed made.
     */
    public static void commitChanges() {
        prefEditor.commit();
    }
}
