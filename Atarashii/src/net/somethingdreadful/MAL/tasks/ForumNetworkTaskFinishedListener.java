package net.somethingdreadful.MAL.tasks;

import net.somethingdreadful.MAL.api.response.ForumMain;

public interface ForumNetworkTaskFinishedListener {
    public void onForumNetworkTaskFinished(ForumMain result);
}
