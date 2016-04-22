package net.somethingdreadful.MAL.api;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class APIHelper {
    /**
     * Checks if the API is returning a successful response.
     * <p/>
     * With successful we mean 200-299.
     *
     * @param responseBody The responseBody.
     * @param methodName   Method name which made the request
     * @return boolean True is it was a good response
     */
    public static boolean isOK(Call<ResponseBody> responseBody, String methodName) {
        retrofit2.Response response = null;
        try {
            response = responseBody.execute();
            return response.isSuccessful();
        } catch (Exception e) {
            if (response != null)
                Crashlytics.log(Log.ERROR, "MALX", "MALApi." + methodName + "(): " + response.message());
            else
                Crashlytics.log(Log.ERROR, "MALX", "MALApi." + methodName + "(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This will log the exception for Crashlytics.
     *
     * @param activity Activity where the SnackBar should be shown
     * @param response Retrofit response
     * @param className The class name of the request executor
     * @param methodName method name
     * @param e The exception which was caught
     */
    public static void logE(Activity activity, Response response, String className, String methodName, Exception e) {
        if (response != null && activity != null) {
            Crashlytics.log(Log.ERROR, "MALX", className + "." + methodName + "(): " + response.message());
            switch (response.code()) {
                case 400: // Bad Request
                    Theme.Snackbar(activity, R.string.toast_error_api);
                    break;
                case 401: // Unauthorized
                    Crashlytics.log(Log.ERROR, "MALX", className + ".doInBackground(): User is not logged in");
                    Theme.Snackbar(activity, R.string.toast_info_password);
                    break;
                case 404: // Not Found
                    if (methodName.contains("search"))
                        Theme.Snackbar(activity, R.string.toast_error_nothingFound);
                    else
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                    Crashlytics.log(Log.ERROR, "MALX", className + ".doInBackground(): Error while getting records");
                    break;
                case 500: // Internal Server Error
                    Crashlytics.log(Log.ERROR, "MALX", className + ".doInBackground(): Internal server error, API bug?");
                    Theme.Snackbar(activity, R.string.toast_error_api);
                    break;
                case 503: // Service Unavailable
                case 504: // Gateway Timeout
                    Crashlytics.log(Log.ERROR, "MALX", className + ".doInBackground(): Gateway Timeout");
                    Theme.Snackbar(activity, R.string.toast_error_maintenance);
                    break;
                default:
                    Theme.Snackbar(activity, R.string.toast_error_Records);
                    Crashlytics.log(Log.ERROR, "MALX", className + ".doInBackground(): Unknown API error: " + response.code());
                    break;
            }
        } else {
            Crashlytics.log(Log.ERROR, "MALX", className + "." + methodName + "(): " + e.getMessage());
        }
        Crashlytics.logException(e);
        e.printStackTrace();
    }

    /**
     * Check if the device is connected with the internet.
     *
     * @param context The context.
     * @return boolean True if the app can receive internet requests
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
