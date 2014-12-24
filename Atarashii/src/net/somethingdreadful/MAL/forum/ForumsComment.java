package net.somethingdreadful.MAL.forum;

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
    String message;
    RelativeLayout comment;
    ImageView send;
    EditText input;
    EditText title;
    ForumJob task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.fragment_forum_comments, container, false);
        comment = (RelativeLayout) view.findViewById(R.id.commentbar);
        send = (ImageView) view.findViewById(R.id.commentImage);
        input = (EditText) view.findViewById(R.id.message);
        title = (EditText) view.findViewById(R.id.editText);

        if (bundle != null && bundle.getString("message") != null) {
            message = bundle.getString("message");
            id = bundle.getInt("id");
            task = (ForumJob) bundle.getSerializable("task");
        }

        return view;
    }

    /**
     * Sends the message.
     */
    public void send() {
        input.clearFocus();
        title.clearFocus();
        if (task == ForumJob.ADDTOPIC && !input.getText().toString().equals("") && !title.getText().toString().equals(""))
            new ForumNetworkTask(activity, this, task, id).execute(title.getText().toString(), input.getText().toString());
        else if (!input.getText().toString().equals(""))
            new ForumNetworkTask(activity, this, task, id).execute(input.getText().toString());
        input.setEnabled(false);
        title.setEnabled(false);
        activity.hideKeyboard(input);
        activity.hideKeyboard(title);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putString("message", message);
        state.putInt("id", id);
        state.putSerializable("task", task);
        super.onSaveInstanceState(state);
    }

    /**
     * Changes the ID and applies the provided message.
     *
     * @param id The post id
     * @param message The message that should be modified
     * @param task The task that should be performed
     * @return ForumJob The task that will be used to send the message
     */
    public ForumJob setId(int id, String message, ForumJob task) {
        if (this.id != id) {
            this.id = id;
            this.task = task;

            if (task == ForumJob.ADDTOPIC)
                title.setVisibility(View.VISIBLE);
            else
                title.setVisibility(View.GONE);

            if (message != null) {
                this.message = HtmlList.convertComment(message);
                input.setText(this.message);
            }
        }
        return ForumJob.COMMENT;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((ForumActivity) activity);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob job) {
        activity.back();
        input.setEnabled(true);
        title.setEnabled(true);
    }
}