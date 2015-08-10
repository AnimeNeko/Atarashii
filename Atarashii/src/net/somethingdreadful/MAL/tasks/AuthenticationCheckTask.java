package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.account.AccountType;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.OAuth;
import net.somethingdreadful.MAL.api.response.UserProfile.Profile;

import retrofit.RetrofitError;

public class AuthenticationCheckTask extends AsyncTask<String, Void, Boolean> {
    private AuthenticationCheckListener callback;
    private String username;
    Activity activity;

    public AuthenticationCheckTask(AuthenticationCheckListener callback, Activity activity) {
        this.callback = callback;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
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
        } catch (RetrofitError re) {
            if (re.getResponse() != null && activity != null) {
                switch (re.getResponse().getStatus()) {
                    case 503: // Service Unavailable
                    case 504: // Gateway Timeout
                        Crashlytics.log(Log.ERROR, "MALX", "AuthenticationCheckTask.doInBackground(1): " + re.getMessage());
                        Theme.Snackbar(activity, R.string.toast_error_maintenance);
                        break;
                    default:
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                }
            } else {
                 Theme.Snackbar(activity, R.string.toast_error_maintenance);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "AuthenticationCheckTask.doInBackground(2): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (callback != null) {
            callback.onAuthenticationCheckFinished(result, username);
        }
    }

    public interface AuthenticationCheckListener {
        void onAuthenticationCheckFinished(boolean result, String username);
    }
}
