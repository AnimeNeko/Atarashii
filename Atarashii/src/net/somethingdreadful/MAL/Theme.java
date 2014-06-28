package net.somethingdreadful.MAL;

import org.holoeverywhere.ThemeManager;
import org.holoeverywhere.app.Application;
import org.holoeverywhere.preference.PreferenceInit;

public class Theme extends Application {
    static {
        ThemeManager.setDefaultTheme(ThemeManager.MIXED);
    }
}
