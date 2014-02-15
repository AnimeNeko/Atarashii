package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class FriendsNetworkTask extends AsyncTask<String, Void, ArrayList<User>> {
    private Context context;
    private boolean forcesync;
    FriendsNetworkTaskFinishedListener callback;
    
    public FriendsNetworkTask(Context context, boolean forcesync, FriendsNetworkTaskFinishedListener callback) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
    }

    @Override
    protected ArrayList<User> doInBackground(String... params) {
        ArrayList<User> result = null;
        if ( params == null ) {
            Log.e("MALX", "FriendsNetworkTask: no username to fetch friendlist");
            return null;
        }
        MALManager mManager = new MALManager(context);
        try {
            if ( forcesync && MALApi.isNetworkAvailable(context) ) {
                result = mManager.downloadAndStoreFriendList(params[0]);
            } else {
                result = mManager.getFriendListFromDB();
                if ( ( result == null || result.isEmpty() ) && MALApi.isNetworkAvailable(context) )
                    result = mManager.downloadAndStoreFriendList(params[0]);
            }

            /* returning null means there was an error, so return an empty ArrayList if there was no error
             * but an empty result
             */
            if ( result == null )
                result = new ArrayList<User>();
        } catch (Exception e) {
            Log.e("MALX", "error getting friendlist: " + e.getMessage());
            result = null;
        }
        
        return result;
    }
    
    @Override
    protected void onPostExecute(ArrayList<User> result) {
        if ( callback != null )
            callback.onFriendsNetworkTaskFinished(result);
    }
}
