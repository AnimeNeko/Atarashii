package net.somethingdreadful.MAL.forum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;

public class ForumsPosts extends Fragment implements ForumNetworkTaskFinishedListener, View.OnClickListener {
    ForumActivity activity;
    View view;
    WebView webview;
    public int id;
    int page = 0;
    ViewFlipper viewFlipper;
    public ForumMain record;
    RelativeLayout comment;
    ImageView send;
    EditText input;

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.fragment_forum_posts, container, false);
        webview = (WebView) view.findViewById(R.id.webview);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        comment = (RelativeLayout) view.findViewById(R.id.commentbar);
        send = (ImageView) view.findViewById(R.id.commentImage);
        input = (EditText) view.findViewById(R.id.editText);

        if (bundle != null && bundle.getSerializable("posts") != null) {
            apply((ForumMain) bundle.getSerializable("posts"));
            id = bundle.getInt("id");
        }

        send.setOnClickListener(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new PostsInterface(this), "Posts");

        return view;
    }

    public void getComments(int id, String comment) {
        activity.getComments(id, comment);
    }

    public void toggleComments(){
        comment.setVisibility(!(comment.getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("posts", record);
        state.putInt("id", id);
        super.onSaveInstanceState(state);
    }

    public ForumJob setId(int id) {
        if (this.id != id) {
            this.id = id;
            toggle(true);
            getRecords(1);
        }
        return ForumJob.POSTS;
    }

    private void getRecords(int page) {
        this.page = page;
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, ForumJob.POSTS, id).execute(Integer.toString(page));
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
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob job) {
        if (job == ForumJob.POSTS) {
            apply(result);
            send.setEnabled(true);
            input.setEnabled(true);
        } else {
            Toast.makeText(activity.getApplicationContext(), R.string.toast_info_comment_add, Toast.LENGTH_SHORT).show();
            input.setText("");
            getRecords(page);
        }
    }

    public void apply(ForumMain result) {
        activity.setTitle(getString(R.string.title_activity_forum));
        webview.loadDataWithBaseURL(null, HtmlList.convertList(result.getList(), activity), "text/html", "utf-8", null);
        toggle(false);
        record = result;
    }

    @Override
    public void onClick(View v) {
        input.clearFocus();
        toggleComments();
        send.setEnabled(false);
        input.setEnabled(false);
        new ForumNetworkTask(activity, this, ForumJob.ADDCOMMENT, id).execute(input.getText().toString());
    }
}