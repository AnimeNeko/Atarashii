package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.response.AnimeManga.Manga;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.RetrofitError;

public class NetworkTask extends AsyncTask<String, Void, Object> {
    TaskJob job;
    MALApi.ListType type;
    Context context;
    Bundle data;
    NetworkTaskCallbackListener callback;
    APIAuthenticationErrorListener authErrorCallback;
    Object taskResult;
    TaskJob[] arrayTasks = {TaskJob.GETLIST, TaskJob.FORCESYNC, TaskJob.GETMOSTPOPULAR, TaskJob.GETTOPRATED,
            TaskJob.GETJUSTADDED, TaskJob.GETUPCOMING, TaskJob.SEARCH, TaskJob.REVIEWS};


    public NetworkTask(TaskJob job, MALApi.ListType type, Context context, Bundle data, NetworkTaskCallbackListener callback, APIAuthenticationErrorListener authErrorCallback) {
        if (job == null || type == null || context == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.type = type;
        this.context = context;
        this.data = data;
        this.callback = callback;
        this.authErrorCallback = authErrorCallback;
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
        int page = 1;

        if (data != null && data.containsKey("page")) {
            page = data.getInt("page", 1);
            Crashlytics.setInt("Page", page);
        } else
            Crashlytics.setInt("Page", 0);

        taskResult = null;
        MALManager mManager = new MALManager(context);

        if (!AccountService.isMAL() && MALApi.isNetworkAvailable(context))
            mManager.verifyAuthentication();

        try {
            switch (job) {
                case GETLIST:
                    if (params != null)
                        taskResult = isAnimeTask() ? mManager.getAnimeListFromDB(params.length == 2 ? params[1] : Anime.STATUS_WATCHING, params[0]) : mManager.getMangaListFromDB(params.length == 2 ? params[1] : Manga.STATUS_READING, params[0]);
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

                        if (isAnimeTask())
                            mManager.cleanDirtyAnimeRecords(params[0]);
                        else
                            mManager.cleanDirtyMangaRecords(params[0]);
                        taskResult = isAnimeTask() ? mManager.downloadAndStoreAnimeList(params[0]) : mManager.downloadAndStoreMangaList(params[0]);
                        if (taskResult != null && params.length == 2)
                            taskResult = isAnimeTask() ? mManager.getAnimeListFromDB(params[1], params[0]) : mManager.getMangaListFromDB(params[1], params[0]);
                    }
                    break;
                case GETMOSTPOPULAR:
                    taskResult = isAnimeTask() ? mManager.getMostPopularAnime(page) : mManager.getMostPopularManga(page);
                    break;
                case GETTOPRATED:
                    taskResult = isAnimeTask() ? mManager.getTopRatedAnime(page) : mManager.getTopRatedManga(page);
                    break;
                case GETJUSTADDED:
                    taskResult = isAnimeTask() ? mManager.getJustAddedAnime(page) : mManager.getJustAddedManga(page);
                    break;
                case GETUPCOMING:
                    taskResult = isAnimeTask() ? mManager.getUpcomingAnime(page) : mManager.getUpcomingManga(page);
                    break;
                case GET:
                    if (data != null && data.containsKey("recordID")) {
                        Crashlytics.log(Log.INFO, "MALX", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, type, data.getInt("recordID", -1)));
                        if (AccountService.isMAL())
                            taskResult = isAnimeTask() ? mManager.getAnimeRecord(data.getInt("recordID", -1)) : mManager.getMangaRecord(data.getInt("recordID", -1));
                        else
                            taskResult = isAnimeTask() ? mManager.getAnimeRecord(data.getInt("recordID", -1)).createBaseModel() : mManager.getMangaRecord(data.getInt("recordID", -1)).createBaseModel();
                    }
                    break;
                case GETDETAILS:
                    if (data != null && data.containsKey("record"))
                        if (isAnimeTask()) {
                            Anime record = (Anime) data.getSerializable("record");
                            Crashlytics.log(Log.INFO, "MALX", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, type, record.getId()));
                            taskResult = mManager.updateWithDetails(record.getId(), record, "");
                            if (!AccountService.isMAL())
                                mManager.getAnime(record.getId(), AccountService.getUsername());
                        } else {
                            Manga record = (Manga) data.getSerializable("record");
                            Crashlytics.log(Log.INFO, "MALX", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, type, record.getId()));
                            taskResult = mManager.updateWithDetails(record.getId(), record, "");
                            if (!AccountService.isMAL())
                                mManager.getManga(record.getId(), AccountService.getUsername());
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
            if (re.getResponse() != null) {
                /* Search and Toplist API's are returning an 404 status code if nothing is found (nothing
                 * found for search, invalid page number for toplists, that is the normal behavior
                 * and no error. So return an empty list in this case.
                 */
                switch (re.getResponse().getStatus()) {
                    case 401:
                        Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): User is not logged in");
                        if (re.getResponse().getStatus() == 401 && authErrorCallback != null)
                            authErrorCallback.onAPIAuthenticationError(type, job);
                        break;
                    case 404:
                        if (job.equals(TaskJob.SEARCH) || job.equals(TaskJob.GETJUSTADDED) ||
                                job.equals(TaskJob.GETMOSTPOPULAR) || job.equals(TaskJob.GETTOPRATED) || job.equals(TaskJob.GETUPCOMING)) {
                            taskResult = new ArrayList<>();
                        } else {
                            Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): The requested page was not found");
                            Crashlytics.logException(re);
                        }
                        break;
                    case 500:
                        Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): Internal server error, API bug?");
                        Crashlytics.logException(re);
                    default:
                        doCallback(taskResult, true);
                        break;
                }
                Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task API error on job %s: %d - %s", type.toString(), job.name(), re.getResponse().getStatus(), re.getResponse().getReason()));
                return isArrayList() ? new ArrayList<>() : null;
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task unknown API error on job %s: %s", type.toString(), job.name(), re.getMessage()));
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task error on job %s: %s", type.toString(), job.name(), e.getMessage()));
            Crashlytics.logException(e);
            return isArrayList() ? new ArrayList<>() : null;
        }
        return taskResult;
    }

    @Override
    protected void onPostExecute(Object result) {
        doCallback(result, false);
    }

    private void doCallback(Object result, boolean cancelled) {
        if (callback != null) {
            if (result != null)
                callback.onNetworkTaskFinished(taskResult, job, type, data, cancelled);
            else
                callback.onNetworkTaskError(job, type, data, cancelled);
        }
    }
}
