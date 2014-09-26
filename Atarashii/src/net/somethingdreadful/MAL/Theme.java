package net.somethingdreadful.MAL;

import android.content.res.Configuration;

import org.holoeverywhere.ThemeManager;
import org.holoeverywhere.app.Application;

import java.util.Locale;

public class Theme extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PrefManager Prefs = new PrefManager(getApplicationContext());
        Locale locale = Prefs.getLocale();

        // apply it until the app remains cached
        Locale.setDefault(locale);
        Configuration newConfig = new Configuration();
        newConfig.locale = locale;
        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
    }

    static {
        ThemeManager.setDefaultTheme(ThemeManager.MIXED);
    }
}
