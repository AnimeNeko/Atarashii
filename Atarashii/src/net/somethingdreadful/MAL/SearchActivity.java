package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.BaseMALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.MangaRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SearchActivity extends BaseActionBarSearchView
implements BaseItemGridFragment.IBaseItemGridFragment, ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SearchSectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Context context;
    PrefManager mPrefManager;
    public MALManager mManager;
    BaseItemGridFragment animeItemGridFragment;
    BaseItemGridFragment mangaItemGridFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        mPrefManager = new PrefManager(context);

        setContentView(R.layout.activity_search);
        mManager = new MALManager(context);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new SearchSectionsPagerAdapter(
                getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.searchResult);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageMargin(32);
        // For swipe
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        // Add tabs for the animu and manga lists
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTag(mSectionsPagerAdapter.getTag(i))
                    .setTabListener(this));
        }

        String query = getIntent().getStringExtra("net.somethingdreadful.MAL.search_query");
        int ordinalListType = getIntent().getIntExtra(
                "net.somethingdreadful.MAL.search_type", BaseMALApi.ListType.ANIME.ordinal());
        BaseMALApi.ListType listType = BaseMALApi.ListType.values()[ordinalListType];
        if (query != null && !query.equals("")) {
            doSearch(query, listType);
            setQuery(query);
            if (listType == BaseMALApi.ListType.MANGA) {
                actionBar.setSelectedNavigationItem(1);
            }
        }
    }

    @Override
    public BaseMALApi.ListType getCurrentListType() {
        return (BaseMALApi.ListType) getSupportActionBar().getSelectedTab().getTag();
    }

    @Override
    public void doSearch(String query, BaseMALApi.ListType listType) {
        networkThread nt = new networkThread();
        nt.setListType(listType);
        nt.execute(query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_search_view, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_settings:
                startActivity(new Intent(this, Settings.class));
                break;

            case R.id.menu_logout:
                //showLogoutDialog();
                break;

            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void fragmentReady() {
        //Interface implementation for knowing when the dynamically created fragment is finished loading
        //We use instantiateItem to return the fragment. Since the fragment IS instantiated, the method returns it.
        animeItemGridFragment = (BaseItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
        mangaItemGridFragment = (BaseItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 1);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public class networkThread extends AsyncTask<String, Void, Void> {
        JSONArray _result;

        public MALApi.ListType getListType() {
            return listType;
        }

        public void setListType(MALApi.ListType listType) {
            this.listType = listType;
        }

        MALApi.ListType listType;

        @Override
        protected Void doInBackground(String... params) {
            String query = params[0];
            MALApi api = new MALApi(context);
            _result = api.search(getListType(), query);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String type = MALApi.getListTypeString(getListType());
            try {
                switch (listType) {
                    case ANIME: {
                        ArrayList<AnimeRecord> list = new ArrayList<AnimeRecord>();
                        for (int i = 0; i < _result.length(); i++) {
                            JSONObject genre = (JSONObject) _result.get(i);
                            AnimeRecord record = new AnimeRecord(mManager.getRecordDataFromJSONObject(genre, type));
                            list.add(record);
                        }
                        animeItemGridFragment.setAnimeRecords(list);
                        break;
                    }
                    case MANGA: {
                        ArrayList<MangaRecord> list = new ArrayList<MangaRecord>();
                        for (int i = 0; i < _result.length(); i++) {
                            JSONObject genre = (JSONObject) _result.get(i);
                            MangaRecord record = new MangaRecord(mManager.getRecordDataFromJSONObject(genre, type));
                            list.add(record);
                        }
                        mangaItemGridFragment.setMangaRecords(list);
                        break;
                    }
                }
            } catch (JSONException e) {
                Log.e(SearchActivity.class.getName(), Log.getStackTraceString(e));
            }

        }
    }


}
