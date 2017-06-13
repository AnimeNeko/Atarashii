package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver;

public class WriteDetailTask extends AsyncTask<GenericRecord, Void, Boolean> {
    private ListType type = ListType.ANIME;
    private final Activity activity;

    public WriteDetailTask(ListType type, Activity activity) {
        this.type = type;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(GenericRecord... gr) {
        boolean error = false;
        boolean isNetworkAvailable = APIHelper.isNetworkAvailable(activity);
        ContentManager manager = new ContentManager(activity);

        if (!AccountService.isMAL() && isNetworkAvailable)
            error = manager.verifyAuthentication();

        try {
            // Sync details if there is network connection
            if (isNetworkAvailable && !error) {
                if (type.equals(ListType.ANIME)) {
                    error = !manager.writeAnimeDetails((Anime) gr[0]);
                } else {
                    error = !manager.writeMangaDetails((Manga) gr[0]);
                }
            }
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "WriteDetailTask.doInBackground(5, " + type + "): unknown API error (?): " + e.getMessage());
            AppLog.logException(e);
            error = true;
        }

        // Records updated successfully and will be marked as done if it hasn't been removed
        if (isNetworkAvailable && !error && !gr[0].getDeleteFlag()) {
            gr[0].clearDirty();
        }

        if (gr[0].getDeleteFlag()) {
            // Delete record
            if (ListType.ANIME.equals(type)) {
                manager.deleteAnime((Anime) gr[0]);
            } else {
                manager.deleteManga((Manga) gr[0]);
            }
        } else {
            // Save the records
            if (type.equals(ListType.ANIME)) {
                manager.saveAnimeToDatabase((Anime) gr[0]);
            } else {
                manager.saveMangaToDatabase((Manga) gr[0]);
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
    }
}