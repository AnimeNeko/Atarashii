package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.api.response.UserProfile.User;

import java.util.ArrayList;

public interface FriendsNetworkTaskFinishedListener {
    void onFriendsNetworkTaskFinished(ArrayList<User> result);
}
