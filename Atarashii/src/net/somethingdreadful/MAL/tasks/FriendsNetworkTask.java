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
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;

import java.util.ArrayList;

import retrofit.RetrofitError;

public class FriendsNetworkTask extends AsyncTask<String, Void, ArrayList<Profile>> {
    FriendsNetworkTaskListener callback;
    private Context context;
    private boolean forcesync;
    Activity activity;

    public FriendsNetworkTask(Context context, boolean forcesync, FriendsNetworkTaskListener callback, Activity activity) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
        this.activity = activity;
    }

    @Override
    protected ArrayList<Profile> doInBackground(String... params) {
        ArrayList<Profile> result = null;
        if (params == null) {
            Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(): No username to fetch friendlist");
            return null;
        }
        MALManager mManager = new MALManager(context);
        try {
            if (forcesync && MALApi.isNetworkAvailable(context)) {
                result = mManager.downloadAndStoreFriendList(params[0]);
            } else if (params[0].equalsIgnoreCase(AccountService.getUsername())) {
                result = mManager.getFriendListFromDB();
                if ((result == null || result.isEmpty()) && MALApi.isNetworkAvailable(context))
                    result = mManager.downloadAndStoreFriendList(params[0]);
            } else {
                result = mManager.getFriendList(params[0]);
            }

            /*
             * returning null means there was an error, so return an empty ArrayList if there was no error
             * but an empty result
             */
            if (result == null)
                result = new ArrayList<>();
        } catch (RetrofitError re) {
            if (re.getResponse() != null && activity != null) {
                switch (re.getResponse().getStatus()) {
                    case 400: // Bad Request
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 401: // Unauthorized
                        Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(1): User is not logged in");
                        Theme.Snackbar(activity, R.string.toast_info_password);
                        break;
                    case 404: // Not Found
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(2): The requested page was not found");
                        break;
                    case 500: // Internal Server Error
                        Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(3): Internal server error, API bug?");
                        Crashlytics.logException(re);
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 503: // Service Unavailable
                    case 504: // Gateway Timeout
                        Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(4): task unknown API error (503 Gateway Timeout)");
                        Theme.Snackbar(activity, R.string.toast_error_maintenance);
                        break;
                    default:
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        break;
                }
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(5): task unknown API error (?)");
                Theme.Snackbar(activity, R.string.toast_error_maintenance);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(5): task unknown API error (?): " + e.getMessage());
            Crashlytics.logException(e);
        }
        mManager.closeDB();

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Profile> result) {
        if (callback != null)
            callback.onFriendsNetworkTaskFinished(result);
    }

    public interface FriendsNetworkTaskListener {
        void onFriendsNetworkTaskFinished(ArrayList<Profile> result);
    }
}
