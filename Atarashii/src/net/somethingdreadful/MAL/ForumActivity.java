package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.Forum;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.forum.ForumInterface;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public class ForumActivity extends AppCompatActivity implements ForumNetworkTask.ForumNetworkTaskListener, NumberPickerDialogFragment.onUpdateClickListener {
    @Bind(R.id.webview)
    public
    WebView webview;
    @Bind(R.id.progress1)
    ProgressBar progress;
    private testforumhtmlunit test;
    private MenuItem search;
    private String query;
    private boolean loading = false;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Theme.setTheme(this, R.layout.activity_forum, false);
        Theme.setActionBar(this);
        ButterKnife.bind(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new ForumInterface(this), "Forum");
        webview.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                setLoading(false);
            }
        });

        test = new testforumhtmlunit(this);
        if (bundle != null) {
            test.setForumMenuLayout(bundle.getString("forumMenuLayout"));
            webview.restoreState(bundle.getBundle("webview"));
        } else {
            getRecords(ForumJob.MENU, 0, "1");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forum, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        ComponentName cn = new ComponentName(this, ForumActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        search = searchItem;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_ViewMALPage:
                String[] details = webview.getTitle().split(" ");
                switch (details[0]) {
                    case "M": // main board
                        startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse("http://myanimelist.net/forum/")));
                        break;
                    case "S": // sub board
                        startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse("http://myanimelist.net/forum/?subboard=" + details[1])));
                        break;
                    case "T": // topic list
                        startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse("http://myanimelist.net/forum/?board=" + details[1])));
                        break;
                    case "C": // commments
                        startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse("http://myanimelist.net/forum/?topicid=" + details[1])));
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Handle the intent for the searchView
     *
     * @param intent The intent given by android
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            if (query.equals("Atarashii:clear")) {
                webview.clearCache(true);
                CookieManager.getInstance().removeAllCookie();
                finish();
            } else {
                getRecords(ForumJob.SEARCH, 0, "1");
            }
            search.collapseActionView();
        }
    }

    public void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putString("forumMenuLayout", test.getForumMenuLayout());
        Bundle webviewState = new Bundle();
        webview.saveState(webviewState);
        state.putBundle("webview", webviewState);
        super.onSaveInstanceState(state);
    }

    public void getRecords(ForumJob job, int id, String page) {
        if (!loading) {
            loading = true;
            test.setSubBoard(false);
            setLoading(true);
            test.setId(id);
            test.setPage(page);
            switch (job) {
                case MENU:
                    if (!test.menuExists())
                        new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        test.setForumMenu(null);
                    break;
                case SUBCATEGORY:
                    test.setSubBoard(true);
                case CATEGORY:
                case TOPIC:
                    new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);
                    break;
                case SEARCH:
                    new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
                    break;
            }
        }
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
            case SEARCH:
            case CATEGORY:
            case SUBCATEGORY:
                test.setForumList(forum);
                break;
            case TOPIC:
                test.setForumComments(forum);
                break;
            case UPDATECOMMENT:
                Theme.Snackbar(this, forum != null ? R.string.toast_info_comment_added : R.string.toast_error_Records);
                setLoading(false);
                break;
            case ADDCOMMENT:
                Theme.Snackbar(this, forum != null ? R.string.toast_info_comment_added : R.string.toast_error_Records);
                if (forum != null)
                    test.setForumComments(forum);
                break;
        }
        loading = false;
    }

    @Override
    public void onUpdated(int number, int id) {
        String[] details = webview.getTitle().split(" ");
        switch (details[0]) {
            case "S": // sub board
                getRecords(ForumJob.SUBCATEGORY, Integer.parseInt(String.valueOf(id)), String.valueOf(number));
                break;
            case "T": // topic list
                getRecords(ForumJob.CATEGORY, Integer.parseInt(String.valueOf(id)), String.valueOf(number));
                break;
            case "C": // commments
                getRecords(ForumJob.TOPIC, Integer.parseInt(String.valueOf(id)), String.valueOf(number));
                break;
        }
    }

    public class testforumhtmlunit {
        final Context context;
        @Getter
        @Setter
        String forumMenuLayout;
        final String forumMenuTiles;
        final String forumListLayout;
        final String forumListTiles;
        final String forumCommentsLayout;
        final String forumCommentsTiles;
        final String spoilerStructure;
        @Getter
        @Setter
        int id;
        @Getter
        @Setter
        String page;
        boolean subBoard = false;

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

        public void setSubBoard(boolean subBoard) {
            this.subBoard = subBoard;
        }

        public boolean getSubBoard() {
            return this.subBoard;
        }

        public boolean menuExists() {
            return !forumMenuLayout.contains("<!-- insert here the tiles -->");
        }

        private void loadWebview(String html) {
            if (Theme.darkTheme) {
                html = html.replace("#f2f2f2;", "#212121;"); // hover tags
                html = html.replace("#FFF;", "#313131;"); // body
                html = html.replace("#EEE;", "#212121;"); // body border
                html = html.replace("#022f70;", "#0078a0;"); // selection tags
                html = html.replace("#3E454F;", "#818181;"); // time ago
                html = html.replace("markdown {", "markdown {color:#E3E3E3;"); // comment body color
            }
            html = html.replace("data:text/html,", "");
            webview.loadData(html, "text/html; charset=utf-8", "UTF-8");
        }

        public void setForumMenu(ArrayList<Forum> menu) {
            if (menu != null && menu.size() > 0) {
                String forumArray = "";
                String tempTile;
                String description;
                for (Forum item : menu) {
                    tempTile = forumMenuTiles;
                    description = item.getDescription();

                    if (item.getChildren() != null) {
                        tempTile = tempTile.replace("onClick=\"tileClick(<!-- id -->)\"", "");
                        description = description + " ";

                        for (int i = 0; i < item.getChildren().size(); i++) {
                            Forum child = item.getChildren().get(i);
                            description = description + "<a onClick=\"subTileClick(" + child.getId() + ")\">" + child.getName() + "</a>" + (i < item.getChildren().size() - 1 ? ", " : "");
                        }
                    } else {
                        tempTile = tempTile.replace("<!-- id -->", String.valueOf(item.getId()));
                    }

                    tempTile = tempTile.replace("<!-- header -->", item.getName());
                    tempTile = tempTile.replace("<!-- description -->", description);
                    tempTile = tempTile.replace("<!-- last reply -->", getString(context, R.string.dialog_message_last_post));
                    forumArray = forumArray + tempTile;
                }
                forumMenuLayout = forumMenuLayout.replace("<!-- insert here the tiles -->", forumArray);
                forumMenuLayout = forumMenuLayout.replace("<!-- title -->", "M 0"); // M = menu, 0 = id
            }
            if (menuExists())
                loadWebview(forumMenuLayout);
        }

        public void setForumList(ArrayList<Forum> forumList) {
            if (forumList != null && forumList.size() > 0) {
                String tempForumList;
                String forumArray = "";
                String tempTile;
                int maxPages = forumList.get(0).getMaxPages();
                for (Forum item : forumList) {
                    tempTile = forumListTiles;
                    tempTile = tempTile.replace("<!-- id -->", String.valueOf(item.getId()));
                    tempTile = tempTile.replace("<!-- title -->", item.getName());
                    if (item.getReply() != null) {
                        tempTile = tempTile.replace("<!-- username -->", item.getReply().getUsername());
                        tempTile = tempTile.replace("<!-- time -->", DateTools.parseDate(item.getReply().getTime(), true));
                    }
                    forumArray = forumArray + tempTile;
                }
                tempForumList = forumListLayout.replace("<!-- insert here the tiles -->", forumArray);
                tempForumList = tempForumList.replace("<!-- title -->", (getSubBoard() ? "S " : "T ") + getId() + " " + maxPages); // T = Topics || S = subboard, id
                if (Integer.parseInt(getPage()) == 1) {
                    tempForumList = tempForumList.replace("class=\"previous\"", "class=\"previous\" style=\"visibility: hidden;\"");
                }
                if (Integer.parseInt(getPage()) == maxPages) {
                    tempForumList = tempForumList.replace("class=\"next\"", "class=\"next\" style=\"visibility: hidden;\"");
                }
                tempForumList = tempForumList.replace("Forum.prevTopicList(" + getPage(), "Forum.prevTopicList(" + (Integer.parseInt(getPage()) - 1));
                tempForumList = tempForumList.replace("Forum.nextTopicList(" + getPage(), "Forum.nextTopicList(" + (Integer.parseInt(getPage()) + 1));
                tempForumList = tempForumList.replace("<!-- page -->", getPage());
                tempForumList = tempForumList.replace("<!-- next -->", context.getString(R.string.next));
                tempForumList = tempForumList.replace("<!-- previous -->", context.getString(R.string.previous));
                loadWebview(tempForumList);
            }
        }

        public void setForumComments(ArrayList<Forum> forumList) {
            if (forumList != null && forumList.size() > 0) {
                String tempForumList;
                String rank;
                String comment;
                String forumArray = "";
                String tempTile;
                int maxPages = forumList.get(0).getMaxPages();
                for (Forum item : forumList) {
                    rank = item.getProfile().getSpecialAccesRank(item.getUsername());
                    comment = item.getComment();
                    comment = convertComment(comment);

                    tempTile = forumCommentsTiles;
                    if (item.getUsername().equalsIgnoreCase(AccountService.getUsername()))
                        tempTile = tempTile.replace("fa-quote-right fa-lg\"", "fa-pencil fa-lg\" id=\"edit\"");
                    tempTile = tempTile.replace("<!-- username -->", item.getUsername());
                    tempTile = tempTile.replace("<!-- comment id -->", Integer.toString(item.getId()));
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
                    if (item.getChildren() != null) {
                        tempTile = tempTile.replace("<!-- child -->", getChildren(item.getChildren()));
                    }
                    forumArray = forumArray + tempTile;
                }
                tempForumList = forumCommentsLayout.replace("<!-- insert here the tiles -->", forumArray);
                tempForumList = tempForumList.replace("<!-- title -->", "C " + getId() + " " + maxPages + " " + getPage()); // C = Comments, id, maxPages, page
                if (Integer.parseInt(getPage()) == 1) {
                    tempForumList = tempForumList.replace("class=\"previous\"", "class=\"previous\" style=\"visibility: hidden;\"");
                }
                if (Integer.parseInt(getPage()) == maxPages) {
                    tempForumList = tempForumList.replace("class=\"next\"", "class=\"next\" style=\"visibility: hidden;\"");
                }
                tempForumList = tempForumList.replace("Forum.prevCommentList(" + getPage(), "Forum.prevCommentList(" + (Integer.parseInt(getPage()) - 1));
                tempForumList = tempForumList.replace("Forum.nextCommentList(" + getPage(), "Forum.nextCommentList(" + (Integer.parseInt(getPage()) + 1));
                tempForumList = tempForumList.replace("<!-- page -->", getPage());
                tempForumList = tempForumList.replace("<!-- next -->", context.getString(R.string.next));
                tempForumList = tempForumList.replace("<!-- previous -->", context.getString(R.string.previous));
                if (!AccountService.isMAL()) {
                    tempForumList = tempForumList.replace("[b][/b]", "____");
                    tempForumList = tempForumList.replace("[i][/i]", "__");
                    tempForumList = tempForumList.replace("[s][/s]", "~~~~");
                    tempForumList = tempForumList.replace("[spoiler][/spoiler]", "~!!~");
                    tempForumList = tempForumList.replace("[url=][/url]", "[link](URL)");
                    tempForumList = tempForumList.replace("[img][/img]", "img220(URL)");
                    tempForumList = tempForumList.replace("[yt][/yt]", "youtube(ID)");
                    tempForumList = tempForumList.replace("[list][/list]", "1.");
                    tempForumList = tempForumList.replace("[size=][/size]", "##");
                    tempForumList = tempForumList.replace("[center][/center]", "~~~~~~");
                    tempForumList = tempForumList.replace("[quote][/quote]", ">");
                    tempForumList = tempForumList.replaceAll("webm(.+?),\"(.+?)\"\\);\\}", "webm$1,\"webm(URL)\"\\);}");
                    tempForumList = tempForumList.replaceAll("ulist(.+?),\"(.+?)\"\\);\\}", "ulist$1,\"- \"\\);}");
                }
                loadWebview(tempForumList);
            }
        }

        private String getChildren(ArrayList<Forum> forumList) {
            if (forumList != null && forumList.size() > 0) {
                String rank;
                String comment;
                String forumArray = "";
                String tempTile;
                for (Forum item : forumList) {
                    rank = item.getProfile().getSpecialAccesRank(item.getUsername());
                    comment = item.getComment() == null ? "" : item.getComment();
                    comment = convertComment(comment);

                    tempTile = forumCommentsTiles;
                    tempTile = tempTile.replace("<div class=\"comment\">", "<div class=\"subComment\">");
                    if (item.getUsername().equalsIgnoreCase(AccountService.getUsername()))
                        tempTile = tempTile.replace("fa-quote-right fa-lg\"", "fa-pencil fa-lg\" id=\"edit\"");
                    tempTile = tempTile.replace("<!-- username -->", item.getUsername());
                    tempTile = tempTile.replace("<!-- comment id -->", Integer.toString(item.getId()));
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
                    if (item.getChildren() != null) {
                        tempTile = tempTile.replace("<!-- child -->", getChildren(item.getChildren()));
                    }
                    forumArray = forumArray + tempTile;
                }
                return forumArray;
            }
            return "";
        }

        private String convertComment(String comment) {
            if (AccountService.isMAL()) {
                comment = comment.replaceAll("<div class=\"spoiler\">((.|\\n)+?)<br>((.|\\n)+?)</span>((.|\\n)+?)</div>", spoilerStructure + "$3</div></input>");
                comment = comment.replaceAll("<div class=\"hide_button\">((.|\\n)+?)class=\"quotetext\">((.|\\n)+?)</div>", spoilerStructure + "$3</input>");
                comment = comment.replaceAll("@(\\w+)", "<font color=\"#022f70\"><b>@$1</b></font>");
            } else {
                comment = comment.replaceAll("(.*)>(.*)", "<div class=\"quotetext\">$2</div>");
                comment = comment.replaceAll("`((.|\\n)+?)`", "<div class=\"codetext\">$1</div>");
                comment = comment.replaceAll("__((.|\\n)+?)__", "<b>$1</b>");
                comment = comment.replaceAll("_((.|\\n)+?)_", "<i>$1</i>");
                comment = comment.replaceAll("~~~((.|\\n)+?)~~~", "<center>$1</center>");
                comment = comment.replaceAll("~~((.|\\n)+?)~~", "<span style=\"text-decoration:line-through;\">$1</span>");
                comment = comment.replaceAll("~!((.|\\n)+?)!~", spoilerStructure + "$1</div></input>");
                comment = comment.replaceAll("\\[((.|\\n)+?)\\]\\(((.|\\n)+?)\\)", "<a href=\"$3\" rel=\"nofollow\">$1</a>");
                comment = comment.replaceAll("img(\\d.+?)\\((\\w.+?)\\)", "<img width=\"$1\" src=\"$2\">");
                comment = comment.replaceAll("(.*)##(.*)", "<h1>$2</h1>");
                comment = comment.replaceAll("(.*)#(.*)", "<h1>$2</h1>");
                comment = comment.replace("\n", "<br>");
                comment = comment.replaceAll("@(\\w+)", "<font color=\"#022f70\"><b>@$1</b></font>");
            }
            return comment;
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
