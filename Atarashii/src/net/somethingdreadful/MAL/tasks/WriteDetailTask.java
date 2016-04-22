package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver;
import net.somethingdreadful.MAL.widgets.Widget1;

public class WriteDetailTask extends AsyncTask<GenericRecord, Void, Boolean> {
    private ListType type = ListType.ANIME;
    private final TaskJob job;
    private final APIAuthenticationErrorListener authErrorCallback;
    private final Activity activity;

    public WriteDetailTask(ListType type, TaskJob job, APIAuthenticationErrorListener authErrorCallback, Activity activity) {
        this.job = job;
        this.type = type;
        this.authErrorCallback = authErrorCallback;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(GenericRecord... gr) {
        boolean error = false;
        MALManager manager = new MALManager(activity);

        if (!AccountService.isMAL() && APIHelper.isNetworkAvailable(activity))
            manager.verifyAuthentication();

        try {
            if (APIHelper.isNetworkAvailable(activity)) {
                if (type.equals(ListType.ANIME)) {
                    Anime anime = (Anime) gr[0];
                    if (manager.writeAnimeDetails(anime))
                        anime.clearDirty();
                    manager.saveAnimeToDatabase(anime);
                } else {
                    Manga manga = (Manga) gr[0];
                    if (manager.writeMangaDetails(manga))
                        manga.clearDirty();
                    manager.saveMangaToDatabase(manga);
                }
            }
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

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // send broadcast for list updates
        Intent i = new Intent();
        i.setAction(RecordStatusUpdatedReceiver.RECV_IDENT);
        i.putExtra("type", type);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);

        Widget1.forceRefresh(activity);
    }
}