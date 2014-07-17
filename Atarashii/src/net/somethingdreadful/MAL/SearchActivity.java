package net.somethingdreadful.MAL;

import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.tasks.TaskJob;

import org.holoeverywhere.app.Activity;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SearchActivity extends Activity implements TabListener, ViewPager.OnPageChangeListener, IGF.IGFReadyListener {
    IGF af;
    IGF mf;
    String query;
    static boolean animeError = false;
    static boolean mangaError = false;
    static int called = 0;
    ViewPager ViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;
    PrefManager mPrefManager;
    Context context;
    SearchView searchView;
    ActionBar actionBar;

    public static void onError(ListType type, boolean error, Activity activity, TaskJob job) {
        called = called + 1;

        if (error) {
            if (ListType.ANIME.equals(type)) {
                animeError = true;
            } else {
                mangaError = true;
            }
        }

        if (called >= 2) {
            called = 0;
            if (job.equals(TaskJob.FORCESYNC))
                Crouton.makeText(activity, R.string.crouton_info_SyncDone, Style.CONFIRM).show();
            if (mangaError & animeError)
                Crouton.makeText(activity, R.string.crouton_error_nothingFound, Style.ALERT).show();
            NotificationManager nm = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(R.id.notification_sync);

            mangaError = false;
            animeError = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        context = getApplicationContext();
        mPrefManager = new PrefManager(context);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        
        ViewPager = (ViewPager) findViewById(R.id.pager);
        ViewPager.setAdapter(mSectionsPagerAdapter);
        ViewPager.setOnPageChangeListener(this);

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                .setText(mSectionsPagerAdapter.getPageTitle(i))
                .setTabListener(this));
        }
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
            if (searchView != null) {
                searchView.setQuery(query, false);
            }
            if (af != null && mf != null) {
                af.searchRecords(query);
                mf.searchRecords(query);
            }
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        ViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onIGFReady() {
        af = (IGF)mSectionsPagerAdapter.instantiateItem(ViewPager, 0);
        mf = (IGF)mSectionsPagerAdapter.instantiateItem(ViewPager, 1);
        if (query != null) { // there is already a search to do
            af.searchRecords(query);
            mf.searchRecords(query);
        }
    }


    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search_view, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
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
}