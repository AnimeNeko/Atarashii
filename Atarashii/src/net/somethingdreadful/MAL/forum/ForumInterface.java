package net.somethingdreadful.MAL.forum;

import android.webkit.JavascriptInterface;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.tasks.ForumJob;

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
     * Get more pages certain comments.
     */
    @JavascriptInterface
    public void commentList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page);
            }
        });
    }
}