package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
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
    private final boolean forcesync;
    private final Activity activity;
    private final int id;

    public FriendsNetworkTask(boolean forcesync, FriendsNetworkTaskListener callback, Activity activity, int id) {
        this.forcesync = forcesync;
        this.callback = callback;
        this.activity = activity;
        this.id = id;
    }

    @Override
    protected ArrayList<Profile> doInBackground(String... params) {
        boolean isNetworkAvailable = APIHelper.isNetworkAvailable(activity);
        ArrayList<Profile> result = null;
        if (params == null) {
            Crashlytics.log(Log.ERROR, "Atarashii", "FriendsNetworkTask.doInBackground(): No username to fetch friendlist");
            return null;
        }
        ContentManager cManager = new ContentManager(activity);
        try {
            if (forcesync && isNetworkAvailable) {
                result = request(cManager, params[0]);
            } else if (params[0].equalsIgnoreCase(AccountService.getUsername()) && id != 1) {
                result = cManager.getFriendListFromDB();
                if ((result == null || result.isEmpty()) && isNetworkAvailable)
                    result = request(cManager, params[0]);
            } else if (id != 1 || isNetworkAvailable) {
                result = request(cManager, params[0]);
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

    private ArrayList<Profile> request(ContentManager cManager, String param) {
        switch (id) {
            case 0:
                return cManager.downloadAndStoreFriendList(param);
            case 1:
                return cManager.getFollowers(param);
            default:
                return cManager.downloadAndStoreFriendList(param);
        }
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
