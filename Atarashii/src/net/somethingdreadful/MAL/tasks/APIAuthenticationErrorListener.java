package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.api.MALApi;

public interface APIAuthenticationErrorListener {
    public void onAPIAuthenticationError(MALApi.ListType type, TaskJob job);
}
