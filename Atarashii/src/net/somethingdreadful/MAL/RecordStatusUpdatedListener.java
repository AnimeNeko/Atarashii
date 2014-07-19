package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.api.MALApi;

public interface RecordStatusUpdatedListener {
    public void onRecordStatusUpdated(MALApi.ListType type);
}
