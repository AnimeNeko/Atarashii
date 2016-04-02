package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.widgets.Widget1;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.RetrofitError;

public class NetworkTask extends AsyncTask<String, Void, Object> {
    private TaskJob job;
    private MALApi.ListType type;
    private Activity activity;
    private Context context;
    private Bundle data;
    private NetworkTaskListener callback;
    private APIAuthenticationErrorListener authErrorCallback;
    private Object taskResult;
    private final TaskJob[] arrayTasks = {TaskJob.GETLIST, TaskJob.FORCESYNC, TaskJob.GETMOSTPOPULAR, TaskJob.GETTOPRATED,
            TaskJob.GETJUSTADDED, TaskJob.GETUPCOMING, TaskJob.SEARCH, TaskJob.REVIEWS};


    public NetworkTask(TaskJob job, MALApi.ListType type, Activity activity, Bundle data, NetworkTaskListener callback, APIAuthenticationErrorListener authErrorCallback) {
        if (job == null || type == null || activity == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.type = type;
        this.activity = activity;
        this.data = data;
        this.callback = callback;
        this.authErrorCallback = authErrorCallback;
    }

    public NetworkTask(TaskJob job, MALApi.ListType type, Context context, NetworkTaskListener callback, APIAuthenticationErrorListener authErrorCallback) {
        if (job == null || type == null || context == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.type = type;
        this.context = context;
        this.data = new Bundle();
        this.callback = callback;
        this.authErrorCallback = authErrorCallback;
    }

    private Context getContext() {
        return context != null ? context : activity;
    }

    private boolean isAnimeTask() {
        return type.equals(MALApi.ListType.ANIME);
    }

    private boolean isArrayList() {
        return Arrays.asList(arrayTasks).contains(job);
    }

    @Override
    protected Object doInBackground(String... params) {
        if (job == null) {
            Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): No job identifier, don't know what to do");
            return null;
        }

        if (!MALApi.isNetworkAvailable(getContext()) && !job.equals(TaskJob.GETLIST) && !job.equals(TaskJob.GETDETAILS)) {
            if (activity != null)
                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
            return null;
        }

        int page = 1;

        if (data != null && data.containsKey("page")) {
            page = data.getInt("page", 1);
            Crashlytics.setInt("Page", page);
        } else
            Crashlytics.setInt("Page", 0);

        taskResult = null;
        MALManager mManager = new MALManager(getContext());

        if (!AccountService.isMAL() && MALApi.isNetworkAvailable(getContext()))
            mManager.verifyAuthentication();

        try {
            switch (job) {
                case GETLIST:
                    if (params != null)
                        taskResult = isAnimeTask() ? mManager.getAnimeListFromDB(params[0], Integer.parseInt(params[1]), params[2]) : mManager.getMangaListFromDB(params[0], Integer.parseInt(params[1]), params[2]);
                    break;
                case FORCESYNC:
                    if (params != null) {
                        /* FORCESYNC may not require authentication if there are no dirty records to update, so a forced sync would even
                         * work if the password has changed, which would be strange for the user. So do an Auth-Check before syncing
                         *
                         * this will throw an RetrofitError-Exception if the credentials are wrong
                         */
                        if (AccountService.isMAL())
                            mManager.verifyAuthentication();

                        mManager.cleanDirtyAnimeRecords();
                        mManager.cleanDirtyMangaRecords();

                        taskResult = isAnimeTask() ? mManager.downloadAndStoreAnimeList(AccountService.getUsername()) : mManager.downloadAndStoreMangaList(AccountService.getUsername());
                        taskResult = isAnimeTask() ? mManager.getAnimeListFromDB(params[0], Integer.parseInt(params[1]), params[2]) : mManager.getMangaListFromDB(params[0], Integer.parseInt(params[1]), params[2]);

                        Widget1.forceRefresh(getContext());
                    }
                    break;
                case GETMOSTPOPULAR:
                    taskResult = isAnimeTask() ? mManager.getMostPopularAnime(page).getAnime() : mManager.getMostPopularManga(page).getManga();
                    break;
                case GETTOPRATED:
                    taskResult = isAnimeTask() ? mManager.getTopRatedAnime(page).getAnime() : mManager.getTopRatedManga(page).getManga();
                    break;
                case GETJUSTADDED:
                    taskResult = isAnimeTask() ? mManager.getJustAddedAnime(page).getAnime() : mManager.getJustAddedManga(page).getManga();
                    break;
                case GETUPCOMING:
                    taskResult = isAnimeTask() ? mManager.getUpcomingAnime(page).getAnime() : mManager.getUpcomingManga(page).getManga();
                    break;
                case GETDETAILS:
                    if (data != null && data.containsKey("recordID"))
                        if (isAnimeTask()) {
                            // Get Anime from database
                            Anime record = mManager.getAnime(data.getInt("recordID", -1));

                            if (MALApi.isNetworkAvailable(activity)) {
                                // Get records from the website
                                if (record == null)
                                    record = mManager.getAnimeRecord(data.getInt("recordID", -1));

                                // Check if the record is on the animelist.
                                // after that load details if synopsis == null or else return the DB record
                                if ((record.getSynopsis() == null || params[0].equals("true")) && record.getWatchedStatus() != null) {
                                    Crashlytics.log(Log.INFO, "MALX", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, type, record.getId()));
                                    taskResult = mManager.updateWithDetails(record.getId(), record);
                                } else {
                                    taskResult = record;
                                }
                            } else if (record != null) {
                                taskResult = record;
                            } else {
                                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                            }
                        } else {
                            // Get Manga from database
                            Manga record = mManager.getManga(data.getInt("recordID", -1));

                            if (MALApi.isNetworkAvailable(activity)) {
                                // Get records from the website
                                if (record == null)
                                    record = mManager.getMangaRecord(data.getInt("recordID", -1));

                                // Check if the record is on the mangalist
                                // load details if synopsis == null or else return the DB record
                                if ((record.getSynopsis() == null || params[0].equals("true")) && record.getReadStatus() != null) {
                                    Crashlytics.log(Log.INFO, "MALX", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, type, record.getId()));
                                    taskResult = mManager.updateWithDetails(record.getId(), record);
                                } else {
                                    taskResult = record;
                                }
                            } else if (record != null) {
                                taskResult = record;
                            } else {
                                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                            }
                        }
                    break;
                case SEARCH:
                    if (params != null)
                        taskResult = isAnimeTask() ? mManager.searchAnime(params[0], page) : mManager.searchManga(params[0], page);
                    break;
                case REVIEWS:
                    if (params != null)
                        taskResult = isAnimeTask() ? mManager.getAnimeReviews(Integer.parseInt(params[0]), page) : mManager.getMangaReviews(Integer.parseInt(params[0]), page);
                    break;
                default:
                    Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task invalid job identifier %s", type.toString(), job.name()));
            }
            /* if result is still null at this point there was no error but the API returned an empty result
             * (e. g. an empty anime-/mangalist), so create an empty list to let the callback know that
             * there was no error
             */
            if (taskResult == null)
                return isArrayList() ? new ArrayList<>() : null;
        } catch (RetrofitError re) {
            if (re.getResponse() != null && activity != null) {
                /* Search and Toplist API's are returning an 404 status code if nothing is found (nothing
                 * found for search, invalid page number for toplists, that is the normal behavior
                 * and no error. So return an empty list in this case.
                 */
                switch (re.getResponse().getStatus()) {
                    case 400: // Bad Request
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 401: // Unauthorized
                        Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): User is not logged in");
                        Theme.Snackbar(activity, R.string.toast_info_password);
                        if (re.getResponse().getStatus() == 401 && authErrorCallback != null)
                            authErrorCallback.onAPIAuthenticationError(type, job);
                        break;
                    case 404: // Not Found
                        if (isArrayList()) {
                            taskResult = new ArrayList<>();
                            if (job.equals(TaskJob.SEARCH))
                                Theme.Snackbar(activity, R.string.toast_error_nothingFound);
                            else
                                Theme.Snackbar(activity, R.string.toast_error_Records);
                        } else {
                            Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): The requested page was not found");
                            Crashlytics.logException(re);
                            Theme.Snackbar(activity, R.string.toast_error_api);
                        }
                        break;
                    case 500: // Internal Server Error
                        Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): Internal server error, API bug?");
                        Crashlytics.logException(re);
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 503: // Service Unavailable
                    case 504: // Gateway Timeout
                        Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task unknown API error on job %s: %s", type.toString(), job.name(), re.getMessage()));
                        Theme.Snackbar(activity, R.string.toast_error_maintenance);
                        break;
                    default:
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        break;
                }
                Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task API error on job %s: %d - %s", type.toString(), job.name(), re.getResponse().getStatus(), re.getResponse().getReason()));
                Crashlytics.logException(re);
                return isArrayList() ? new ArrayList<>() : null;
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task unknown API error on job %s: %s", type.toString(), job.name(), re.getMessage()));
                if (activity != null)
                    Theme.Snackbar(activity, R.string.toast_error_maintenance);
            }
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "doInBackground(): " + String.format("%s-task error on job %s", type.toString(), job.name()), e);
            return isArrayList() && !job.equals(TaskJob.FORCESYNC) && !job.equals(TaskJob.GETLIST) ? new ArrayList<>() : null;
        }
        return taskResult;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (callback != null) {
            if (result != null)
                callback.onNetworkTaskFinished(taskResult, job, type, data, false);
            else
                callback.onNetworkTaskError(job, type, data, false);
        }
    }

    public interface NetworkTaskListener {
        void onNetworkTaskFinished(Object result, TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled);

        void onNetworkTaskError(TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled);
    }
}
