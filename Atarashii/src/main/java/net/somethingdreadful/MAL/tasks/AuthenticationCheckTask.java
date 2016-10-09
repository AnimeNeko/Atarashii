package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
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
     * <p/>
     * Only use this with the FirstTimeActivity
     *
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
                boolean valid = api.isAuth();
                if (valid) {
                    AccountService.addAccount(params[0], params[1], AccountType.MyAnimeList);
                    (new ContentManager(activity)).getProfile(params[0]);
                }
                return valid;
            } else if (params != null) {
                ALApi api = new ALApi(activity);

                OAuth auth = api.getAuthCode(params[0]);
                Profile profile = api.getCurrentUser();
                if (profile == null) // try again
                    profile = api.getCurrentUser();

                if (profile == null) {
                    Theme.Snackbar(activity, R.string.toast_error_keys);
                } else {
                    AccountService.addAccount(profile.getUsername(), "none", AccountType.AniList);
                    AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
                    AccountService.setRefreshToken(auth.refresh_token);

                    PrefManager.setNavigationBackground(profile.getImageUrlBanner());
                    return true;
                }
            }
        } catch (Exception e) {
            AppLog.logTaskCrash("AuthenticationCheckTask", "doInBackground()", e);
            e.printStackTrace();
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
