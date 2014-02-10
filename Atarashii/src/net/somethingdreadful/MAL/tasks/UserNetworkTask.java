package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.response.User;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class UserNetworkTask extends AsyncTask<String, Void, User> {
    Context context;
    boolean forcesync;
    UserNetworkTaskFinishedListener callback;
    
    public UserNetworkTask(Context context, boolean forcesync, UserNetworkTaskFinishedListener callback) {
        this.context = context;
        this.forcesync = forcesync;
        this.callback = callback;
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected User doInBackground(String... params) {
        User result = null;
        if ( params == null ) {
            Log.e("MALX", "UserNetworkTask: no username to fetch profile");
            return null;
        }
        MALManager mManager = new MALManager(context);
        Log.d("MALX", "downloading profile of " + params[0]);
        
        if ( forcesync && isNetworkAvailable() ) {
           result = mManager.downloadAndStoreProfile(params[0]); 
        } else {
            result = mManager.getProfileFromDB(params[0]);
            if ( result == null && isNetworkAvailable() )
                result = mManager.downloadAndStoreProfile(params[0]);
        }
        return result;
    }
    
    @Override
    protected void onPostExecute(User result) {
        if (callback != null)
            callback.onUserNetworkTaskFinished(result);
    }
}
