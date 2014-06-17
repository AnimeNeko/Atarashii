package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.api.MALApi;

import java.util.ArrayList;

public interface NetworkTaskCallbackListener {
    public void onNetworkTaskFinished(Object result, TaskJob job, int page, MALApi.ListType type);
}
