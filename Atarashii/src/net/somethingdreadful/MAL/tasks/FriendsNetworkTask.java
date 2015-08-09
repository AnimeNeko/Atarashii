package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.UserProfile.User;

import java.util.ArrayList;

public class FriendsNetworkTask extends AsyncTask<String, Void, ArrayList<User>> {
    FriendsNetworkTaskListener callback;
    private Context context;
    private boolean forcesync;

    public FriendsNetworkTask(Context context, boolean forcesync, FriendsNetworkTaskListener callback) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
    }

    @Override
    protected ArrayList<User> doInBackground(String... params) {
        ArrayList<User> result;
        if (params == null) {
            Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(): No username to fetch friendlist");
            return null;
        }
        MALManager mManager = new MALManager(context);
        try {
            if (forcesync && MALApi.isNetworkAvailable(context)) {
                result = mManager.downloadAndStoreFriendList(params[0]);
            } else {
                result = mManager.getFriendListFromDB(params[0]);
                if ((result == null || result.isEmpty()) && MALApi.isNetworkAvailable(context))
                    result = mManager.downloadAndStoreFriendList(params[0]);
            }

            /*
             * returning null means there was an error, so return an empty ArrayList if there was no error
             * but an empty result
             */
            if (result == null)
                result = new ArrayList<User>();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "FriendsNetworkTask.doInBackground(): " + e.getMessage());
            result = null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<User> result) {
        if (callback != null)
            callback.onFriendsNetworkTaskFinished(result);
    }

    public interface FriendsNetworkTaskListener {
        void onFriendsNetworkTaskFinished(ArrayList<User> result);
    }
}
