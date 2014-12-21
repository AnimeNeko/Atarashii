package net.somethingdreadful.MAL.forum;

import android.webkit.JavascriptInterface;

import net.somethingdreadful.MAL.account.AccountService;

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
    public void clicked(final String id, String position) {
        String username = posts.record.getList().get(Integer.parseInt(position)).getUsername();
        final String comment = posts.record.getList().get(Integer.parseInt(position)).getComment();

        if (username.equals(AccountService.getUsername(posts.activity)))
            posts.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    posts.activity.getComments(Integer.parseInt(id), comment);
                }
            });
    }
}