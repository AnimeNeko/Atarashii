package net.somethingdreadful.MAL.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {
 
    private static final Object SALock = new Object();
    private static SyncAdapter SA = null;
 
    @Override
    public void onCreate() {
        synchronized (SALock) {
            if (SA == null)
                SA = new SyncAdapter(getApplicationContext(), true);
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return SA.getSyncAdapterBinder();
	}
}