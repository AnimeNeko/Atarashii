package net.somethingdreadful.MAL;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.dialog.SearchIdDialogFragment;
import net.somethingdreadful.MAL.tasks.TaskJob;

public class SearchActivity extends ActionBarActivity implements IGFCallbackListener {
    public String query;
    IGF af;
    IGF mf;
    ViewPager ViewPager;
    IGFPagerAdapter mIGFPagerAdapter;
    SearchView searchView;
    ActionBar actionBar;

    boolean callbackAnimeError = false;
    boolean callbackMangaError = false;
    boolean callbackAnimeResultEmpty = false;
    boolean callbackMangaResultEmpty = false;
    int callbackCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mIGFPagerAdapter = new IGFPagerAdapter(getFragmentManager());

        ViewPager = (ViewPager) findViewById(R.id.pager);
        ViewPager.setAdapter(mIGFPagerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            if (TextUtils.isDigitsOnly(query)) {
                FragmentManager fm = getFragmentManager();
                (new SearchIdDialogFragment()).show(fm, "fragment_id_search");
            } else {
                if (searchView != null) {
                    searchView.setQuery(query, false);
                }
                if (af != null && mf != null) {
                    af.searchRecords(query);
                    mf.searchRecords(query);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search_view, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(query, false);
        return true;
    }

    @Override
    protected void onResume() {
        if (getIntent() != null)
            handleIntent(getIntent());
        super.onResume();
    }

    @Override
    public void onIGFReady(IGF igf) {
        /* Set Username to the search IGFs, looks strange but has a reason:
         * The username is passed to DetailViews if clicked, the DetailView tries to get user-specific
         * details (read/watch status, score). To do this it needs the username to determine the correct
         * anime-/mangalist
         */
        igf.setUsername(AccountService.getUsername());
        if (igf.listType.equals(ListType.ANIME))
            af = igf;
        else
            mf = igf;
        if (query != null && !TextUtils.isDigitsOnly(query)) // there is already a search to do
            igf.searchRecords(query);
    }

    @Override
    public void onRecordsLoadingFinished(ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {
        if (cancelled) {
            return;
        }

        callbackCounter++;

        if (type.equals(ListType.ANIME)) {
            callbackAnimeError = error;
            callbackAnimeResultEmpty = resultEmpty;
        } else {
            callbackMangaError = error;
            callbackMangaResultEmpty = resultEmpty;
        }

        if (callbackCounter >= 2) {
            callbackCounter = 0;

            if (callbackAnimeError && callbackMangaError) // the sync failed completely
                Toast.makeText(getApplicationContext(), R.string.toast_error_Search, Toast.LENGTH_SHORT).show();
            else if (callbackAnimeError || callbackMangaError) // one list failed to sync
                Toast.makeText(getApplicationContext(), callbackAnimeError ? R.string.toast_error_Search_Anime : R.string.toast_error_Search_Manga, Toast.LENGTH_SHORT).show();
            else if (callbackAnimeResultEmpty && callbackMangaResultEmpty)
                Toast.makeText(getApplicationContext(), R.string.toast_error_nothingFound, Toast.LENGTH_SHORT).show();
        }
    }
}