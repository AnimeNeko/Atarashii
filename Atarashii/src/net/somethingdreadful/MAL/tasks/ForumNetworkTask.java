package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Forum;

import java.util.ArrayList;

import retrofit.RetrofitError;

public class ForumNetworkTask extends AsyncTask<String, Void, ArrayList<Forum>> {
    ForumNetworkTaskListener callback;
    ForumJob type;
    int id;
    Activity activity;

    public ForumNetworkTask(ForumNetworkTaskListener callback, Activity activity, ForumJob type, int id) {
        this.callback = callback;
        this.type = type;
        this.id = id;
        this.activity = activity;
    }

    @Override
    protected ArrayList<Forum> doInBackground(String... params) {
        ArrayList<Forum> result = new ArrayList<>();
        MALManager mManager = new MALManager(activity);

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
        } catch (RetrofitError re) {
            if (re.getResponse() != null && activity != null) {
                switch (re.getResponse().getStatus()) {
                    case 400: // Bad Request
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 401: // Unauthorized
                        Crashlytics.log(Log.ERROR, "MALX", "ForumNetworkTask.doInBackground(1): User is not logged in");
                        Theme.Snackbar(activity, R.string.toast_info_password);
                        break;
                    case 404: // Not Found
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        break;
                    case 500: // Internal Server Error
                        Crashlytics.log(Log.ERROR, "MALX", "ForumNetworkTask.doInBackground(2): Internal server error, API bug?");
                        Crashlytics.logException(re);
                        Theme.Snackbar(activity, R.string.toast_error_api);
                        break;
                    case 503: // Service Unavailable
                    case 504: // Gateway Timeout
                        Crashlytics.log(Log.ERROR, "MALX", "ForumNetworkTask.doInBackground(3): " + String.format("%s-task unknown API error on id %s: %s", type.toString(), id, re.getMessage()));
                        Theme.Snackbar(activity, R.string.toast_error_maintenance);
                        break;
                    default:
                        Theme.Snackbar(activity, R.string.toast_error_Records);
                        break;
                }
                Crashlytics.log(Log.ERROR, "MALX", "ForumNetworkTask.doInBackground(4): " + String.format("%s-task unknown API error on id %s: %s", type.toString(), id, re.getMessage()));
            } else {
                Crashlytics.log(Log.ERROR, "MALX", "ForumNetworkTask.doInBackground(5): " + String.format("%s-task unknown API error on id %s: %s", type.toString(), id, re.getMessage()));
                Theme.Snackbar(activity, R.string.toast_error_maintenance);
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
