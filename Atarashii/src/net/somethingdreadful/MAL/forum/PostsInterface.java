package net.somethingdreadful.MAL.forum;

import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import net.somethingdreadful.MAL.R;
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
                if (comment.contains("embed src"))
                    Toast.makeText(posts.activity, posts.activity.getString(R.string.toast_info_disabled_youtube), Toast.LENGTH_LONG).show();
                else
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

    /**
     * Go the the previous page.
     */
    @JavascriptInterface
    public void previous() {
        posts.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posts.activity.posts.getRecords(posts.activity.posts.page - 1);
            }
        });
    }

    /**
     * Go to the next page.
     */
    @JavascriptInterface
    public void next() {
        posts.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posts.activity.posts.getRecords(posts.activity.posts.page + 1);
            }
        });
    }

    /**
     * Go to the first page.
     */
    @JavascriptInterface
    public void first() {
        posts.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posts.activity.posts.getRecords(1);
            }
        });
    }

    /**
     * Go to the last page.
     */
    @JavascriptInterface
    public void last() {
        posts.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posts.activity.posts.getRecords(posts.activity.posts.record.getPages());
            }
        });
    }
}