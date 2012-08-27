package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DetailView extends FragmentActivity implements DetailsBasicFragment.IDetailsBasicAnimeFragment, EpisodesPickerDialogFragment.DialogDismissedListener {

    MALManager mManager;
    Context context;
    int recordID;
    ActionBar actionBar;
    AnimeRecord mAr;
   
    DetailsBasicFragment bfrag;
    FragmentManager fm;
    EpisodesPickerDialogFragment epd;
    
    TextView SynopsisView;
    TextView AnimeTypeView;
    TextView AnimeStatusView;
    TextView MyStatusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);	
        
        bfrag = (DetailsBasicFragment) getSupportFragmentManager().findFragmentById(R.id.DetailsFragment);
		
		context = getApplicationContext();
		mManager = new MALManager(context);
		
		fm = getSupportFragmentManager();
		
		//Get the recordID, passed in from the calling activity
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
			break;
		case R.id.action_SetWatched:
			showEpisodesWatchedDialog();
			break;
		}
		
		
		return true;
	}
    
    @Override
    public void onResume()
    {
    	super.onResume();
    
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    	if (mAr.getDirty() == 1)
    	{
    		writeDetails(mAr);
    	}
    }
   

    //Called after the basic fragment is finished it's setup, populate data into it
    //Probably no longer necessary, since we no longer instantiate the fragment dynamically
	public void basicFragmentReady() {
		
		SynopsisView = (TextView) bfrag.getView().findViewById(R.id.Synopsis);
		AnimeStatusView = (TextView) bfrag.getView().findViewById(R.id.animeStatusLabel);
		AnimeTypeView = (TextView) bfrag.getView().findViewById(R.id.animeTypeLabel);
		MyStatusView = (TextView) bfrag.getView().findViewById(R.id.animeMyStatusLabel);
		
		//waitTask is basically a ridiculously hacky solution to a problem that shouldn't exist. It's introducing a delay on 
		//separate thread while waiting for the activity to draw it's action bar. Unfortunately the actionBar has no callbacks
		//for when it's actually displayed and ready, so this is really the only way I found to do it
		new waitTask().execute();
		
		getAnimeDetails(recordID);		
	}
	
	public void getAnimeDetails(int id)
	{
		new getAnimeDetailsTask().execute();
	}
	
	public void showEpisodesWatchedDialog()
	{
		//Standard code for setting up a dialog fragment
		//Note we use setStyle to change the theme, the default light styled dialog didn't look good so we use the dark dialog
		epd = new EpisodesPickerDialogFragment();
		epd.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
		epd.show(fm, "fragment_EditEpisodesWatchedDialog");
	}
	
	public class getAnimeDetailsTask extends AsyncTask<Void, Boolean, AnimeRecord>
	{

		int mID;
		MALManager mmManager;
		ActionBar bar;
		ImageDownloader imageDownloader = new ImageDownloader(context);
		
		@Override
		protected void onPreExecute()
		{
			mID = recordID;
			mmManager = mManager;
		}
		
		@Override
		protected AnimeRecord doInBackground(Void... arg0) {
			
			mAr = mmManager.getAnimeRecordFromDB(mID);
			
			//Basically I just use publishProgress as an easy way to display info we already have loaded sooner
			//This way, I can let the database work happen on the background thread and then immediately display it while
			//the synopsis loads if it hasn't previously been downloaded.
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
			AnimeStatusView.setText(mAr.getRecordStatus().toUpperCase());
			AnimeTypeView.setText(mAr.getRecordType().toUpperCase());
			MyStatusView.setText(mAr.getMyStatus().toUpperCase());
			
			
			//I think there's a potential crash issue here. If the image isn't loaded (ie the user if bloody impatient and clicked
			//something while the picture was still loading), it can crash.
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
				Thread.sleep(100); //I really bloody can't believe I had to resort to this, but there simply isn't any other way
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
			
			//After the delay, call the method that positions the synopsis to run it's caluclations.
			//Hopefully, the actionbar is done it's setup by now
			bfrag.positionSynopsis();
			
		}
	}
	
	public class writeDetailsTask extends AsyncTask<AnimeRecord, Void, Boolean>
	{

		
		@Override
		protected Boolean doInBackground(AnimeRecord... ar) {
			
			boolean result;
			
			mManager.insertOrUpdateAnime(ar[0]);
			result = mManager.writeAnimeDetailsToMAL(ar[0]);
			
			if (result == true)
			{
				ar[0].setDirty(0);
				mManager.insertOrUpdateAnime(ar[0]);
			}
			
			return result;
			

		}
		
	}

	//Dialog returns new value, do something with it
	public void onDialogDismissed(int newValue) {
		if (newValue == Integer.parseInt(mAr.getWatched()))
		{
			
		}
		else
		{
			if (Integer.parseInt(mAr.getTotal()) != 0)
			{
				if (newValue == Integer.parseInt(mAr.getTotal()))
				{
					mAr.setMyStatus(mAr.STATUS_COMPLETED);
				}
				if (newValue == 0)
				{
					mAr.setMyStatus(mAr.STATUS_PLANTOWATCH);
				}
				
				mAr.setEpisodesWatched(newValue);
				mAr.setDirty(1);
				
			}
		}
		
	}
	
	//Create new write task and run it
	public void writeDetails(AnimeRecord ar)
	{
		new writeDetailsTask().execute(ar);
	}
    

}
