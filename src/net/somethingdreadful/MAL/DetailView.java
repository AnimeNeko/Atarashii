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
    MangaRecord mMr;
    String recordType;
   
    DetailsBasicFragment bfrag;
    FragmentManager fm;
    EpisodesPickerDialogFragment epd;
    
    TextView SynopsisView;
    TextView RecordTypeView;
    TextView RecordStatusView;
    TextView MyStatusView;
    TextView ProgressCounterView;
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

		//Get the recordType, also passed from calling activity
		//Record type will determine how the detail view lays out itself
		recordType = getIntent().getStringExtra("net.somethingdreadful.MAL.recordType");
		
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
			showProgressDialog();
			break;
		case R.id.SetStatus_InProgress:
			setStatus(1);
			break;
		case R.id.SetStatus_Complete:
			setStatus(2);
			break;
		case R.id.SetStatus_OnHold:
			setStatus(3);
			break;
		case R.id.SetStatus_Dropped:
			setStatus(4);
			break;
		case R.id.SetStatus_Planned:
			setStatus(5);
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
    	
    	if("anime".equals(recordType))
    	{
    		if (mAr.getDirty() == 1)
    		{
    			writeDetails(mAr);
    		}
    	}
    	else
    	{
    		if (mMr.getDirty() == 1)
    		{
    			writeDetails(mMr);
    		}
    	}
    }
   

    //Called after the basic fragment is finished it's setup, populate data into it
    //Probably no longer necessary, since we no longer instantiate the fragment dynamically
	public void basicFragmentReady() {
		
		CoverImageView = (ImageView) bfrag.getView().findViewById(R.id.detailCoverImage);
		SynopsisView = (TextView) bfrag.getView().findViewById(R.id.Synopsis);
		RecordStatusView = (TextView) bfrag.getView().findViewById(R.id.animeStatusLabel);
		RecordTypeView = (TextView) bfrag.getView().findViewById(R.id.animeTypeLabel);
		MyStatusView = (TextView) bfrag.getView().findViewById(R.id.animeMyStatusLabel);
		ProgressCounterView = (TextView) bfrag.getView().findViewById(R.id.animeEpisodesWatchedCounterLabel);
		getAnimeDetails(recordID);		
	}
	
	public void getAnimeDetails(int id)
	{
		new getDetailsTask().execute();
	}
	
	public void showProgressDialog() // Just a function to keep logic out of the switch statement
	{
		if ("anime".equals(recordType))
		{
			showEpisodesWatchedDialog();
		}
		else
		{
			showMangaProgressDialog();
		}
	}
	
	public void showEpisodesWatchedDialog()
	{
		//Standard code for setting up a dialog fragment
		//Note we use setStyle to change the theme, the default light styled dialog didn't look good so we use the dark dialog
		epd = new EpisodesPickerDialogFragment();
		epd.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
		epd.show(fm, "fragment_EditEpisodesWatchedDialog");
	}
	
	public void showMangaProgressDialog() //TODO Create MangaProgressFragment, will have both chapter and volume pickers
	{
		//Standard code for setting up a dialog fragment
		Toast.makeText(context, "TODO: Make a MangaProgressFragment", Toast.LENGTH_SHORT).show();
	}
	
	public class getDetailsTask extends AsyncTask<Void, Boolean, GenericMALRecord>
	{

		int mID;
		MALManager mmManager;
		ActionBar bar;
		ImageDownloader imageDownloader = new ImageDownloader(context);
		String internalType;
		
		@Override
		protected void onPreExecute()
		{
			mID = recordID;
			mmManager = mManager;
			internalType = recordType;
		}
		
		@Override
		protected GenericMALRecord doInBackground(Void... arg0) {
			
			if ("anime".equals(internalType))
			{
				mAr = mmManager.getAnimeRecordFromDB(mID);
				
				//Basically I just use publishProgress as an easy way to display info we already have loaded sooner
				//This way, I can let the database work happen on the background thread and then immediately display it while
				//the synopsis loads if it hasn't previously been downloaded.
				publishProgress(true);
				
				if (mAr.getSynopsis() == null)
				{
					mAr = mmManager.updateWithDetails(mID, mAr);
				}
				
				return mAr;
			}
			else
			{
				mMr = mmManager.getMangaRecordFromDB(mID);
				
				//Basically I just use publishProgress as an easy way to display info we already have loaded sooner
				//This way, I can let the database work happen on the background thread and then immediately display it while
				//the synopsis loads if it hasn't previously been downloaded.
				publishProgress(true);
				
				if (mMr.getSynopsis() == null)
				{
					mMr = mmManager.updateWithDetails(mID, mMr);
				}
				
				return mMr;
			}
			
		}
		
		@Override
		protected void onProgressUpdate(Boolean... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			
			if ("anime".equals(internalType))
			{
				actionBar.setTitle(mAr.getName());
				
				
				CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
				RecordStatusView.setText(WordUtils.capitalize(mAr.getRecordStatus()));
				RecordTypeView.setText(mAr.getRecordType());
				MyStatusView.setText(WordUtils.capitalize(mAr.getMyStatus()));
				ProgressCounterView.setText(mManager.watchedCounterBuilder(mAr.getPersonalProgress(),
																		Integer.parseInt(mAr.getTotal())));
			}
			else
			{
				actionBar.setTitle(mMr.getName());
				
				//TODO set stuff, related to having a different layout and such
				CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mMr.getImageUrl())));
				
			}
			
			
			//I think there's a potential crash issue here. If the image isn't loaded (ie the user if bloody impatient and clicked
			//something while the picture was still loading), it can crash.
//			((RelativeLayout) bfrag.getView().findViewById(R.id.backgroundContainer))
//				.setBackgroundDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
		}

		@Override
		protected void onPostExecute(GenericMALRecord gr)
		{
			SynopsisView.setText(gr.getSpannedSynopsis(), TextView.BufferType.SPANNABLE);
		}
	}
	
	public class writeDetailsTask extends AsyncTask<GenericMALRecord, Void, Boolean>
	{

		MALManager internalManager;
		GenericMALRecord internalGr;
		String internalType;
		
		@Override
		protected void onPreExecute()
		{
			internalManager = mManager;
			internalType = recordType;

		}
		
		
		@Override
		protected Boolean doInBackground(GenericMALRecord... gr) {
			
			boolean result;
			
			
			if ("anime".equals(internalType))
			{
				internalManager.saveItem((AnimeRecord) gr[0], false);
				result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_ANIME);
			}
			else
			{
				internalManager.saveItem((MangaRecord) gr[0], false);
				result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_MANGA);
			}
			
			
			if (result == true)
			{
				gr[0].setDirty(gr[0].CLEAN);
				
				if ("anime".equals(internalType))
				{
					internalManager.saveItem((AnimeRecord) gr[0], false);
				}
				else
				{
					internalManager.saveItem((MangaRecord) gr[0], false);
				}
			}
			
			return result;
			

		}
		
	}

	//Dialog returns new value, do something with it
	public void onDialogDismissed(int newValue) {
		if ("anime".equals(recordType))
		{
			if (newValue == mAr.getPersonalProgress())
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
				
				ProgressCounterView.setText(mManager.watchedCounterBuilder(newValue, 
																				Integer.parseInt(mAr.getTotal())));
				
			}
		}
		else
		{
			if (newValue == mMr.getPersonalProgress())
			{
				
			}
			else
			{
				if (Integer.parseInt(mMr.getTotal()) != 0)
				{
					if (newValue == Integer.parseInt(mAr.getTotal()))
					{
						mMr.setMyStatus(mMr.STATUS_COMPLETED);
					}
					if (newValue == 0)
					{
						mMr.setMyStatus(mMr.STATUS_PLANTOWATCH);
					}
					
				}
				
				mMr.setChaptersRead(newValue);
				mMr.setDirty(mAr.DIRTY);
				
				ProgressCounterView.setText(mManager.watchedCounterBuilder(newValue, 
																				Integer.parseInt(mMr.getTotal())));
				
			}
		}
		
		
	}
	
	//Create new write task and run it
	public void writeDetails(GenericMALRecord gr)
	{
		new writeDetailsTask().execute(gr);
	}
	
	public void setStatus(int pickValue)
	{
		if ("anime".equals(recordType))
		{
			switch (pickValue)
			{
				case 1:
					setAnimeStatus(mAr.STATUS_WATCHING);
					break;
				case 2:
					setAnimeStatus(mAr.STATUS_COMPLETED);
					break;
				case 3:
					setAnimeStatus(mAr.STATUS_ONHOLD);
					break;
				case 4:
					setAnimeStatus(mAr.STATUS_DROPPED);
					break;
				case 5:
					setAnimeStatus(mAr.STATUS_PLANTOWATCH);
					break;
			}
				
		}
		else
		{
			switch (pickValue)
			{
				case 1:
					setMangaStatus(mMr.STATUS_WATCHING);
					break;
				case 2:
					setMangaStatus(mMr.STATUS_COMPLETED);
					break;
				case 3:
					setMangaStatus(mMr.STATUS_ONHOLD);
					break;
				case 4:
					setMangaStatus(mMr.STATUS_DROPPED);
					break;
				case 5:
					setMangaStatus(mMr.STATUS_PLANTOWATCH);
					break;
			}
		}
	}
    
	public void setAnimeStatus(String status)
	{
		mAr.setMyStatus(status);
		mAr.setDirty(mAr.DIRTY);
		
		MyStatusView.setText(WordUtils.capitalize(status));
	}
	
	public void setMangaStatus(String status)
	{
		mMr.setMyStatus(status);
		mMr.setDirty(mAr.DIRTY);
		
		MyStatusView.setText(WordUtils.capitalize(status));
	}
}
