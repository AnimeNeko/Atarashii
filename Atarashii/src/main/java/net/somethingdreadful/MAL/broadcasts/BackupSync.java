package net.somethingdreadful.MAL.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.tasks.BackupTask;

public class BackupSync extends BroadcastReceiver implements BackupTask.BackupTaskListener {

    @Override
    public void onReceive(Context context, Intent intent) {
        new BackupTask(this, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onBackupTaskFinished() {

    }

    @Override
    public void onBackupTaskFailed() {

    }
}