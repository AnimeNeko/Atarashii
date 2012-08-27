package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Home extends FragmentActivity implements ActionBar.TabListener, AnimuFragment.IAnimeFragment {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    HomeSectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Context context;
    PrefManager mPrefManager;
    public MALManager mManager;
    private boolean init = false;
    AnimuFragment af;
    public boolean instanceExists;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        
        mPrefManager = new PrefManager(context);
        init = mPrefManager.getInit();
        
        //The following is state handling code
        if (savedInstanceState != null)
        {
        	instanceExists = savedInstanceState.getBoolean("instanceExists", false);
        }
        else
        {
        	instanceExists = false;
        }
        	
		if (init == true) {
			setContentView(R.layout.activity_home);
			// Creates the adapter to return the Animu and Mango fragments
			mSectionsPagerAdapter = new HomeSectionsPagerAdapter(
					getSupportFragmentManager());
			
			mManager = new MALManager(context);
			
			if (!instanceExists)
			{
				
			}

			// Set up the action bar.
			final ActionBar actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
			mViewPager.setPageMargin(32);

			// When swiping between different sections, select the corresponding
			// tab.
			// We can also use ActionBar.Tab#select() to do this if we have a
			// reference to the
			// Tab.
			mViewPager
					.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							actionBar.setSelectedNavigationItem(position);
						}
					});

			// Add tabs for the animu and mango lists
			for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
				// Create a tab with text corresponding to the page title
				// defined by the adapter.
				// Also specify this Activity object, which implements the
				// TabListener interface, as the
				// listener for when this tab is selected.
				actionBar.addTab(actionBar.newTab()
						.setText(mSectionsPagerAdapter.getPageTitle(i))
						.setTabListener(this));
			
			}

			
		}
		else //If the app hasn't been configured, take us to the first run screen to sign in
		{
			Intent firstRunInit = new Intent(this, FirstTimeInit.class);
        	firstRunInit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(firstRunInit);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.menu_settings:
    		startActivity(new Intent (this, Settings.class));
    		break;
    	
    	//The following is the code that handles switching the list. It calls the fragment to update, then update the menu by invalidating
    	case R.id.listType_all:
    		if (af != null)
    		{
    			af.getAnimeRecords(0, false);
    			invalidateOptionsMenu();
    		}	
    		break;
    	case R.id.listType_watching:
    		if (af != null)
    		{
    			af.getAnimeRecords(1, false);
    			invalidateOptionsMenu();
    		}
    		break;
    	case R.id.listType_completed:
    		if (af != null)
    		{
    			af.getAnimeRecords(2, false);
    			invalidateOptionsMenu();
    		}
    		break;
    	case R.id.listType_onhold:
    		if (af != null)
    		{
    			af.getAnimeRecords(3, false);
    			invalidateOptionsMenu();
    		}
    		break;
    	case R.id.listType_dropped:
    		if (af != null)
    		{
    			af.getAnimeRecords(4, false);
    			invalidateOptionsMenu();
    		}
    		break;
    	case R.id.listType_plantowatch:
    		if (af != null)
    		{
    			af.getAnimeRecords(5, false);
    			invalidateOptionsMenu();
    		}
    		break;
    	case R.id.forceSync:
    		if (af != null)
    		{
    			af.getAnimeRecords(af.currentList, true);
    			Toast.makeText(context, R.string.toast_SyncMessage, Toast.LENGTH_LONG).show();
    		}
    		break;
    		
    	}
    	
    	return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	
    	if (instanceExists == true)
    	{
    		af.getAnimeRecords(af.currentList, false);
    	}
    	
    }


    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }


    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

	public void fragmentReady() { 
		//Interface implementation for knowing when the dynamically created fragment is finished loading

		//We use instantiateItem to return the fragment. Since the fragment IS instantiated, the method returns it.
		af = (AnimuFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
		
		//Logic to check if we have just signed in. If yes, automatically do a sync
		if (getIntent().getBooleanExtra("net.somethingdreadful.MAL.firstSync", false))
		{
			af.getAnimeRecords(af.currentList, true);
			getIntent().removeExtra("net.somethingdreadful.MAL.firstSync");
			Toast.makeText(context, R.string.toast_SyncMessage, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle state)
	{
		//This is telling out future selves that we already have some things and not to do them
		state.putBoolean("instanceExists", true);
		
		super.onSaveInstanceState(state);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
		if (af != null)
		{
			//All this is handling the ticks in the switch list menu
			switch (af.currentList)
	    	{
	    	case 0:
	    		menu.findItem(R.id.listType_all).setChecked(true);
	    		break;
	    	case 1:
	    		menu.findItem(R.id.listType_watching).setChecked(true);
	    		break;
	    	case 2:
	    		menu.findItem(R.id.listType_completed).setChecked(true);
	    		break;
	    	case 3:
	    		menu.findItem(R.id.listType_onhold).setChecked(true);
	    		break;
	    	case 4: 
	    		menu.findItem(R.id.listType_dropped).setChecked(true);
	    		break;
	    	case 5:
	    		menu.findItem(R.id.listType_plantowatch).setChecked(true);
	    	}
		}
	
    	
    	return true;
    }


}
