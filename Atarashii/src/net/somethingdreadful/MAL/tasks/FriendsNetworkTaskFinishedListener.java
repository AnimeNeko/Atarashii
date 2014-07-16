package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.api.response.User;

import java.util.ArrayList;

public interface FriendsNetworkTaskFinishedListener {
    public void onFriendsNetworkTaskFinished(ArrayList<User> result);
}
