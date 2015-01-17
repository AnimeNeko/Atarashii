package net.somethingdreadful.MAL.account;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import net.somethingdreadful.MAL.Home;
import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.R;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }

    @Override
    public void onPerformSync(Account arg0, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Intent notificationIntent = new Intent(context, Home.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder syncNotificationBuilder = new Notification.Builder(context).setOngoing(true)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.toast_info_SyncMessage));
        Notification syncNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                syncNotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            syncNotification = syncNotificationBuilder.build();
        } else {
            syncNotification = syncNotificationBuilder.getNotification();
        }
        nm.notify(R.id.notification_sync, syncNotification);

        MALManager mManager = new MALManager(context);
        String account = AccountService.getUsername();
        mManager.cleanDirtyAnimeRecords(account);
        mManager.downloadAndStoreAnimeList(account);
        mManager.cleanDirtyMangaRecords(account);
        mManager.downloadAndStoreMangaList(account);

        nm.cancel(R.id.notification_sync);
    }
}