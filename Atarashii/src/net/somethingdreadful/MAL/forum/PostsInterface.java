package net.somethingdreadful.MAL.forum;

import android.webkit.JavascriptInterface;

public class PostsInterface {
    ForumsPosts activity;

    PostsInterface(ForumsPosts activity) {
        this.activity = activity;
    }

    /**
     * This method will be triggered when the user clicks on an HTML post.
     *
     * @param id The HTML post id
     * @param position The array position of the post
     */
    @JavascriptInterface
    public void clicked(final String id, String position) {
        final String comment = activity.record.getList().get(Integer.parseInt(position)).getComment();
        activity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.activity.getComments(Integer.parseInt(id), comment);
            }
        });
    }
}
