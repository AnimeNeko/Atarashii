package net.somethingdreadful.MAL.tasks;


import net.somethingdreadful.MAL.api.response.UserProfile.User;

public interface UserNetworkTaskFinishedListener {
    void onUserNetworkTaskFinished(User result);
}
