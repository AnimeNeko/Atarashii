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

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.Forum;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.forum.ForumHTMLUnit;
import net.somethingdreadful.MAL.forum.ForumInterface;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForumActivity extends AppCompatActivity implements ForumNetworkTask.ForumNetworkTaskListener, NumberPickerDialogFragment.onUpdateClickListener {
    @BindView(R.id.webview)
    public
    WebView webview;
    @BindView(R.id.progress1)
    ProgressBar progress;
    private ForumHTMLUnit forumHTMLUnit;
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

        forumHTMLUnit = new ForumHTMLUnit(this, webview);
        if (bundle != null) {
            forumHTMLUnit.setForumMenuLayout(bundle.getString("forumMenuLayout"));
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
                        launchBrowser("https://myanimelist.net/forum/", "http://anilist.co/forum/categories");
                        break;
                    case "S": // sub board
                        launchBrowser("https://myanimelist.net/forum/?subboard=" + details[1], "http://anilist.co/forum/thread/" + details[1]);
                        break;
                    case "T": // topic list
                        launchBrowser("https://myanimelist.net/forum/?board=" + details[1], "http://anilist.co/forum/tag?tag=" + details[1]);
                        break;
                    case "C": // commments
                        launchBrowser("https://myanimelist.net/forum/?topicid=" + details[1], "http://anilist.co/forum/thread/" + details[1]);
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchBrowser(String MAL, String AL) {
        if (AccountService.isMAL())
            startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse(MAL)));
        else
            startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse(AL)));
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
        state.putString("forumMenuLayout", forumHTMLUnit.getForumMenuLayout());
        Bundle webviewState = new Bundle();
        webview.saveState(webviewState);
        state.putBundle("webview", webviewState);
        super.onSaveInstanceState(state);
    }

    public void getRecords(ForumJob job, int id, String page) {
        if (!loading) {
            loading = true;
            forumHTMLUnit.setSubBoard(false);
            setLoading(true);
            forumHTMLUnit.setId(id);
            forumHTMLUnit.setPage(page);
            switch (job) {
                case MENU:
                    if (!forumHTMLUnit.menuExists())
                        new ForumNetworkTask(this, this, job, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        forumHTMLUnit.setForumMenu(null);
                    break;
                case SUBCATEGORY:
                    forumHTMLUnit.setSubBoard(true);
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
                forumHTMLUnit.setForumMenu(forum);
                break;
            case SEARCH:
            case CATEGORY:
            case SUBCATEGORY:
                forumHTMLUnit.setForumList(forum);
                break;
            case TOPIC:
                forumHTMLUnit.setForumComments(forum);
                break;
            case UPDATECOMMENT:
                Theme.Snackbar(this, forum != null ? R.string.toast_info_comment_added : R.string.toast_error_Records);
                setLoading(false);
                break;
            case ADDCOMMENT:
                Theme.Snackbar(this, forum != null ? R.string.toast_info_comment_added : R.string.toast_error_Records);
                if (forum != null)
                    forumHTMLUnit.setForumComments(forum);
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
}
