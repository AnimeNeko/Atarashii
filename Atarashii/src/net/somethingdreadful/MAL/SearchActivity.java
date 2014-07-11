package net.somethingdreadful.MAL;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import android.support.v4.view.MenuItemCompat;
import net.somethingdreadful.MAL.tasks.TaskJob;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SearchActivity extends Activity implements TabListener, ViewPager.OnPageChangeListener {
	static IGF af;
	static IGF mf;
    ViewPager ViewPager;
    PrefManager mPrefManager;
    static String query;
    Context context;
    SearchView searchView;
    ActionBar actionBar;
    ArrayList<String> tabs = new ArrayList<String>();
    
    static boolean animeError = false;
    static boolean mangaError = false;
    static int called = 0;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        context = getApplicationContext();
        mPrefManager = new PrefManager(context);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);  
        
        ViewPager = (ViewPager) findViewById(R.id.pager);
        ViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));
        ViewPager.setOnPageChangeListener(this); 
        
        setTabs();

        handleIntent(getIntent());
    }
	
	public void setTabs(){
		tabs.add("Anime");
		tabs.add("Manga");
		if (af == null){
			af = new IGF();
			af.isAnime = true;
			af.taskjob = TaskJob.SEARCH;
			mf = new IGF();
			mf.isAnime = false;
			mf.taskjob = TaskJob.SEARCH;
		}
		
		for (String tab : tabs) {
        	actionBar.addTab(actionBar.newTab().setText(tab).setTabListener(this));
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
        handleIntent(intent);
    }
    
    @Override
    public void onStop () {
    	query = "";
    	super.onStop();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            if (searchView != null){
            	searchView.setQuery(query, false);
            	af.page = 1;
            	mf.page = 1;
            	af.getRecords(true, null, 0);
            	mf.getRecords(true, null, 0);
            }
        }
    }
    
    public MALApi.ListType getCurrentListType() {
        return (MALApi.ListType) getSupportActionBar().getSelectedTab().getTag();
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
	
	public class TabsPagerAdapter  extends FragmentPagerAdapter {
		  
		 	    public TabsPagerAdapter (FragmentManager fm) {
		 	        super(fm);
		 	    }
		 
		 	    @Override
		 	    public Fragment getItem(int i) {
		 	    	switch (i) {
		         		case 0:
		         			return af;
		         		case 1:
		         			return mf;
		 	            default:
		         			return af;
		 	        }
		 	    }
		 
		 	    @Override
		 	    public int getCount() {
		 	        return 2;
		 	    }
		 	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
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
    public void onPause() {
        super.onPause();
    }
	
	public static void onError(ListType type, boolean error, Activity activity, TaskJob job) {
		called = called + 1;
		
		if (error) {
			if (ListType.ANIME.equals(type)){
				animeError = true;
			} else {
				mangaError = true;
			}
		}

		if (called >= 2){
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
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_search_view, menu);

	    // Associate searchable configuration with the SearchView
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false);
	    searchView.setFocusableInTouchMode(true);
	    searchView.requestFocusFromTouch();
	    searchView.setQuery(query, false);
	    return true;
	}
}