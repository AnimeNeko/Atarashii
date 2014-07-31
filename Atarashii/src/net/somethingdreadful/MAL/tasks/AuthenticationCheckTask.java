package net.somethingdreadful.MAL.tasks;

import android.os.AsyncTask;

import net.somethingdreadful.MAL.api.MALApi;

public class AuthenticationCheckTask extends AsyncTask<String, Void, Boolean> {
    private AuthenticationCheckFinishedListener callback;

    public AuthenticationCheckTask(AuthenticationCheckFinishedListener callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params != null && params.length >= 2) {
            MALApi api = new MALApi(params[0], params[1]);
            return api.isAuth();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (callback != null) {
            callback.onAuthenticationCheckFinished(result);
        }
    }
}
