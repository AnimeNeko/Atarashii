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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang3.text.WordUtils;

public class DetailView extends FragmentActivity implements DetailsBasicFragment.IDetailsBasicAnimeFragment, EpisodesPickerDialogFragment.DialogDismissedListener {

    MALManager mManager;
    Context context;
    int recordID;
    ActionBar actionBar;
    AnimeRecord mAr;
   
    DetailsBasicFragment bfrag;
    FragmentManager fm;
    EpisodesPickerDialogFragment epd;
    
    TextView ItemTitleView;
    TextView SynopsisView;
    TextView AnimeTypeView;
    TextView AnimeStatusView;
    TextView MyStatusView;
    TextView EpisodesWatchedCounterView;
    ImageView CoverImageView;

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
		case R.id.SetStatus_Watching:
			setAnimeStatus(mAr.STATUS_WATCHING);
			break;
		case R.id.SetStatus_Complete:
			setAnimeStatus(mAr.STATUS_COMPLETED);
			break;
		case R.id.SetStatus_OnHold:
			setAnimeStatus(mAr.STATUS_ONHOLD);
			break;
		case R.id.SetStatus_Dropped:
			setAnimeStatus(mAr.STATUS_DROPPED);
			break;
		case R.id.SetStatus_PlanToWatch:
			setAnimeStatus(mAr.STATUS_PLANTOWATCH);
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
		
		CoverImageView = (ImageView) bfrag.getView().findViewById(R.id.detailCoverImage);
		ItemTitleView = (TextView) bfrag.getView().findViewById(R.id.itemTitle);
		SynopsisView = (TextView) bfrag.getView().findViewById(R.id.Synopsis);
		AnimeStatusView = (TextView) bfrag.getView().findViewById(R.id.animeStatusLabel);
		AnimeTypeView = (TextView) bfrag.getView().findViewById(R.id.animeTypeLabel);
		MyStatusView = (TextView) bfrag.getView().findViewById(R.id.animeMyStatusLabel);
		EpisodesWatchedCounterView = (TextView) bfrag.getView().findViewById(R.id.animeEpisodesWatchedCounterLabel);
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
			
			
			CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
			ItemTitleView.setText(mAr.getName());
			AnimeStatusView.setText(WordUtils.capitalize(mAr.getRecordStatus()));
			AnimeTypeView.setText(mAr.getRecordType());
			MyStatusView.setText(WordUtils.capitalize(mAr.getMyStatus()));
			EpisodesWatchedCounterView.setText(mManager.watchedCounterBuilder(Integer.parseInt(mAr.getWatched()),
																	Integer.parseInt(mAr.getTotal())));
			
			
			//I think there's a potential crash issue here. If the image isn't loaded (ie the user if bloody impatient and clicked
			//something while the picture was still loading), it can crash.
//			((RelativeLayout) bfrag.getView().findViewById(R.id.backgroundContainer))
//				.setBackgroundDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
		}

		@Override
		protected void onPostExecute(AnimeRecord ar)
		{
			SynopsisView.setText(ar.getSpannedSynopsis(), TextView.BufferType.SPANNABLE);
		}
	}
	
	public class writeDetailsTask extends AsyncTask<AnimeRecord, Void, Boolean>
	{

		MALManager internalManager;
		
		@Override
		protected void onPreExecute()
		{
			internalManager = mManager;
		}
		
		
		@Override
		protected Boolean doInBackground(AnimeRecord... ar) {
			
			boolean result;
			
			internalManager.insertOrUpdateAnime(ar[0], false);
			result = internalManager.writeAnimeDetailsToMAL(ar[0]);
			
			if (result == true)
			{
				ar[0].setDirty(ar[0].CLEAN);
				internalManager.insertOrUpdateAnime(ar[0], false);
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
				
			}
			
			mAr.setEpisodesWatched(newValue);
			mAr.setDirty(mAr.DIRTY);
			
			EpisodesWatchedCounterView.setText(mManager.watchedCounterBuilder(newValue, 
																			Integer.parseInt(mAr.getTotal())));
			
		}
		
	}
	
	//Create new write task and run it
	public void writeDetails(AnimeRecord ar)
	{
		new writeDetailsTask().execute(ar);
	}
    
	public void setAnimeStatus(String status)
	{
		mAr.setMyStatus(status);
		mAr.setDirty(mAr.DIRTY);
		
		MyStatusView.setText(status.toUpperCase());
	}
}
