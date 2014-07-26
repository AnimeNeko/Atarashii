package net.somethingdreadful.MAL;

import org.holoeverywhere.ThemeManager;
import org.holoeverywhere.app.Application;

public class Theme extends Application {
    static {
        ThemeManager.setDefaultTheme(ThemeManager.MIXED);
    }
}
