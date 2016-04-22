package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
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
        ContentManager mManager = new ContentManager(activity);
        if (!AccountService.isMAL() && APIHelper.isNetworkAvailable(activity))
            mManager.verifyAuthentication();

        try {
            switch (type) {
                case MENU: // list with all categories
                    result = mManager.getForumCategories();
                    break;
                case CATEGORY: // list with all topics of a certain category
                    result = mManager.getCategoryTopics(id, Integer.parseInt(params[0]));
                    break;
                case SUBCATEGORY:
                    result = mManager.getSubCategory(id, Integer.parseInt(params[0]));
                    break;
                case TOPIC: // list with all comments of users
                    result = mManager.getTopic(id, Integer.parseInt(params[0]));
                    break;
                case SEARCH:
                    result = mManager.search(params[0]);
                    break;
                case ADDCOMMENT:
                    result = mManager.addComment(id, params[0]) ? new ArrayList<Forum>() : null;
                    if (result != null)
                        result = mManager.getTopic(id, Integer.parseInt(params[1]));
                    break;
                case UPDATECOMMENT:
                    result = mManager.updateComment(id, params[0]) ? new ArrayList<Forum>() : null;
                    break;
                /*
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
                    break;*/
            }
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "doInBackground(6): " + String.format("%s-task unknown API error on id %s", type.toString(), id), e);
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
