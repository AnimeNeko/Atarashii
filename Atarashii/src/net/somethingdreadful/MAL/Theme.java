package net.somethingdreadful.MAL;

import android.content.res.Configuration;

import org.holoeverywhere.ThemeManager;
import org.holoeverywhere.app.Application;

import java.util.Locale;

public class Theme extends Application {

    Locale locale;

    @Override
    public void onCreate() {
        super.onCreate();
        PrefManager Prefs = new PrefManager(getApplicationContext());
        locale = Prefs.getLocale();

        // apply it until the app remains cached
        Locale.setDefault(locale);
        Configuration newConfig = new Configuration();
        newConfig.locale = locale;
        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // apply it until the app remains cached
        Locale.setDefault(locale);
        Configuration Config = new Configuration();
        Config.locale = locale;
        getBaseContext().getResources().updateConfiguration(Config, getBaseContext().getResources().getDisplayMetrics());
    }

    static {
        ThemeManager.setDefaultTheme(ThemeManager.MIXED);
    }
}
