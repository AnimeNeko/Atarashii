package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.Log;

public class AppLog {
    /**
     * Init the Fabric plugins
     *
     * @param context The context
     */
    public static void initFabric(Context context) {

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
            e.printStackTrace();
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
    }

    /**
     * Log information about the user.
     *
     * @param name The name of the user
     */
    public static void setUserName(String name) {
    }

    /**
     * Log Exception.
     *
     * @param e The exception
     */
    public static void logException(Exception e) {
        e.printStackTrace();
    }

    /**
     * Log information.
     *
     * @param name The name of the parameter
     * @param data The log value/info
     */
    public static void log(int level, String name, String data) {
        Log.i(name + "l" + level, data);
    }
}