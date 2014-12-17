package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ViewFlipper;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

public class ForumsPosts extends Fragment implements ForumNetworkTaskFinishedListener {
    ForumActivity activity;
    View view;
    WebView webview;
    int id;
    int page = 0;
    ViewFlipper viewFlipper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_forum_posts, container, false);
        webview = (WebView) view.findViewById(R.id.webview);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);

        return view;
    }

    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            toggle(true);
            getRecords(1);
        }
    }

    private void getRecords(int page) {
        this.page = page;
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, TaskJob.POSTS, id).execute(Integer.toString(page));
    }

    private void toggle(boolean loading){
        viewFlipper.setDisplayedChild(loading ? 1 : 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((ForumActivity) activity);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result) {
        activity.setTitle(getString(R.string.title_activity_forum));
        webview.loadDataWithBaseURL(null, HtmlList.HtmlList(result.getList(), activity), "text/html", "utf-8", null);
        toggle(false);
    }
}