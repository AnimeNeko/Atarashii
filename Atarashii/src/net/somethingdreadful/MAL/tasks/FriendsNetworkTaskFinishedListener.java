package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.Friend;

public interface FriendsNetworkTaskFinishedListener {
    public void FriendsNetworkTaskFinished(ArrayList<Friend> result);
}
