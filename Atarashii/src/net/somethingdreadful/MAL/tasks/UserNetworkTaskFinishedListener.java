package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.api.response.User;

public interface UserNetworkTaskFinishedListener {
    public void onUserNetworkTaskFinished(User result);
}
