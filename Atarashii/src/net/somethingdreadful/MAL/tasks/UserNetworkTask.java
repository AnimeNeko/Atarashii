package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.UserProfile.History;
import net.somethingdreadful.MAL.api.response.UserProfile.User;

import java.util.ArrayList;

public class UserNetworkTask extends AsyncTask<String, Void, User> {
    Context context;
    boolean forcesync;
    UserNetworkTaskListener callback;

    public UserNetworkTask(Context context, boolean forcesync, UserNetworkTaskListener callback) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
    }

    @Override
    protected User doInBackground(String... params) {
        User result = null;
        if (params == null) {
            Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(): No username to fetch profile");
            return null;
        }
        MALManager mManager = new MALManager(context);

        try {
            if (!AccountService.isMAL() && MALApi.isNetworkAvailable(context))
                mManager.verifyAuthentication();

            if (forcesync && MALApi.isNetworkAvailable(context)) {
                result = mManager.downloadAndStoreProfile(params[0]);
            } else {
                result = mManager.getProfileFromDB(params[0]);
                if ((result == null || (result.getProfile().getDetails().getAccessRank() == null && AccountService.isMAL())) && MALApi.isNetworkAvailable(context))
                    result = mManager.downloadAndStoreProfile(params[0]);
                else if (result != null && result.getProfile().getDetails().getAccessRank() == null && AccountService.isMAL())
                    result = null;
            }

            if (result != null) {
                ArrayList<History> activities = mManager.getActivityFromDB(params[0]);
                if (MALApi.isNetworkAvailable(context) && activities == null || MALApi.isNetworkAvailable(context) && forcesync)
                    activities = mManager.downloadAndStoreActivity(params[0]);
                result.setActivity(activities);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(): " + e.getMessage());
            Crashlytics.logException(e);
        }

        return result;
    }

    @Override
    protected void onPostExecute(User result) {
        if (callback != null)
            callback.onUserNetworkTaskFinished(result);
    }

    public interface UserNetworkTaskListener {
        void onUserNetworkTaskFinished(User result);
    }
}
