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
        
        if ( forcesync && MALApi.isNetworkAvailable(context) ) {
            result = mManager.downloadAndStoreFriendList(params[0]);
        } else {
            result = mManager.getFriendListFromDB();
            if ( ( result == null || result.isEmpty() ) && MALApi.isNetworkAvailable(context) )
                result = mManager.downloadAndStoreFriendList(params[0]);
        }
        
        return result;
    }
    
    @Override
    protected void onPostExecute(ArrayList<User> result) {
        if ( callback != null )
            callback.onFriendsNetworkTaskFinished(result);
    }
}
