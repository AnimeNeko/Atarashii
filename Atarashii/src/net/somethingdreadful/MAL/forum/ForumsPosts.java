package net.somethingdreadful.MAL.forum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ViewFlipper;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;

public class ForumsPosts extends Fragment implements ForumNetworkTaskFinishedListener {
    public int id;
    public ForumMain record;
    ForumActivity activity;
    HtmlUtil htmlUtil;
    View view;
    WebView webview;
    public int page = 0;
    ViewFlipper viewFlipper;

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.fragment_forum_posts, container, false);
        webview = (WebView) view.findViewById(R.id.webview);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        htmlUtil = new HtmlUtil(activity);

        if (bundle != null && bundle.getSerializable("posts") != null) {
            id = bundle.getInt("id");
            page = bundle.getInt("page");
            apply((ForumMain) bundle.getSerializable("posts"));
        }

        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new PostsInterface(this), "Posts");

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("posts", record);
        state.putInt("id", id);
        state.putInt("page", page);
        super.onSaveInstanceState(state);
    }

    /**
     * Change the records in this fragment.
     *
     * @param id The new id of the record
     * @return ForumJob The task of this fragment
     */
    public ForumJob setId(int id) {
        if (this.id != id) {
            this.id = id;
            toggle(true);
            getRecords(1);
        }
        return ForumJob.POSTS;
    }

    /**
     * Get the requested records.
     *
     * @param page The page number
     */
    public void getRecords(int page) {
        if (page != this.page)
            toggle(true);
        this.page = page;
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, ForumJob.POSTS, id).execute(Integer.toString(page));
    }

    /**
     * Show or hide the progress indicator.
     *
     * @param loading True if the indicator should be shown
     */
    private void toggle(boolean loading) {
        viewFlipper.setDisplayedChild(loading ? 1 : 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((ForumActivity) activity);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob job) {
            apply(result);
    }

    /**
     * Refresh the UI for changes.
     *
     * @param result The new record
     */
    public void apply(ForumMain result) {
        activity.setTitle(getString(R.string.title_activity_forum));
        webview.loadDataWithBaseURL(null, htmlUtil.convertList(result, activity, AccountService.getUsername(activity), page), "text/html", "utf-8", null);
        toggle(false);
        record = result;
    }
}