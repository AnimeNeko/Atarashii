package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;

public class UserNetworkTask extends AsyncTask<String, Void, User> {
    Context context;
    boolean forcesync;
    UserNetworkTaskFinishedListener callback;

    public UserNetworkTask(Context context, boolean forcesync, UserNetworkTaskFinishedListener callback) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
    }

    @Override
    protected User doInBackground(String... params) {
        User result;
        if (params == null) {
            Crashlytics.log(Log.ERROR, "MALX", "UserNetworkTask.doInBackground(): No username to fetch profile");
            return null;
        }
        MALManager mManager = new MALManager(context);

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

        if (!AccountService.isMAL() && result != null && params.length == 2) {
            if (MALApi.isNetworkAvailable(context)) {
                result.setActivity(mManager.downloadAndStoreActivity(params[1]));
            } else {
                result.setActivity(mManager.getActivityFromDB(params[1]));
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(User result) {
        if (callback != null)
            callback.onUserNetworkTaskFinished(result);
    }
}
