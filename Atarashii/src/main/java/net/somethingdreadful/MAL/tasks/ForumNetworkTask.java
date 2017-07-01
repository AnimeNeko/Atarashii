package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Forum;

import java.util.ArrayList;

public class ForumNetworkTask extends AsyncTask<String, Void, ArrayList<Forum>> {
    private final ForumNetworkTaskListener callback;
    private final ForumJob type;
    private final int id;
    private final Activity activity;

    public ForumNetworkTask(ForumNetworkTaskListener callback, Activity activity, ForumJob type, int id) {
        this.callback = callback;
        this.type = type;
        this.id = id;
        this.activity = activity;
    }

    @Override
    protected ArrayList<Forum> doInBackground(String... params) {
        ArrayList<Forum> result = new ArrayList<>();
        ContentManager cManager = new ContentManager(activity);
        boolean error = false;
        if (APIHelper.isNetworkAvailable(activity))
            error = cManager.verifyAuthentication();

        try {
            switch (type) {
                case MENU: // list with all categories
                    result = cManager.getForumCategories();
                    break;
                case CATEGORY: // list with all topics of a certain category
                    result = cManager.getCategoryTopics(id, Integer.parseInt(params[0]));
                    break;
                case SUBCATEGORY:
                    result = cManager.getSubCategory(id, Integer.parseInt(params[0]));
                    break;
                case TOPIC: // list with all comments of users
                    result = cManager.getTopic(id, Integer.parseInt(params[0]));
                    break;
                case SEARCH:
                    result = cManager.search(params[0]);
                    break;
                case ADDCOMMENT:
                    if (!error) {
                        result = cManager.addComment(id, params[0]) ? new ArrayList<Forum>() : null;
                        if (result != null)
                            result = cManager.getTopic(id, Integer.parseInt(params[1]));
                    }
                    break;
                case UPDATECOMMENT:
                    if (!error) {
                        result = cManager.updateComment(id, params[0]) ? new ArrayList<Forum>() : null;
                    }
                    break;
            }
        } catch (Exception e) {
            AppLog.logTaskCrash("ForumNetworkTask", "doInBackground(6): " + String.format("%s-task unknown API error on id %s", type.toString(), id), e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Forum> result) {
        if (callback != null)
            callback.onForumNetworkTaskFinished(result, type);
    }

    public interface ForumNetworkTaskListener {
        void onForumNetworkTaskFinished(ArrayList<Forum> result, ForumJob task);
    }
}
