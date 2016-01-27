package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;

import java.util.ArrayList;

import retrofit.RetrofitError;

public class UserNetworkTask extends AsyncTask<String, Void, Profile> {
    Context context;
    boolean forcesync;
    UserNetworkTaskListener callback;
    private Activity activity;

    public UserNetworkTask(Context context, boolean forcesync, UserNetworkTaskListener callback, Activity activity) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
        this.activity = activity;
    }

    @Override
    protected Profile doInBackground(String... params) {
        Profile result = null;
        if (params == null) {
            Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(): No username to fetch profile");
            return null;
        }
        MALManager mManager = new MALManager(context);

        try {
            if (!AccountService.isMAL() && MALApi.isNetworkAvailable(context))
                mManager.verifyAuthentication();

            if (forcesync && MALApi.isNetworkAvailable(context)) {
                result = mManager.getProfile(params[0]);
            } else if (params[0].equalsIgnoreCase(AccountService.getUsername())) {
                result = mManager.getProfileFromDB();
                if (result == null && MALApi.isNetworkAvailable(context))
                    result = mManager.getProfile(params[0]);
            } else if (MALApi.isNetworkAvailable(context)) {
                result = mManager.getProfile(params[0]);
            }

            if (result != null && MALApi.isNetworkAvailable(context)) {
                ArrayList<History> activities = mManager.getActivity(params[0]);
                result.setActivity(activities);
            }
        } catch (RetrofitError re) {
            if (re.getResponse() != null && activity != null) {
                switch (re.getResponse().getStatus()) {
                    case 400: // Bad Request
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 401: // Unauthorized
                        Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(1): User is not logged in");
                        Theme.Snackbar(activity, R.string.toast_info_password);
                        break;
                    case 404: // Not Found
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(2): The requested page was not found");
                        break;
                    case 500: // Internal Server Error
                        Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(3): Internal server error, API bug?");
                        Crashlytics.logException(re);
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 503: // Service Unavailable
                    case 504: // Gateway Timeout
                        Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(4): task unknown API error (503 Gateway Timeout)");
                        Theme.Snackbar(activity, R.string.toast_error_maintenance);
                        break;
                    default:
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        break;
                }
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(5): task unknown API error (?)");
                Theme.Snackbar(activity, R.string.toast_error_maintenance);
            }
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "doInBackground(5): task unknown API error (?)", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Profile result) {
        if (callback != null)
            callback.onUserNetworkTaskFinished(result);
    }

    public interface UserNetworkTaskListener {
        void onUserNetworkTaskFinished(Profile result);
    }
}
