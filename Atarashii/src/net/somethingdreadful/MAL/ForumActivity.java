package net.somethingdreadful.MAL;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ViewFlipper;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.forum.ForumsComment;
import net.somethingdreadful.MAL.forum.ForumsMain;
import net.somethingdreadful.MAL.forum.ForumsPosts;
import net.somethingdreadful.MAL.forum.ForumsTopics;
import net.somethingdreadful.MAL.tasks.ForumJob;

public class ForumActivity extends ActionBarActivity {

    public ForumsMain main;
    public ForumsTopics topics;
    public ForumsPosts posts;
    public ForumsComment comments;
    public boolean discussion = false;
    FragmentManager manager;
    ViewFlipper viewFlipper;
    public ForumJob task;
    public ForumJob prevTask;
    Menu menu;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_forum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        manager = getFragmentManager();
        main = (ForumsMain) manager.findFragmentById(R.id.main);
        topics = (ForumsTopics) manager.findFragmentById(R.id.topics);
        posts = (ForumsPosts) manager.findFragmentById(R.id.posts);
        comments = (ForumsComment) manager.findFragmentById(R.id.comment);

        if (bundle != null) {
            viewFlipper.setDisplayedChild(bundle.getInt("child"));
            task = (ForumJob) bundle.getSerializable("task");
            discussion = bundle.getBoolean("discussion");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("child", viewFlipper.getDisplayedChild());
        state.putSerializable("task", task);
        state.putBoolean("discussion", discussion);
        super.onSaveInstanceState(state);
    }

    /**
     * Switch the view to the topics fragment.
     *
     * @param id The board id
     */
    public void getTopics(int id) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(id, ForumJob.TOPICS));
    }

    /**
     * Switch the view to the topics fragment to show subBoards.
     *
     * @param id The subBoard id
     */
    public void getSubBoard(int id) {
        viewFlipper.setDisplayedChild(1);
        topics.type = ((id == 1 || id == 2) ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
        setTask(topics.setId(id, ForumJob.SUBBOARD));
    }

    /**
     * Switch the view to the topics posts.
     *
     * @param id The id of the topic
     */
    public void getPosts(int id) {
        viewFlipper.setDisplayedChild(2);
        setTask(posts.setId(id));
    }

    /**
     * Switch the view to the comment editor.
     *
     * @param id The comment id
     * @param comment The comment text
     * @param task The task to peform
     */
    public void getComments(int id, String comment, ForumJob task) {
        viewFlipper.setDisplayedChild(3);
        prevTask = this.task;
        setTask(comments.setId(id, comment, task));
    }

    /**
     * Switch the view to the discussion view.
     *
     * @param id The comment id
     */
    public void getDiscussion(int id) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(id, ForumJob.DISCUSSION));
        discussion = true;
    }

    /**
     * Handle the back and home buttons.
     */
    public void back() {
        switch (task) {
            case BOARD:
                finish();
                break;
            case SUBBOARD:
                setTask(ForumJob.BOARD);
                viewFlipper.setDisplayedChild(0);
                break;
            case DISCUSSION:
                setTask(ForumJob.SUBBOARD);
                topics.task = ForumJob.SUBBOARD;
                topics.topicsAdapter.clear();
                topics.apply(topics.subBoard);
                discussion = false;
                break;
            case TOPICS:
                setTask(ForumJob.BOARD);
                viewFlipper.setDisplayedChild(0);
                break;
            case POSTS:
                if (discussion) {
                    setTask(ForumJob.DISCUSSION);
                } else
                    setTask(ForumJob.TOPICS);
                viewFlipper.setDisplayedChild(1);
                break;
            case COMMENT:
                if (prevTask == ForumJob.POSTS)
                    viewFlipper.setDisplayedChild(2);
                else
                    viewFlipper.setDisplayedChild(1);
                setTask(prevTask);
                break;
        }
    }

    /**
     * Force android to hide the keyboard
     *
     * @param text The EditText view
     */
    public void hideKeyboard(EditText text) {
        text.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    /**
     * Change the task & change the menu items.
     *
     * @param task The new ForumTask
     */
    public void setTask(ForumJob task) {
        this.task = task;
        menu.findItem(R.id.action_add).setVisible((task == ForumJob.POSTS || task == ForumJob.TOPICS) && getTopicStatus() && viewFlipper.getDisplayedChild() != 3);
        menu.findItem(R.id.action_send).setVisible(viewFlipper.getDisplayedChild() == 3);
        menu.findItem(R.id.action_ViewMALPage).setVisible(viewFlipper.getDisplayedChild() != 3);
    }

    /**
     * Checks if the ID allows to add a topics
     *
     * @return boolean If true then the ID allows to add comments
     */
    public boolean getTopicStatus() {
        return task != ForumJob.TOPICS || (topics.id != 5 && topics.id != 14 && topics.id != 15 && topics.id != 17);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forum, menu);
        this.menu = menu;
        setTask(ForumJob.BOARD);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
            case R.id.action_ViewMALPage:
                startActivity(new Intent(Intent.ACTION_VIEW, getUri()));
                break;
            case R.id.action_add:
                if (task == ForumJob.POSTS)
                    posts.toggleComments();
                else if (task == ForumJob.TOPICS)
                    getComments(topics.id, null, ForumJob.ADDTOPIC);
                break;
            case R.id.action_send:
                comments.send();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the Uri depending on the ForumTask.
     *
     * @return Uri The uri of the desired URL to launch
     */
    public Uri getUri() {
        Uri url = Uri.EMPTY;
        switch (task) {
            case BOARD:
                url = Uri.parse("http://myanimelist.net/forum/");
                break;
            case SUBBOARD:
                url = Uri.parse("http://myanimelist.net/forum/?subboard=" + topics.id);
                break;
            case DISCUSSION:
                url = Uri.parse("http://myanimelist.net/forum/?" + (topics.type == MALApi.ListType.ANIME ? "anime" : "manga") + "id=" + topics.id);
                break;
            case TOPICS:
                url = Uri.parse("http://myanimelist.net/forum/?board=" + topics.id);
                break;
            case POSTS:
                url = Uri.parse("http://myanimelist.net/forum/?topicid=" + posts.id);
                break;
        }
        return url;
    }
}
