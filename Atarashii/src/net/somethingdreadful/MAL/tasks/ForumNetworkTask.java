package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.response.ForumMain;

public class ForumNetworkTask extends AsyncTask<String, Void, ForumMain> {
    Context context;
    ForumNetworkTaskFinishedListener callback;
    TaskJob type;
    int id;

    public ForumNetworkTask(Context context, ForumNetworkTaskFinishedListener callback, TaskJob type, int id) {
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
        }
        return result;
    }

    @Override
    protected void onPostExecute(ForumMain result) {
        if (callback != null)
            callback.onForumNetworkTaskFinished(result);
    }
}
