package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.ItemGridFragment.IItemGridFragment;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTask;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.MangaNetworkTask;
import net.somethingdreadful.MAL.tasks.MangaNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SearchActivity extends BaseActionBarSearchView
implements IItemGridFragment, ActionBar.TabListener,
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
    ViewFlipper vf;
    ActionBar actionBar;
    static Context context;
    PrefManager mPrefManager;
    ItemGridFragment af;
    ItemGridFragment mf;
    BaseActionBarSearchView b;
    
    public boolean animeError = false;
    public boolean mangaError = false;
    public boolean instanceExists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        mPrefManager = new PrefManager(context);
        instanceExists = savedInstanceState != null && savedInstanceState.getBoolean("instanceExists", false);

        setContentView(R.layout.activity_search);

        vf = (ViewFlipper)findViewById(R.id.viewFlipperSearch);

        // Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new SearchSectionsPagerAdapter(getSupportFragmentManager());

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

        BaseActionBarSearchView.query = getIntent().getStringExtra("net.somethingdreadful.MAL.search_query");
        int ordinalListType = getIntent().getIntExtra("net.somethingdreadful.MAL.search_type", MALApi.ListType.ANIME.ordinal());
        MALApi.ListType listType = MALApi.ListType.values()[ordinalListType];
        if (BaseActionBarSearchView.query != null && !BaseActionBarSearchView.query.equals("")) {
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

    public void doSearch() { //i search both anime and manga
        toggleLoadingIndicator(true);
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
    	ItemGridFragment.home = false;
        af = (ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
        mf = (ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 1);
        af.setMode(TaskJob.SEARCH);
        mf.setMode(TaskJob.SEARCH);
        doSearch();	
    }

    @Override
    public boolean onQueryTextChange(String newText) {
    	animeError = false;
    	mangaError = false;
        return false;
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
	
	@Override
    public void onPause() {
        super.onPause();
        instanceExists = true;
    }

	public void onResume() {
        super.onResume();
        ItemGridFragment.home = false;
        if (instanceExists && af.getMode()==null) {
        	af.getRecords(af.currentList, "anime", false, context);
        	mf.getRecords(mf.currentList, "manga", false, context);
        }
    }

    private void toggleLoadingIndicator(boolean show) {
        if (vf != null) {
            vf.setDisplayedChild(show ? 1 : 0);
        }
    }

	public void onAnimeNetworkTaskFinished(ArrayList<Anime> result, TaskJob job, int page) {
		if (result != null) {
            af.setAnimeRecords(result);
			if (result.size() > 0) {
				SearchActivity.this.af.scrollListener.notifyMorePages(ListType.ANIME);
			} else if (!animeError) {
				animeError = true;
				if (mangaError)
					Crouton.makeText(this, R.string.crouton_error_nothingFound, Style.ALERT).show();
			}
		} else if (!animeError) {
		    Crouton.makeText(this, R.string.crouton_error_Anime_Sync, Style.ALERT).show();
		    animeError = true;
        }
        toggleLoadingIndicator(false);
	}

	public void onMangaNetworkTaskFinished(ArrayList<Manga> result, TaskJob job, int page) {
		if (result != null) {
            mf.setMangaRecords(result);
			if (result.size() > 0) {
				SearchActivity.this.mf.scrollListener.notifyMorePages(ListType.MANGA);	
			} else if (!mangaError) {
				mangaError = true;
				if (animeError)
					Crouton.makeText(this, R.string.crouton_error_nothingFound, Style.ALERT).show();
			}
		} else if (!mangaError) {
		    Crouton.makeText(this, R.string.crouton_error_Manga_Sync, Style.ALERT).show();
		    mangaError = true;
		}
        toggleLoadingIndicator(false);
	}
}
