package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.somethingdreadful.MAL.MALManager;
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

    public NetworkTask(TaskJob job, MALApi.ListType type, Context context, Bundle data, NetworkTaskCallbackListener callback) {
        if(job == null || type == null || context == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.type = type;
        this.context = context;
        this.data = data;
        this.callback = callback;
    }

    private boolean isAnimeTask() {
        return type.equals(MALApi.ListType.ANIME);
    }

    @Override
    protected Object doInBackground(String... params) {
        if ( job == null ) {
            Log.e("MALX", "no job identifier, don't know what to do");
            return null;
        }
        int page = 1;

        if (data != null) {
            if (data.containsKey("page"))
                page = data.getInt("page",1);
        }

        Object result = null;
        MALManager mManager = new MALManager(context);
        try {
            switch (job) {
                case GETLIST:
                    if ( params != null )
                        result = isAnimeTask() ? mManager.getAnimeListFromDB(params[0]) : mManager.getMangaListFromDB(params[0]);
                    else
                        result = isAnimeTask() ? mManager.getAnimeListFromDB() : mManager.getMangaListFromDB();
                    break;
                case FORCESYNC:
                    if(isAnimeTask())
                        mManager.cleanDirtyAnimeRecords();
                    else
                        mManager.cleanDirtyMangaRecords();
                    result = isAnimeTask() ? mManager.downloadAndStoreAnimeList() : mManager.downloadAndStoreMangaList();
                    if ( result != null && params != null )
                        result = isAnimeTask() ? mManager.getAnimeListFromDB(params[0]) : mManager.getMangaListFromDB(params[0]);
                    break;
                case GETMOSTPOPULAR:
                    result = isAnimeTask() ? mManager.getAPIObject().getMostPopularAnime(page) : mManager.getAPIObject().getMostPopularManga(page);
                    break;
                case GETTOPRATED:
                    result = isAnimeTask() ? mManager.getAPIObject().getTopRatedAnime(page) : mManager.getAPIObject().getTopRatedManga(page);
                    break;
                case GETJUSTADDED:
                    result = isAnimeTask() ? mManager.getAPIObject().getJustAddedAnime(page) : mManager.getAPIObject().getJustAddedManga(page);
                    break;
                case GETUPCOMING:
                    result = isAnimeTask() ? mManager.getAPIObject().getUpcomingAnime(page) : mManager.getAPIObject().getUpcomingManga(page);
                    break;
                case GET:
                    if (data != null && data.containsKey("recordID"))
                        result = isAnimeTask() ? mManager.getAnimeRecord(data.getInt("recordID", -1)) : mManager.getMangaRecord(data.getInt("recordID", -1));
                    break;
                case GETDETAILS:
                    if (data != null && data.containsKey("record")) {
                        if(isAnimeTask()) {
                            Anime record = (Anime)data.getSerializable("record");
                            result = mManager.updateWithDetails(record.getId(), record);
                        } else {
                            Manga record = (Manga)data.getSerializable("record");
                            result = mManager.updateWithDetails(record.getId(), record);
                        }
                    }
                    break;
                case SEARCH:
                    if ( params != null ) {
                        /* The search API returns an 404 status code if nothing is found, that is the
                         * normal behavior and no error. So return an empty list in this case.
                         */
                        try {
                            result = isAnimeTask() ? mManager.getAPIObject().searchAnime(params[0], page) : mManager.getAPIObject().searchManga(params[0], page);
                        } catch (RetrofitError re) {
                            if (re.getResponse() != null) {
                                if (re.getResponse().getStatus() == 404)
                                    result = new ArrayList<Anime>();
                                else // thats really an error, throw it again for the outer catch
                                throw re;
                            }
                        }
                    }
                    break;
                default:
                    Log.e("MALX", String.format("%s-task invalid job identifier %s", type.toString(), job.name()));
            }
        } catch (RetrofitError re) {
            if (re.getResponse() != null)
                Log.e("MALX", String.format("%s-task API error on job %s: %d - %s", type.toString(), job.name(), re.getResponse().getStatus(), re.getResponse().getReason()));
            else
                Log.e("MALX", String.format("%s-task unknown API error on job %s", type.toString(), job.name()));
        } catch (Exception e) {
            Log.e("MALX", String.format("%s-task error on job %s: %s", type.toString(),  job.name(), e.getMessage()));
            result = null;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (callback != null)
            callback.onNetworkTaskFinished(result, job, type, data);
    }
}
