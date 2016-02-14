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
import net.somethingdreadful.MAL.api.ALModels.OAuth;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;

import retrofit.RetrofitError;

public class AuthenticationCheckTask extends AsyncTask<String, Void, Boolean> {
    private final AuthenticationCheckListener callback;
    private final Activity activity;

    public AuthenticationCheckTask(AuthenticationCheckListener callback, Activity activity) {
        this.callback = callback;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            if (params != null && params.length >= 2) {
                MALApi api = new MALApi(params[0], params[1]);
                if (api.isAuth())
                    AccountService.addAccount(params[0], params[1], AccountType.MyAnimeList);
                return api.isAuth();
            } else if (params != null) {
                ALApi api = new ALApi();

                OAuth auth = api.getAuthCode(params[0]);
                Profile profile = api.getCurrentUser();

                AccountService.addAccount(profile.getUsername(), "none", AccountType.AniList);
                AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
                AccountService.setRefreshToken(auth.refresh_token);

                PrefManager.setNavigationBackground(profile.getImageUrlBanner());
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
            Theme.logTaskCrash(this.getClass().getSimpleName(), "doInBackground(2)", e);
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
