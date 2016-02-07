package net.somethingdreadful.MAL.forum;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;

public class ForumInterface {
    ForumActivity forum;

    public ForumInterface(ForumActivity forum) {
        this.forum = forum;
    }

    /**
     * Get the topics from a certain category.
     */
    @JavascriptInterface
    public void tileClick(final String id) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(id), "1");
            }
        });
    }

    /**
     * Get more pages certain category.
     */
    @JavascriptInterface
    public void topicList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Get the topics from a certain category.
     */
    @JavascriptInterface
    public void subTileClick(final String id) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum.getRecords(ForumJob.SUBCATEGORY, Integer.parseInt(id), "1");
            }
        });
    }

    /**
     * Get the posts from a certain topic.
     */
    @JavascriptInterface
    public void topicClick(final String id) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(id), "1");
            }
        });
    }

    /**
     * Get next topic page.
     */
    @JavascriptInterface
    public void nextTopicList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Get topic comment page.
     */
    @JavascriptInterface
    public void prevTopicList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Send a comment.
     */
    @JavascriptInterface
    public void sendComment(final String comment) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!comment.equals("") && comment.length() > 16) {
                    forum.setLoading(true);
                    String[] details = forum.webview.getTitle().split(" ");
                    new ForumNetworkTask(forum, forum, ForumJob.ADDCOMMENT, Integer.parseInt(details[1])).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment, details[3]);
                } else {
                    Theme.Snackbar(forum, R.string.toast_info_comment);
                }
            }
        });
    }

    /**
     * Get next comment page.
     */
    @JavascriptInterface
    public void nextCommentList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Get comment page.
     */
    @JavascriptInterface
    public void pagePicker(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                Bundle bundle = new Bundle();
                bundle.putInt("id", Integer.parseInt(details[1]));
                bundle.putString("title", forum.getString(R.string.Page_number));
                bundle.putInt("current", Integer.parseInt(page));
                bundle.putInt("max", Integer.parseInt(details[2]));
                bundle.putInt("min", 1);
                FragmentManager fm = forum.getFragmentManager();
                NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment().setOnSendClickListener(forum);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fm, "fragment_page");
            }
        });
    }

    /**
     * Get previous comment page.
     */
    @JavascriptInterface
    public void prevCommentList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Open the userprofile.
     */
    @JavascriptInterface
    public void profileClick(final String username) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent Profile = new Intent(forum, ProfileActivity.class);
                Profile.putExtra("username", username);
                forum.startActivity(Profile);
            }
        });
    }
}