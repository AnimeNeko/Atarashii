package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.User;

public interface FriendsNetworkTaskFinishedListener {
    public void onFriendsNetworkTaskFinished(ArrayList<User> result);
}
