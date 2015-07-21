package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.MALApi;
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
            case SUBBOARD:
                result = mManager.getSubBoards(id, Integer.parseInt(params[0]));
                break;
            case DISCUSSION:
                if (params[1].equals(MALApi.ListType.ANIME.toString()))
                    result = mManager.getDiscussion(id, Integer.parseInt(params[0]), MALApi.ListType.ANIME);
                else
                    result = mManager.getDiscussion(id, Integer.parseInt(params[0]), MALApi.ListType.MANGA);
                break;
            case TOPICS:
                result = mManager.getTopics(id, Integer.parseInt(params[0]));
                break;
            case POSTS:
                result = mManager.getPosts(id, Integer.parseInt(params[0]));
                break;
            case ADDTOPIC:
                result.setList(mManager.addTopic(id, params[0], params[1]) ? new ArrayList<Forum>() : null);
                break;
            case ADDCOMMENT:
                result.setList(mManager.addComment(id, params[0]) ? new ArrayList<Forum>() : null);
                break;
            case UPDATECOMMENT:
                result.setList(mManager.updateComment(id, params[0]) ? new ArrayList<Forum>() : null);
                break;
            case SEARCH:
                result = mManager.search(params[0]);
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
