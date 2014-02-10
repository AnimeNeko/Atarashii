package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTask;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.MangaNetworkTask;
import net.somethingdreadful.MAL.tasks.MangaNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SearchActivity extends BaseActionBarSearchView
implements BaseItemGridFragment.IBaseItemGridFragment, ActionBar.TabListener,
	AnimeNetworkTaskFinishedListener, MangaNetworkTaskFinishedListener {

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
    ActionBar actionBar;
    Context context;
    PrefManager mPrefManager;
    public MALManager mManager;
    BaseItemGridFragment animeItemGridFragment;
    BaseItemGridFragment mangaItemGridFragment;
    Activity activity;
    
    boolean noAnimeRecordsFound = false;
    boolean errorSearchingAnime = false;
    boolean noMangaRecordsFound = false;
    boolean errorSearchingManga = false;
    
    boolean searchedOnce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        activity = this;
        mPrefManager = new PrefManager(context);
        
        searchedOnce = false;

        setContentView(R.layout.activity_search);
        mManager = new MALManager(context);

        // Set up the action bar.
        actionBar = getSupportActionBar();
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
                "net.somethingdreadful.MAL.search_type", MALApi.ListType.ANIME.ordinal());
        MALApi.ListType listType = MALApi.ListType.values()[ordinalListType];
        if (query != null && !query.equals("")) {
            doSearch(query, listType);
            setQuery(query);
            if (listType == MALApi.ListType.MANGA) {
                actionBar.setSelectedNavigationItem(1);
            }
        }
        
        NfcHelper.disableBeam(this);
    }

    @Override
    public MALApi.ListType getCurrentListType() {
        return (MALApi.ListType) getSupportActionBar().getSelectedTab().getTag();
    }

    @Override
    public void doSearch(String query, MALApi.ListType listType) { //ignore listtype, search both anime and manga
        new AnimeNetworkTask(TaskJob.SEARCH, context, this).execute(query);
        new MangaNetworkTask(TaskJob.SEARCH, context, this).execute(query);
        
        if (mSearchView != null) {
        	mSearchView.clearFocus();
            mSearchView.setFocusable(false);
        }
        
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
    
    public void displayCrouton() {
    	if (!searchedOnce) {
    		searchedOnce = true;
    	}
    	else {
    		if (noAnimeRecordsFound && noMangaRecordsFound && (!errorSearchingAnime || !errorSearchingManga)) {
            	Crouton.makeText(activity, R.string.crouton_nothingFound, Style.ALERT).show();
            }
            else if (noAnimeRecordsFound) {          	
            	mViewPager.setCurrentItem(1);
            	actionBar.setSelectedNavigationItem(1);
            }
            else if (noMangaRecordsFound) {
            	mViewPager.setCurrentItem(0);
            	actionBar.setSelectedNavigationItem(0);
            }
    		
    		// error handling
    		if (errorSearchingAnime && errorSearchingManga) {
    		    errorSearchingManga = false;
    		    errorSearchingAnime = false;
    		    Crouton.makeText(activity, R.string.crouton_Search_error, Style.ALERT).show();
    		} else {
        		if (errorSearchingManga) {
                    errorSearchingManga = false;
                    Crouton.makeText(activity, R.string.crouton_Search_Manga_error, Style.ALERT).show();
                }
        		
        		if (errorSearchingAnime) {
                    errorSearchingAnime = false;
                    Crouton.makeText(activity, R.string.crouton_Search_Anime_error, Style.ALERT).show();
                }
    		}
    		
    		searchedOnce = false;
    		noAnimeRecordsFound = false;
    		noMangaRecordsFound = false;
    	}
    }

	@Override
	public void onMangaNetworkTaskFinished(ArrayList<Manga> result, TaskJob job, int page) {
		if ( result != null ) {
			if (result.size() == 0)
				noMangaRecordsFound = true;
			mangaItemGridFragment.setMangaRecords(result);
		} else {
		    noMangaRecordsFound = true;
		    errorSearchingManga = true;
		}
		displayCrouton();
	}

	@Override
	public void onAnimeNetworkTaskFinished(ArrayList<Anime> result, TaskJob job, int page) {
		if ( result != null ) {
			if (result.size() == 0)
				noAnimeRecordsFound = true;
			animeItemGridFragment.setAnimeRecords(result);
		} else {
            noAnimeRecordsFound = true;
            errorSearchingAnime = true;
        }
		displayCrouton();
	}


}
