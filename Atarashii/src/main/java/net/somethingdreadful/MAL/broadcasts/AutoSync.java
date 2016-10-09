package net.somethingdreadful.MAL.broadcasts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.Home;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.util.ArrayList;

public class AutoSync extends BroadcastReceiver implements NetworkTask.NetworkTaskListener {
    private static NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            AppLog.log(Log.ERROR, "Atarashii", "AutoSync.onReceive(): context is null");
            return;
        }
        PrefManager.create(context);
        AccountService.create(context);
        if (APIHelper.isNetworkAvailable(context) && AccountService.getAccount() != null) {
            Intent notificationIntent = new Intent(context, Home.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (networkChange(intent) && !PrefManager.getAutosyncDone() || !networkChange(intent)) {
                ArrayList<String> args = new ArrayList<>();
                args.add(ContentManager.listSortFromInt(0, MALApi.ListType.ANIME));
                args.add(String.valueOf(1));
                args.add(String.valueOf(false));
                new NetworkTask(MALApi.ListType.ANIME, context, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));
                new NetworkTask(MALApi.ListType.MANGA, context, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.toast_info_SyncMessage))
                        .setContentIntent(contentIntent);

                nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(R.id.notification_sync, mBuilder.build());
            }
        } else if (!networkChange(intent)) {
            PrefManager.setAutosyncDone(false);
        }
    }

    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, MALApi.ListType type) {
        nm.cancel(R.id.notification_sync);
        PrefManager.setAutosyncDone(true);
    }

    @Override
    public void onNetworkTaskError(TaskJob job) {
        nm.cancel(R.id.notification_sync);
        PrefManager.setAutosyncDone(false);
    }

    private boolean networkChange(Intent intent) {
        return intent != null && intent.getAction() != null && intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
    }
}