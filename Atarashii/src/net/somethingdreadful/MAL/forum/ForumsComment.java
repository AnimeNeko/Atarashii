package net.somethingdreadful.MAL.forum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;

public class ForumsComment extends Fragment implements ForumNetworkTaskFinishedListener {
    ForumActivity activity;
    View view;
    public int id;
    boolean update = false;
    String message;
    RelativeLayout comment;
    ImageView send;
    EditText input;

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.fragment_forum_comments, container, false);
        comment = (RelativeLayout) view.findViewById(R.id.commentbar);
        send = (ImageView) view.findViewById(R.id.commentImage);
        input = (EditText) view.findViewById(R.id.editText);

        if (bundle != null && bundle.getString("message") != null) {
            message = bundle.getString("message");
            id = bundle.getInt("id");
        }

        return view;
    }

    public void send() {
        new ForumNetworkTask(activity, this, update ? ForumJob.UPDATECOMMENT : ForumJob.ADDCOMMENT, id).execute(input.getText().toString());
        input.setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("message", message);
        state.putInt("id", id);
        super.onSaveInstanceState(state);
    }

    public ForumJob setId(int id, String message) {
        if (this.id != id) {
            this.id = id;
            if (message != null) {
                update = true;
                this.message = HtmlList.convertComment(message);
                input.setText(this.message);
            }
        }
        return ForumJob.POSTS;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((ForumActivity) activity);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob job) {
        activity.getPosts(activity.posts.id);
        input.setEnabled(true);
    }
}