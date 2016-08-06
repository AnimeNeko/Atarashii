package net.somethingdreadful.MAL.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.somethingdreadful.MAL.api.MALApi;

public class RecordStatusUpdatedReceiver extends BroadcastReceiver {
    public static final String RECV_IDENT = "net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver";
    private final RecordStatusUpdatedListener callback;

    public RecordStatusUpdatedReceiver(RecordStatusUpdatedListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(RECV_IDENT)) {
            if (callback != null) {
                MALApi.ListType type = (MALApi.ListType) intent.getSerializableExtra("type");
                callback.onRecordStatusUpdated(type);
            }
        }
    }

    public interface RecordStatusUpdatedListener {
        void onRecordStatusUpdated(MALApi.ListType type);
    }
}
