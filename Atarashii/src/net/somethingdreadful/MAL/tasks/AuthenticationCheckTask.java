package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.account.AccountType;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.ALModels.OAuth;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.database.DatabaseHelper;

public class AuthenticationCheckTask extends AsyncTask<String, Void, Boolean> {
    private final AuthenticationCheckListener callback;
    private final Activity activity;

    /**
     * Create an userAccount and verify it.
     *
     * Only use this with the FirstTimeActivity
     * @param callback Auth listener
     * @param activity The FirstTimeActivity
     */
    public AuthenticationCheckTask(AuthenticationCheckListener callback, Activity activity) {
        this.callback = callback;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            // Avoid overwrite issues
            if (DatabaseHelper.DBExists(activity))
                DatabaseHelper.deleteDatabase(activity);

            if (params != null && params.length >= 2) {
                MALApi api = new MALApi(params[0], params[1]);
                if (api.isAuth())
                    AccountService.addAccount(params[0], params[1], AccountType.MyAnimeList);
                return api.isAuth();
            } else if (params != null) {
                ALApi api = new ALApi(activity);

                OAuth auth = api.getAuthCode(params[0]);
                Profile profile = api.getCurrentUser();

                AccountService.addAccount(profile.getUsername(), "none", AccountType.AniList);
                AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
                AccountService.setRefreshToken(auth.refresh_token);

                PrefManager.setNavigationBackground(profile.getImageUrlBanner());
                return true;
            }
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "doInBackground()", e);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (callback != null) {
            callback.onAuthenticationCheckFinished(result);
        }
    }

    public interface AuthenticationCheckListener {
        void onAuthenticationCheckFinished(boolean result);
    }
}
