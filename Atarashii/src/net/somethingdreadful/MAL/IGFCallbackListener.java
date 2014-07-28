package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.TaskJob;

public interface IGFCallbackListener {
    public void onIGFReady(IGF igf);
    public void onRecordsLoadingFinished(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled);
}
