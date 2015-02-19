package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;

import java.util.ArrayList;

import retrofit.RetrofitError;

public class NetworkTask extends AsyncTask<String, Void, Object> {
    TaskJob job;
    MALApi.ListType type;
    Context context;
    Bundle data;
    NetworkTaskCallbackListener callback;
    APIAuthenticationErrorListener authErrorCallback;
    Object taskResult;
    boolean cancelled = false;

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
                    if (data != null && data.containsKey("record")) {
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
                        }
                    }
                    break;
                case SEARCH:
                    if (params != null)
                        taskResult = isAnimeTask() ? mManager.searchAnime(params[0], page) : mManager.searchManga(params[0], page);
                    break;
                default:
                    Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task invalid job identifier %s", type.toString(), job.name()));
            }
            /* if result is still null at this point there was no error but the API returned an empty result
             * (e. g. an empty anime-/mangalist), so create an empty list to let the callback know that
             * there was no error
             */
            if (taskResult == null && !job.equals(TaskJob.GETDETAILS) && !job.equals(TaskJob.GET))
                taskResult = isAnimeTask() ? new ArrayList<Anime>() : new ArrayList<Manga>();
        } catch (RetrofitError re) {
            if (re.getResponse() != null) {
                /* Search and Toplist API's are returning an 404 status code if nothing is found (nothing
                 * found for search, invalid page number for toplists, that is the normal behavior
                 * and no error. So return an empty list in this case.
                 */
                if (re.getResponse().getStatus() == 404 && (job.equals(TaskJob.SEARCH) || job.equals(TaskJob.GETJUSTADDED) ||
                        job.equals(TaskJob.GETMOSTPOPULAR) || job.equals(TaskJob.GETTOPRATED) || job.equals(TaskJob.GETUPCOMING))) {
                    taskResult = new ArrayList<Anime>();
                } else {
                    Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task API error on job %s: %d - %s", type.toString(), job.name(), re.getResponse().getStatus(), re.getResponse().getReason()));
                    // Authentication failed, fire callback!
                    if (re.getResponse().getStatus() == 401) {
                        if (authErrorCallback != null)
                            authErrorCallback.onAPIAuthenticationError(type, job);
                    }
                }
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task unknown API error on job %s: %s", type.toString(), job.name(), re.getMessage()));
            }
            re.printStackTrace();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "NetworkTask.doInBackground(): " + String.format("%s-task error on job %s: %s", type.toString(), job.name(), e.getMessage()));
            Crashlytics.logException(e);
            e.printStackTrace();
            taskResult = null;
        }
        return taskResult;
    }

    /* own cancel implementation, reason:
     * Force syncs should always complete in the background, even if the user switched to an other
     * list. If the user switches list to fast, the doInBackground function won't be called at all
     * because it is not guaranteed that both NetworkTasks (anime/manga) are running parallel. So
     * do a "soft-cancel" for FORCESYNC: don't cancel, but set cancelled to true so it can be set
     * in the callback to prevent updating the view with wrong entries.
     *
     * This "workaround" can be removed once we drop API level < 11, because then we can run the
     * tasks parallel (with AsyncTasks THREAD_POOL_EXECUTOR). This makes sure both NetworkTasks are
     * running parallel.
     */
    public boolean cancelTask() {
        if (!job.equals(TaskJob.FORCESYNC))
            return cancel(true);
        else {
            cancelled = true;
            return true;
        }
    }

    /* this one is called if the task is cancelled and the device API-level is < 11
     * TODO: when those old targets are dropped: remove this and make taskResult a local variable of doInBackground()
     */
    @Override
    protected void onCancelled() {
        doCallback(taskResult, true);
    }

    @Override
    protected void onCancelled(Object result) {
        doCallback(result, true);
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
