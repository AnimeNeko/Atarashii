package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.response.Friend;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class FriendsNetworkTask extends AsyncTask<String, Void, ArrayList<Friend>> {
    private Context context;
    private boolean forcesync;
    FriendsNetworkTaskFinishedListener callback;
    
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    
    public FriendsNetworkTask(Context context, boolean forcesync, FriendsNetworkTaskFinishedListener callback) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
    }

    @Override
    protected ArrayList<Friend> doInBackground(String... params) {
        ArrayList<Friend> result = null;
        if ( params == null ) {
            Log.e("MALX", "FriendsNetworkTask: no username to fetch friendlist");
            return null;
        }
        MALManager mManager = new MALManager(context);
        
        if ( forcesync && isNetworkAvailable() ) {
            result = mManager.downloadAndStoreFriendList(params[0]);
        } else {
            result = mManager.getFriendListFromDB();
            if ( ( result == null || result.isEmpty() ) && isNetworkAvailable() )
                result = mManager.downloadAndStoreFriendList(params[0]);
        }
        
        return result;
    }
    
    @Override
    protected void onPostExecute(ArrayList<Friend> result) {
        if ( callback != null )
            callback.FriendsNetworkTaskFinished(result);
    }
}
