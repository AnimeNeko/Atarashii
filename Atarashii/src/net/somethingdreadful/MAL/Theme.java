package net.somethingdreadful.MAL;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;

import java.util.Locale;

public class Theme extends Application {

    Locale locale;
    Configuration config;

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);
        PrefManager.create(getApplicationContext());
        AccountService.create(getApplicationContext());
        
        locale = PrefManager.getLocale();
        config = new Configuration();
        config.locale = locale;
        setLanguage(); //Change language when it is started
        Crashlytics.setString("Language", locale.toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLanguage(); //Change language after orientation.
    }

    public void setLanguage() {
        Resources res = getBaseContext().getResources();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
