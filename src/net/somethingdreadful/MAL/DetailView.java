package net.somethingdreadful.MAL;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailView extends FragmentActivity implements ActionBar.TabListener, DetailsBasicFragment.IDetailsBasicAnimeFragment {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    DetailSectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    MALManager mManager;
    Context context;
    int recordID;
    ActionBar actionBar;
   
    DetailsBasicFragment bfrag;
    
    TextView SynopsisView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);	
		
		context = getApplicationContext();
		mManager = new MALManager(context);
		
		recordID = getIntent().getIntExtra("net.somethingdreadful.MAL.recordID", 1);
        
		// Create the adapter that will return a fragment for basic stuff and stats
        mSectionsPagerAdapter = new DetailSectionsPagerAdapter(getSupportFragmentManager(), getBaseContext());

        // Set up the action bar.
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
//        actionBar.setTitle(Integer.toString(recordID));
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_view, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			finish();
		}
		
		
		return true;
	}
    

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    //Called after the basic fragment is finished it's setup, populate data into it
	public void basicFragmentReady() {
		bfrag = (DetailsBasicFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
		
		SynopsisView = (TextView) bfrag.getView().findViewById(R.id.Synopsis);
		
		getAnimeDetails(recordID);		
	}
	
	public void getAnimeDetails(int id)
	{
		new getAnimeDetailsTask().execute();
	}
	
	public class getAnimeDetailsTask extends AsyncTask<Void, Boolean, AnimeRecord>
	{

		int mID;
		MALManager mmManager;
		AnimeRecord mAr;
		ActionBar bar;
		ImageDownloader imageDownloader = new ImageDownloader();
		
		@Override
		protected void onPreExecute()
		{
			mID = recordID;
			mmManager = mManager;
			bar = actionBar;
		}
		
		@Override
		protected AnimeRecord doInBackground(Void... arg0) {
			
			mAr = mmManager.getAnimeRecordFromDB(mID);
			
			publishProgress(true);
			
			if (mAr.getSynopsis() == null)
			{
				mAr = mmManager.updateAnimeWithDetails(mID, mAr);
			}
			
			return mAr;
		}
		
		@Override
		protected void onProgressUpdate(Boolean... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			
			actionBar.setTitle(mAr.getName());
			
			((LinearLayout) bfrag.getView().findViewById(R.id.backgroundContainer))
				.setBackgroundDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
		}

		@Override
		protected void onPostExecute(AnimeRecord ar)
		{
			SynopsisView.setText(ar.getSynopsis());
		}
	}
    

}
