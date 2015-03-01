package net.somethingdreadful.MAL.tasks;

import android.os.AsyncTask;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.account.AccountType;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.OAuth;
import net.somethingdreadful.MAL.api.response.Profile;

public class AuthenticationCheckTask extends AsyncTask<String, Void, Boolean> {
    private AuthenticationCheckFinishedListener callback;
    private String username;

    public AuthenticationCheckTask(AuthenticationCheckFinishedListener callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params != null && params.length >= 2) {
            MALApi api = new MALApi(params[0], params[1]);
            return api.isAuth();
        } else if (params != null) {
            ALApi api = new ALApi();

            OAuth auth = api.getAuthCode(params[0]);
            Profile profile = api.getCurrentUser();

            AccountService.addAccount(profile.getDisplayName(), "none", AccountType.AniList);
            AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
            AccountService.setRefreshToken(auth.refresh_token);

            PrefManager.setNavigationBackground(profile.getImageUrlBanner());
            username = profile.getDisplayName();
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (callback != null) {
            callback.onAuthenticationCheckFinished(result, username);
        }
    }
}
