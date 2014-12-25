package net.somethingdreadful.MAL.forum;

import android.content.Intent;
import android.webkit.JavascriptInterface;

import net.somethingdreadful.MAL.tasks.ForumJob;

public class PostsInterface {
    ForumsPosts posts;

    PostsInterface(ForumsPosts posts) {
        this.posts = posts;
    }

    /**
     * This method will be triggered when the user clicks on an HTML post.
     *
     * @param id       The HTML post id
     * @param position The array position of the post
     */
    @JavascriptInterface
    public void edit(final String id, String position) {
        final String comment = posts.record.getList().get(Integer.parseInt(position)).getComment();

        posts.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posts.activity.getComments(Integer.parseInt(id), comment, ForumJob.UPDATECOMMENT);
            }
        });
    }

    /**
     * This method will be triggered when the user clicks on a profile image.
     *
     * @param position The array position of the post
     */
    @JavascriptInterface
    public void viewProfile(String position) {
        String username = posts.record.getList().get(Integer.parseInt(position)).getUsername();
        Intent profile = new Intent(posts.activity, net.somethingdreadful.MAL.ProfileActivity.class);
        profile.putExtra("username", username);
        posts.startActivity(profile);
    }
}