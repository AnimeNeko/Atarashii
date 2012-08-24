package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;
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
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class DetailView extends FragmentActivity implements DetailsBasicFragment.IDetailsBasicAnimeFragment {

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
        
        bfrag = (DetailsBasicFragment) getSupportFragmentManager().findFragmentById(R.id.DetailsFragment);
		
		context = getApplicationContext();
		mManager = new MALManager(context);
		
		recordID = getIntent().getIntExtra("net.somethingdreadful.MAL.recordID", 1);

        // Set up the action bar.
        actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
//        final FrameLayout layout = (FrameLayout) bfrag.getView().findViewById(R.id.backgroundContainer);
//        
//        
//    	ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
//    	if (viewTreeObserver.isAlive()) {
//    	  viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//    	    public void onGlobalLayout() {
//    	      layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//    	      
//    	      int synopsisOffset = layout.getHeight();
//    	      synopsisOffset -= layout.findViewById(R.id.SynopsisLabel).getHeight();
//    	      System.out.println(synopsisOffset);
//    	    	
//    	    	
//    	      LayoutParams params = (LayoutParams) layout.findViewById(R.id.SynopsisLabel).getLayoutParams();
//    	      params.setMargins(0, synopsisOffset, 0, 0);
//    	      layout.findViewById(R.id.SynopsisLabel).setLayoutParams(params);
//    	      
//    	      
//    	    	
//    	    }
//    	  });
//    	}

       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_view, menu);
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
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
    
    @Override
    public void onResume()
    {
    	super.onResume();
    
    }
   

    //Called after the basic fragment is finished it's setup, populate data into it
	public void basicFragmentReady() {
		
		SynopsisView = (TextView) bfrag.getView().findViewById(R.id.Synopsis);
		
		new waitTask().execute();
		
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
			
			((RelativeLayout) bfrag.getView().findViewById(R.id.backgroundContainer))
				.setBackgroundDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
		}

		@Override
		protected void onPostExecute(AnimeRecord ar)
		{
			SynopsisView.setText(ar.getSynopsis());
		}
	}
	
	public class waitTask extends AsyncTask<Void, Void, Void>
	{

		
		@Override
		protected Void doInBackground(Void... arg0) {
			
			
			try {
				Thread.sleep(40); //I really bloody can't believe I had to resort to this, but there simply isn't any other way
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
			

		}
		

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			bfrag.positionSynopsis();
			
		}
	}
    

}
