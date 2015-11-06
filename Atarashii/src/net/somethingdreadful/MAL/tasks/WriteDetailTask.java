package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver;
import net.somethingdreadful.MAL.widgets.Widget1;

import retrofit.RetrofitError;

public class WriteDetailTask extends AsyncTask<GenericRecord, Void, Boolean> {
    Context context;
    ListType type = ListType.ANIME;
    TaskJob job;
    APIAuthenticationErrorListener authErrorCallback;
    private Activity activity;

    public WriteDetailTask(ListType type, TaskJob job, Context context, APIAuthenticationErrorListener authErrorCallback, Activity activity) {
        this.context = context;
        this.job = job;
        this.type = type;
        this.authErrorCallback = authErrorCallback;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(GenericRecord... gr) {
        boolean error = false;
        MALManager manager = new MALManager(context);

        if (!AccountService.isMAL())
            manager.verifyAuthentication();

        try {
            if (MALApi.isNetworkAvailable(context)) {
                if (type.equals(ListType.ANIME))
                    manager.writeAnimeDetails((Anime) gr[0]);
                else
                    manager.writeMangaDetails((Manga) gr[0]);
                gr[0].clearDirty();
            }
        } catch (RetrofitError re) {
            if (re.getResponse() != null && activity != null) {
                switch (re.getResponse().getStatus()) {
                    case 400: // Bad Request
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 401: // Unauthorized
                        Crashlytics.log(Log.ERROR, "MALX", "WriteDetailTask.doInBackground(1, " + type + "): User is not logged in");
                        Theme.Snackbar(activity, R.string.toast_info_password);
                        if (authErrorCallback != null)
                            authErrorCallback.onAPIAuthenticationError(type, job);
                        break;
                    case 404: // Not Found
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        Crashlytics.log(Log.ERROR, "MALX", "WriteDetailTask.doInBackground(2, " + type + "): The requested page was not found");
                        break;
                    case 500: // Internal Server Error
                        Crashlytics.log(Log.ERROR, "MALX", "WriteDetailTask.doInBackground(3, " + type + "): Internal server error, API bug?");
                        Crashlytics.logException(re);
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 503: // Service Unavailable
                    case 504: // Gateway Timeout
                        Crashlytics.log(Log.ERROR, "MALX", "WriteDetailTask.doInBackground(4, " + type + "): " + job + "-task unknown API error (503 Gateway Timeout)");
                        Theme.Snackbar(activity, R.string.toast_error_maintenance);
                        break;
                    default:
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        break;
                }
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "WriteDetailTask.doInBackground(5, " + type + "): " + job + "-task unknown API error (?)");
                Theme.Snackbar(activity, R.string.toast_error_maintenance);
            }
            error = true;
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "WriteDetailTask.doInBackground(5, " + type + "): " + job + "-task unknown API error (?): " + e.getMessage());
            Crashlytics.logException(e);
            error = true;
        }

        // only update if everything went well!
        if (!error) {
            if (!job.equals(TaskJob.UPDATE)) {
                if (ListType.ANIME.equals(type)) {
                    manager.deleteAnime((Anime) gr[0]);
                } else {
                    manager.deleteManga((Manga) gr[0]);
                }
            } else {
                if (type.equals(ListType.ANIME)) {
                    manager.saveAnimeToDatabase((Anime) gr[0]);
                } else {
                    manager.saveMangaToDatabase((Manga) gr[0]);
                }
            }
        }
        manager.closeDB();

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // send broadcast for list updates
        Intent i = new Intent();
        i.setAction(RecordStatusUpdatedReceiver.RECV_IDENT);
        i.putExtra("type", type);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);

        Widget1.forceRefresh(context);
    }
}