package net.somethingdreadful.MAL.broadcasts;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.Home;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.util.ArrayList;

public class AutoSync extends BroadcastReceiver implements APIAuthenticationErrorListener, NetworkTask.NetworkTaskListener {
    private static NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            Crashlytics.log(Log.ERROR, "MALX", "AutoSync.onReceive(): context is null");
            return;
        }
        PrefManager.create(context);
        AccountService.create(context);
        if (MALApi.isNetworkAvailable(context) && AccountService.getAccount() != null) {
            Intent notificationIntent = new Intent(context, Home.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (networkChange(intent) && !PrefManager.getAutosyncDone() || !networkChange(intent)) {
                ArrayList<String> args = new ArrayList<>();
                args.add(AccountService.getUsername());
                args.add("");
                new NetworkTask(TaskJob.FORCESYNC, MALApi.ListType.ANIME, context, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));
                new NetworkTask(TaskJob.FORCESYNC, MALApi.ListType.MANGA, context, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));

                nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder syncNotificationBuilder = new Notification.Builder(context).setOngoing(true)
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.toast_info_SyncMessage));
                Notification syncNotification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        syncNotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                    syncNotification = syncNotificationBuilder.build();
                } else {
                    syncNotification = syncNotificationBuilder.getNotification();
                }
                nm.notify(R.id.notification_sync, syncNotification);
            }
        } else if (!networkChange(intent)) {
            PrefManager.setAutosyncDone(false);
        }
    }

    @Override
    public void onAPIAuthenticationError(MALApi.ListType type, TaskJob job) {
        nm.cancel(R.id.notification_sync);
        PrefManager.setAutosyncDone(false);
    }

    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled) {
        nm.cancel(R.id.notification_sync);
        PrefManager.setAutosyncDone(true);
    }

    @Override
    public void onNetworkTaskError(TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled) {
        nm.cancel(R.id.notification_sync);
        PrefManager.setAutosyncDone(false);
    }

    private boolean networkChange(Intent intent) {
        return intent != null && intent.getAction() != null && intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
    }
}