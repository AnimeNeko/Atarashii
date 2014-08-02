package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.RecordStatusUpdatedReceiver;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;

import retrofit.RetrofitError;

public class WriteDetailTask extends AsyncTask<GenericRecord, Void, Boolean> {
    Context context;
    ListType type;
    TaskJob job;
    APIAuthenticationErrorListener authErrorCallback;

    public WriteDetailTask(ListType type, TaskJob job, Context context, APIAuthenticationErrorListener authErrorCallback) {
        this.context = context;
        this.job = job;
        this.type = type;
        this.authErrorCallback = authErrorCallback;
    }

    @Override
    protected Boolean doInBackground(GenericRecord... gr) {
        boolean error = false;
        MALManager manager = new MALManager(context);

        try {
            if (MALApi.isNetworkAvailable(context)) {
                if (type.equals(ListType.ANIME)) {
                    manager.writeAnimeDetailsToMAL((Anime) gr[0]);
                } else {
                    manager.writeMangaDetailsToMAL((Manga) gr[0]);
                }
                gr[0].setDirty(false);
            }
        } catch (RetrofitError re) {
            if (re.getResponse() != null) {
                Log.e("MALX", String.format("%s-task API error on job %s: %d - %s", type.toString(), job.name(), re.getResponse().getStatus(), re.getResponse().getReason()));
                if (re.getResponse().getStatus() == 401) {
                    if (authErrorCallback != null)
                        authErrorCallback.onAPIAuthenticationError(type, job);
                }
            }
            error = true;
        } catch (Exception e) {
            Log.e("MALX", "error on response WriteDetailTask: " + e.getMessage());
            error = true;
        }

        // only update if everything went well!
        if (!error) {
            if (!job.equals(TaskJob.UPDATE)) {
                if (ListType.ANIME.equals(type)) {
                    manager.deleteAnimeFromDatabase((Anime) gr[0]);
                } else {
                    manager.deleteMangaFromDatabase((Manga) gr[0]);
                }
            } else {
                if (type.equals(ListType.ANIME)) {
                    manager.saveAnimeToDatabase((Anime) gr[0], false);
                } else {
                    manager.saveMangaToDatabase((Manga) gr[0], false);
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // send broadcast for list updates
        Intent i = new Intent();
        i.setAction(RecordStatusUpdatedReceiver.RECV_IDENT);
        i.putExtra("type", type);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }
}