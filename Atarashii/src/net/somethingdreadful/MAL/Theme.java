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
        String localeName = Prefs.getlocale();
        Locale locale;
        if (localeName.equals("pt-br"))
            locale = new Locale("pt", "PT");
        else if (localeName.equals("pt-pt"))
            locale = new Locale("pt", "BR");
        else
            locale = new Locale(Prefs.getlocale());
        Locale.setDefault(locale);

        Configuration newConfig = new Configuration();
        newConfig.locale = locale;
        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
    }

    static {
        ThemeManager.setDefaultTheme(ThemeManager.MIXED);
    }
}
