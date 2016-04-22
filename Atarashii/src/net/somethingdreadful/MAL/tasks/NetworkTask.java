package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.widgets.Widget1;

import java.util.ArrayList;
import java.util.Arrays;

public class NetworkTask extends AsyncTask<String, Void, Object> {
    private TaskJob job;
    private MALApi.ListType type;
    private Activity activity = null;
    private Context context;
    private Bundle data;
    private NetworkTaskListener callback;
    private Object taskResult;
    private final TaskJob[] arrayTasks = {TaskJob.GETLIST, TaskJob.FORCESYNC, TaskJob.GETMOSTPOPULAR, TaskJob.GETTOPRATED,
            TaskJob.GETJUSTADDED, TaskJob.GETUPCOMING, TaskJob.SEARCH, TaskJob.REVIEWS};


    public NetworkTask(TaskJob job, MALApi.ListType type, Activity activity, Bundle data, NetworkTaskListener callback) {
        if (job == null || type == null || activity == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.type = type;
        this.activity = activity;
        this.data = data;
        this.callback = callback;
    }

    public NetworkTask(TaskJob job, MALApi.ListType type, Context context, NetworkTaskListener callback) {
        if (job == null || type == null || context == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.type = type;
        this.context = context;
        this.data = new Bundle();
        this.callback = callback;
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

        if (!APIHelper.isNetworkAvailable(getContext()) && !job.equals(TaskJob.GETLIST) && !job.equals(TaskJob.GETDETAILS)) {
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
        ContentManager mManager = new ContentManager(activity);

        if (!AccountService.isMAL() && APIHelper.isNetworkAvailable(getContext()))
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

                        if (isAnimeTask()) {
                            mManager.cleanDirtyAnimeRecords();
                            mManager.downloadAnimeList(AccountService.getUsername());
                            taskResult =  mManager.getAnimeListFromDB(params[0], Integer.parseInt(params[1]), params[2]);
                        } else {
                            mManager.cleanDirtyMangaRecords();
                            mManager.downloadMangaList(AccountService.getUsername());
                            taskResult =  mManager.getMangaListFromDB(params[0], Integer.parseInt(params[1]), params[2]);
                        }

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

                            if (APIHelper.isNetworkAvailable(activity)) {
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

                            if (APIHelper.isNetworkAvailable(activity)) {
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
