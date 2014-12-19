package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;

import java.util.ArrayList;

public class ForumNetworkTask extends AsyncTask<String, Void, ForumMain> {
    Context context;
    ForumNetworkTaskFinishedListener callback;
    ForumJob type;
    int id;

    public ForumNetworkTask(Context context, ForumNetworkTaskFinishedListener callback, ForumJob type, int id) {
        this.context = context;
        this.callback = callback;
        this.type = type;
        this.id = id;
    }

    @Override
    protected ForumMain doInBackground(String... params) {
        ForumMain result = new ForumMain();
        MALManager mManager = new MALManager(context);
        switch (type) {
            case BOARD:
                result = mManager.getForum();
                break;
            case TOPICS:
                result.setList(mManager.getTopics(id, Integer.parseInt(params[0])));
                break;
            case POSTS:
                result.setList(mManager.getPosts(id, Integer.parseInt(params[0])));
                break;
            case SUBBOARD:
                result.setList(mManager.getSubBoards(id, Integer.parseInt(params[0])));
                break;
            case ADDCOMMENT:
                result.setList(mManager.addComment(id, params[0]) ? new ArrayList<Forum>() : null);
            case UPDATECOMMENT:
                result.setList(mManager.updateComment(id, params[0]) ? new ArrayList<Forum>() : null);
                break;
        }
        return result;
    }

    @Override
    protected void onPostExecute(ForumMain result) {
        if (callback != null)
            callback.onForumNetworkTaskFinished(result, type);
    }
}
