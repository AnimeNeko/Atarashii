package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class AppLog {
    /**
     * Init the Fabric plugins
     *
     * @param context The context
     */
    public static void initFabric(Context context) {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(context, crashlyticsKit);
        Fabric.with(context, new Answers());
    }

    /**
     * Log task crashes
     *
     * @param className The class name when the crash was caught.
     * @param message   The extra message to deliver.
     * @param e         The Error that has been caught.
     */
    public static void logTaskCrash(String className, String message, Exception e) {
        try {
            AppLog.log(Log.ERROR, "Atarashii", className + "." + message + ": " + e.getMessage());
            AppLog.logException(e);
            e.printStackTrace();
            Answers.getInstance().logCustom(new CustomEvent("Error (Task)").putCustomAttribute(className, message));
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /**
     * Log information about the requests.
     *
     * @param name The name of the parameter
     * @param data The log value/info
     */
    public static void setCrashData(String name, String data) {
        Crashlytics.setString(name, data);
    }

    /**
     * Log information about the user.
     *
     * @param name The name of the user
     */
    public static void setUserName(String name) {
        Crashlytics.setUserName(name);
    }

    /**
     * Log Exception.
     *
     * @param e The exception
     */
    public static void logException(Exception e) {
        Crashlytics.logException(e);
    }

    /**
     * Log information.
     *
     * @param name The name of the parameter
     * @param data The log value/info
     */
    public static void log(int level, String name, String data) {
        Crashlytics.log(level, name, data);
    }
}