package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Forum;
import net.somethingdreadful.MAL.forum.ForumInterface;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public class ForumActivity extends AppCompatActivity implements ForumNetworkTask.ForumNetworkTaskListener {
    @Bind(R.id.webview)
    WebView webview;
    @Bind(R.id.progress1)
    ProgressBar progress;
    testforumhtmlunit test;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Theme.setTheme(this, R.layout.activity_forum, false);
        Theme.setActionBar(this);
        ButterKnife.bind(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new ForumInterface(this), "Forum");

        test = new testforumhtmlunit(this);
        if (bundle != null) {
            test.setForumMenuLayout(bundle.getString("forumMenuLayout"));
        }
        getRecords(ForumJob.MENU, 0);
    }

    public void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putString("forumMenuLayout", test.getForumMenuLayout());
        super.onSaveInstanceState(state);
    }

    public void getRecords(ForumJob job, int id) {
        setLoading(true);
        switch (job) {
            case MENU:
                if (!test.menuExists())
                    new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    test.setForumMenu(null);
                break;
            case CATEGORY:
                new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(1));
                break;
            case TOPIC:
                new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(1));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack())
            webview.goBack();
        else
            finish();
    }

    @Override
    public void onForumNetworkTaskFinished(ArrayList<Forum> forum, ForumJob job) {
        switch (job) {
            case MENU:
                test.setForumMenu(forum);
                break;
            case CATEGORY:
                test.setForumList(forum);
                break;
            case TOPIC:
                test.setForumComments(forum);
                break;
        }
        setLoading(false);
    }

    public class testforumhtmlunit {
        Context context;
        @Getter
        @Setter
        String forumMenuLayout;
        String forumMenuTiles;
        String forumListLayout;
        String forumListTiles;
        String forumCommentsLayout;
        String forumCommentsTiles;
        String spoilerStructure;

        public testforumhtmlunit(Context context) {
            forumMenuLayout = getString(context, R.raw.forum_menu);
            forumMenuTiles = getString(context, R.raw.forum_menu_tiles);
            forumListLayout = getString(context, R.raw.forum_list);
            forumListTiles = getString(context, R.raw.forum_list_tiles);
            forumCommentsLayout = getString(context, R.raw.forum_comment);
            forumCommentsTiles = getString(context, R.raw.forum_comment_tiles);
            spoilerStructure = getString(context, R.raw.forum_comment_spoiler_structure);
            this.context = context;
        }

        public boolean menuExists() {
            return !forumMenuLayout.contains("<!-- insert here the tiles -->");
        }

        public void setForumMenu(ArrayList<Forum> menu) {
            if (menu != null && menu.size() > 0) {
                String forumArray = "";
                String tempTile;
                for (Forum item : menu) {
                    tempTile = forumMenuTiles;
                    tempTile = tempTile.replace("<!-- id -->", String.valueOf(item.getId()));
                    tempTile = tempTile.replace("<!-- header -->", item.getName());
                    tempTile = tempTile.replace("<!-- description -->", item.getDescription());
                    tempTile = tempTile.replace("<!-- last reply -->", getString(context, R.string.dialog_message_last_post));
                    forumArray = forumArray + tempTile;
                }
                forumMenuLayout = forumMenuLayout.replace("<!-- insert here the tiles -->", forumArray);
            }
            if (menuExists())
                webview.loadData(forumMenuLayout, "text/html", "UTF-8");
        }

        public void setForumList(ArrayList<Forum> forumList) {
            if (forumList != null && forumList.size() > 0) {
                String tempForumList;
                String forumArray = "";
                String tempTile;
                for (Forum item : forumList) {
                    tempTile = forumListTiles;
                    tempTile = tempTile.replace("<!-- id -->", String.valueOf(item.getId()));
                    tempTile = tempTile.replace("<!-- title -->", item.getName());
                    tempTile = tempTile.replace("<!-- username -->", item.getReply().getUsername());
                    tempTile = tempTile.replace("<!-- time -->", DateTools.parseDate(item.getReply().getTime(), true));
                    forumArray = forumArray + tempTile;
                }
                tempForumList = forumListLayout.replace("<!-- insert here the tiles -->", forumArray);
                webview.loadData(tempForumList, "text/html", "UTF-8");
            }
        }

        public void setForumComments(ArrayList<Forum> forumList) {
            if (forumList != null && forumList.size() > 0) {
                String tempForumList;
                String rank;
                String comment;
                String forumArray = "";
                String tempTile;
                for (Forum item : forumList) {
                    comment = item.getComment();
                    rank = item.getProfile().getSpecialAccesRank(item.getUsername());
                    // Spoiler rebuild
                    comment = comment.replaceAll("<div class=\"spoiler\">((.|\\n)+?)<br>((.|\\n)+?)</span>((.|\\n)+?)</div>", spoilerStructure + "$3</div></input>");

                    tempTile = forumCommentsTiles;
                    tempTile = tempTile.replace("<!-- username -->", item.getUsername());
                    tempTile = tempTile.replace("<!-- time -->", DateTools.parseDate(item.getTime(), true));
                    tempTile = tempTile.replace("<!-- comment -->", comment);
                    if (item.getProfile().getAvatarUrl().contains("xmlhttp-loader"))
                        tempTile = tempTile.replace("<!-- profile image -->", "http://cdn.myanimelist.net/images/na.gif");
                    else
                        tempTile = tempTile.replace("<!-- profile image -->", item.getProfile().getAvatarUrl());
                    if (!rank.equals(""))
                        tempTile = tempTile.replace("<!-- access rank -->", rank);
                    else
                        tempTile = tempTile.replace("<span class=\"forum__mod\"><!-- access rank --></span>", "");

                    forumArray = forumArray + tempTile;
                }
                tempForumList = forumCommentsLayout.replace("<!-- insert here the tiles -->", forumArray);
                webview.loadData(tempForumList, "text/html", "UTF-8");
            }
        }

        /**
         * Get the string of the given resource file.
         *
         * @param context  The application context
         * @param resource The resource of which string we need
         * @return String the wanted string
         */
        @SuppressWarnings("StatementWithEmptyBody")
        private String getString(Context context, int resource) {
            try {
                InputStream inputStream = context.getResources().openRawResource(resource);
                byte[] buffer = new byte[inputStream.available()];
                while (inputStream.read(buffer) != -1) ;
                return new String(buffer);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            return "";
        }
    }
}
