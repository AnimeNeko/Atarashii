package net.somethingdreadful.MAL.account;

import net.somethingdreadful.MAL.Home;
import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.R;
import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

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
    	Notification syncNotification = new NotificationCompat.Builder(context).setOngoing(true)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.crouton_info_SyncMessage)).build();
    	nm.notify(R.id.notification_sync, syncNotification);
		
		MALManager mManager = new MALManager(context);
		mManager.cleanDirtyAnimeRecords();
		mManager.downloadAndStoreAnimeList();
	    mManager.cleanDirtyMangaRecords();
		mManager.downloadAndStoreMangaList();
		
        nm.cancel(R.id.notification_sync);
	}
}