package net.somethingdreadful.MAL.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.database.DatabaseManager;
import net.somethingdreadful.MAL.dialog.RecordPickerDialog;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class Widget1 extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context c, final AppWidgetManager widgetManager, final int[] ids) {
        DatabaseManager db = new DatabaseManager(c);
        final Picasso picasso = Picasso.with(c);
        AccountService.create(c);
        ArrayList<GenericRecord> dbWidgetRecords = db.getWidgetRecords();
        final int number = dbWidgetRecords.size();
        final RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget1);

        Fabric.with(c, new Crashlytics());
        Fabric.with(c, new Answers());
        Crashlytics.log(Log.INFO, "Atarashii", "Widget1.onUpdate(): " + ids.length + " widgets and " + number + " records");
        Answers.getInstance().logCustom(new CustomEvent("Widget").putCustomAttribute("Widgets", ids.length));

        int updates = ids.length <= number ? ids.length : number;

        for (int i = 0; i < updates; i++) {
            final GenericRecord widgetRecord = dbWidgetRecords.get(i);
            final int finalI = i;
            widgetRecord.setId(widgetRecord.isAnime ? widgetRecord.getId() : widgetRecord.getId() * -1);

            picasso.load(widgetRecord.getImageUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            // Handle the + button clicks
                            PrefManager.create(c);
                            int watchValue = widgetRecord.isAnime ? ((Anime) widgetRecord).getWatchedEpisodes() :
                                    PrefManager.getUseSecondaryAmountsEnabled() ? ((Manga) widgetRecord).getVolumesRead() : ((Manga) widgetRecord).getChaptersRead();
                            int maxValue = widgetRecord.isAnime ? ((Anime) widgetRecord).getEpisodes() :
                                    PrefManager.getUseSecondaryAmountsEnabled() ? ((Manga) widgetRecord).getVolumes() : ((Manga) widgetRecord).getChapters();
                            if (watchValue == maxValue)
                                views.setViewVisibility(R.id.popUpButton, View.GONE);
                            else {
                                views.setViewVisibility(R.id.popUpButton, View.VISIBLE);
                                Intent addIntent = new Intent(c, Widget1.class).setAction(Intent.ACTION_EDIT).putExtra("id", widgetRecord.getId());
                                views.setOnClickPendingIntent(R.id.popUpButton, PendingIntent.getBroadcast(c, widgetRecord.getId() * 100 + 1, addIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                            }

                            // Handle the cover clicks
                            Intent viewIntent = new Intent(c, Widget1.class).setAction(Intent.ACTION_VIEW).putExtra("id", widgetRecord.getId());
                            views.setOnClickPendingIntent(R.id.coverImage, PendingIntent.getBroadcast(c, widgetRecord.getId() * 100 + 2, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                            Intent changeIntent = new Intent(c, Widget1.class).setAction(Intent.ACTION_PROVIDER_CHANGED).putExtra("id", widgetRecord.getId());
                            views.setOnClickPendingIntent(R.id.changeRecord, PendingIntent.getBroadcast(c, widgetRecord.getId() * 100 + 3, changeIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                            views.setTextViewText(R.id.animeName, widgetRecord.getTitle());
                            views.setTextViewText(R.id.watchedCount, String.valueOf(watchValue));

                            views.setImageViewBitmap(R.id.coverImage, bitmap);
                            widgetManager.updateAppWidget(ids[finalI], views);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }
    }

    public static void forceRefresh(Context context) {
        Intent updateWidgetIntent = new Intent(context, Widget1.class);
        updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(updateWidgetIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Widget1.class));
        AccountService.create(context);
        DatabaseManager db = new DatabaseManager(context);
        Fabric.with(context, new Crashlytics());
        int id = intent.getIntExtra("id", 0);

        switch (intent.getAction()) {
            case Intent.ACTION_EDIT:
                if (id > 0) {
                    Anime anime = db.getAnime(id);
                    anime.setWatchedEpisodes(anime.getWatchedEpisodes() + 1);
                    new WriteDetailTask(MALApi.ListType.ANIME, TaskJob.UPDATE, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, anime);
                } else {
                    PrefManager.create(context);
                    Manga manga = db.getManga(id * -1);
                    if (PrefManager.getUseSecondaryAmountsEnabled())
                        manga.setVolumesRead(manga.getVolumesRead() + 1);
                    else
                        manga.setChaptersRead(manga.getChaptersRead() + 1);

                    if (manga.getChaptersRead() == manga.getChapters() && manga.getChapters() != 0) {
                        manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
                        if (manga.getRereading()) {
                            manga.setRereadCount(manga.getRereadCount() + 1);
                            manga.setRereading(false);
                        }
                    }
                    new WriteDetailTask(MALApi.ListType.MANGA, TaskJob.UPDATE, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, manga);
                }
                onUpdate(context, AppWidgetManager.getInstance(context), ids);
                break;
            case Intent.ACTION_PROVIDER_CHANGED:
                Intent changeRecord = new Intent(context, RecordPickerDialog.class);
                changeRecord.putExtra("recordID", id > 0 ? id : id * -1);
                changeRecord.putExtra("recordType", id > 0 ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
                changeRecord.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(changeRecord);
                break;
            case Intent.ACTION_VIEW:
                Intent startDetails = new Intent(context, DetailView.class);
                startDetails.putExtra("recordID", id > 0 ? id : id * -1);
                startDetails.putExtra("recordType", id > 0 ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
                startDetails.putExtra("username", AccountService.getUsername());
                startDetails.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startDetails);
                break;
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                final int number = db.getWidgetRecords().size();
                if (intent.getBooleanExtra("checkGhost", false)) {
                    // Remove old widget records
                    if (number > ids.length) {
                        for (int i = 0; i < (number - ids.length); i++)
                            db.removeWidgetRecord();
                        Crashlytics.log(Log.INFO, "Atarashii", "Widget1.onUpdate(): Removing " + (number - ids.length) + " widget records");
                    }
                    // Remove ghost widgets
                    if (ids.length > number) {
                        for (int i = 0; i < (ids.length - number); i++)
                            (new AppWidgetHost(context, 1)).deleteAppWidgetId(ids[ids.length - 2]); // the array length starts with 1 and not 0
                        Crashlytics.log(Log.INFO, "Atarashii", "Widget1.onUpdate(): Removing " + (ids.length - number) + " ghost widgets");
                        ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Widget1.class));
                    }
                }
                onUpdate(context, AppWidgetManager.getInstance(context), ids);
                break;
        }
    }
}