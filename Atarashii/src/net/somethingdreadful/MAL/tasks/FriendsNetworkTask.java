package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.util.ArrayList;

public class FriendsNetworkTask extends AsyncTask<String, Void, ArrayList<Profile>> {
    private final FriendsNetworkTaskListener callback;
    private final Context context;
    private final boolean forcesync;
    private final Activity activity;

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
        ContentManager mManager = new ContentManager(activity);
        try {
            if (forcesync && APIHelper.isNetworkAvailable(context)) {
                result = mManager.downloadAndStoreFriendList(params[0]);
            } else if (params[0].equalsIgnoreCase(AccountService.getUsername())) {
                result = mManager.getFriendListFromDB();
                if ((result == null || result.isEmpty()) && APIHelper.isNetworkAvailable(context))
                    result = mManager.downloadAndStoreFriendList(params[0]);
            } else {
                result = mManager.downloadAndStoreFriendList(params[0]);
            }

            /*
             * returning null means there was an error, so return an empty ArrayList if there was no error
             * but an empty result
             */
            if (result == null)
                result = new ArrayList<>();
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "doInBackground(5): task unknown API error (?)", e);
        }
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
