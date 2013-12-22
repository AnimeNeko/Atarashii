package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.record.UserRecord;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends SherlockFragmentActivity   {
MALManager mManager;
Context context;
ImageView Image;
PrefManager prefs; 
LinearLayout animecard;
LinearLayout mangacard;
UserRecord record;

boolean forcesync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        context = getApplicationContext();
        mManager = new MALManager(this.context);
        prefs = new PrefManager(context);
        animecard =(LinearLayout)findViewById(R.id.Anime_card);
        mangacard =(LinearLayout)findViewById(R.id.Manga_card);
        setTitle(R.string.title_activity_profile); //set title

        new getProfileRecordsTask().execute(); // send url to the background
        
        TextView tv25 = (TextView) findViewById(R.id.websitesmall);
    	tv25.setOnClickListener(new View.OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
    	    	Uri webstiteclick = Uri.parse(record.getWebsite());
            	startActivity(new Intent(Intent.ACTION_VIEW, webstiteclick));
    	    }
    	});
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_profile_view, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.forceSync:
            	if (isNetworkAvailable()){
            		Crouton.makeText(this, R.string.crouton_SyncMessage, Style.INFO).show();
            		forcesync = true;
            		new getProfileRecordsTask().execute();
            	}else{
            		Crouton.makeText(this, R.string.crouton_noConnectivity, Style.ALERT).show();
            	}
                break;
            case R.id.action_ViewMALPage:
            	Uri malurl = Uri.parse("http://myanimelist.net/profile/" + UserRecord.username);
            	startActivity(new Intent(Intent.ACTION_VIEW, malurl));
                break;
            case R.id.View:
            	choosedialog(false);
                break;
            case R.id.Share:
            	choosedialog(true);
        }
        return true;
    }
    
    public void maketext(String string ,int type) { //for the private class, to make still text on errors...
		if (type==1){
			Crouton.makeText(this, string, Style.INFO).show();
		}else if (type==2){
			Crouton.makeText(this, string, Style.ALERT).show();
		}else{
			Crouton.makeText(this, string, Style.CONFIRM).show();
		}
    }
    
    public boolean isNetworkAvailable() { //check if network is available
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    
    public void card() { //settings for hide a card and text userprofile
    	if (prefs.animehide()){
    		animecard.setVisibility(View.GONE);
    	}
    	if (prefs.mangahide()){
    		mangacard.setVisibility(View.GONE);
    	}
    	if (prefs.anime_manga_zero() && record.getMangaTotalEntries() < 1){ //if manga (total entry) is beneath the int then hide
    		mangacard.setVisibility(View.GONE);
    	}
    	if (prefs.anime_manga_zero() && record.getAnimeTotalEntries() < 1){ //if anime (total entry) is beneath the int then hide
    		animecard.setVisibility(View.GONE);
    	}
    	TextView namecard = (TextView) findViewById(R.id.name_text);
    	namecard.setText(UserRecord.username);
    }
    
    public void setcolor(){
    	TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
    	String name = UserRecord.username;
    	if (prefs.Textcolordisable() == false){
    		setcolor(true);
    		setcolor(false);
    		if (record.getAccessRank().contains("Administrator")){
    			tv8.setTextColor(Color.parseColor("#850000"));
    		}else if (record.getAccessRank().contains("Moderator")) {
    			tv8.setTextColor(Color.parseColor("#003385"));
    		}else if (UserRecord.developerRecord(name)) {
    				tv8.setTextColor(Color.parseColor("#008583")); //Developer
    		}else{
    				tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
    		}    
    		TextView tv11 = (TextView) findViewById(R.id.websitesmall);
    		tv11.setTextColor(Color.parseColor("#002EAB"));
    	}
    	if (UserRecord.developerRecord(name)) {
			tv8.setText("Atarashii developer"); //Developer
		}
    }
    
    public void Share(boolean anime) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        if (anime == true){
        	sharingIntent.putExtra(Intent.EXTRA_TEXT, UserRecord.username + " " + R.string.share_animelist + UserRecord.username + "!");
        }else{
        	sharingIntent.putExtra(Intent.EXTRA_TEXT, UserRecord.username + " " + R.string.share_mangalist + UserRecord.username + "!");
        }
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
    
    public void setcolor(boolean type){
    	int time = 0;
    	TextView textview = null;
    	if (type){ // true = anime, else = manga
    		textview = (TextView) findViewById(R.id.atimedayssmall); //anime
    		time= record.getAnimeTimedays() / 3;
    	}else{
    		textview = (TextView) findViewById(R.id.mtimedayssmall); // manga
    		time= record.getMangaTimedays() / 2;
    	}
    	if (time <= 0){
    		textview.setTextColor(Color.parseColor("#CF0404"));
    	} else if (time <=2){
    		textview.setTextColor(Color.parseColor("#CF1F04"));
    	} else if (time <= 3){
    		textview.setTextColor(Color.parseColor("#CF3304"));
    	} else if (time <= 4){
    		textview.setTextColor(Color.parseColor("#CF5204"));
    	} else if (time <= 5){
    		textview.setTextColor(Color.parseColor("#CF7004"));
    	} else if (time <= 6){
    		textview.setTextColor(Color.parseColor("#CF8E04"));
    	} else if (time <= 7){
    		textview.setTextColor(Color.parseColor("#CFB704"));
    	} else if (time <= 8){
    		textview.setTextColor(Color.parseColor("#C4CF04"));
    	} else if (time <= 9){
    		textview.setTextColor(Color.parseColor("#B4CF04"));
    	} else if (time <= 10){
    		textview.setTextColor(Color.parseColor("#ADCF04"));
    	} else if (time <= 11){
    		textview.setTextColor(Color.parseColor("#A3CF04"));
    	} else if (time <= 12){
    		textview.setTextColor(Color.parseColor("#95CF04"));
    	} else if (time <= 13){
    		textview.setTextColor(Color.parseColor("#7ECF04"));
    	} else if (time <= 14){
    		textview.setTextColor(Color.parseColor("#66CF04"));
    	} else if (time <= 15){
    		textview.setTextColor(Color.parseColor("#55CF04"));
    	} else if (time <= 16){
    		textview.setTextColor(Color.parseColor("#44CF04"));
    	} else if (time <= 17){
    		textview.setTextColor(Color.parseColor("#2DCF04"));
    	} else if (time <= 18){
    		textview.setTextColor(Color.parseColor("#18CF04"));
    	} else if (time <= 19){
    		textview.setTextColor(Color.parseColor("#04CF04"));
    	} else if (time <= 20){
    		textview.setTextColor(Color.parseColor("#04CF15"));
    	} else if (time <= 21){
    		textview.setTextColor(Color.parseColor("#00AB2B"));
    	}
    }
    
    public void Settext(){
    	TextView tv1 = (TextView) findViewById(R.id.birthdaysmall);
    	if (record.getBirthday().equals("null")){
    		tv1.setText("Not specified");
    	}else{
    		tv1.setText(record.getBirthday());
    	}
		TextView tv2 = (TextView) findViewById(R.id.locationsmall);
		if (record.getLocation().equals("null")){
    		tv2.setText("Not specified");
    	}else{
    		tv2.setText(record.getLocation());
    	}
		TextView tv25 = (TextView) findViewById(R.id.websitesmall);
		TextView tv26 = (TextView) findViewById(R.id.websitefront);
		LinearLayout tv36 = (LinearLayout) findViewById(R.id.details_card);
		if (record.getWebsite().contains("http://") && record.getWebsite().contains(".")){ // filter fake websites
    		tv25.setText(record.getWebsite());
    	}else{
    		tv25.setVisibility(View.GONE);
    		tv26.setVisibility(View.GONE);
    	}
		TextView tv3 = (TextView) findViewById(R.id.commentspostssmall);
		tv3.setText(record.getComments().toString());
		TextView tv4 = (TextView) findViewById(R.id.forumpostssmall);
		tv4.setText(record.getForumposts().toString());
		TextView tv5 = (TextView) findViewById(R.id.lastonlinesmall);
		tv5.setText(record.getLast());
		TextView tv6 = (TextView) findViewById(R.id.gendersmall);
		tv6.setText(record.getGender());
		TextView tv7 = (TextView) findViewById(R.id.joindatesmall);
		tv7.setText(record.getJoinDate());
		TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
		tv8.setText(record.getAccessRank());
		TextView tv9 = (TextView) findViewById(R.id.animelistviewssmall);
		tv9.setText(record.getAnimeListviews().toString());
		TextView tv10 = (TextView) findViewById(R.id.mangalistviewssmall);
		tv10.setText(record.getMangaListviews().toString());
		
		TextView tv11 = (TextView) findViewById(R.id.atimedayssmall);
		tv11.setText(record.getAnimeTimeDaysD().toString());
		TextView tv12 = (TextView) findViewById(R.id.awatchingsmall);
		tv12.setText(record.getAnimeWatching().toString());
		TextView tv13 = (TextView) findViewById(R.id.acompletedpostssmall);
		tv13.setText(record.getAnimeCompleted().toString());
		TextView tv14 = (TextView) findViewById(R.id.aonholdsmall);
		tv14.setText(record.getAnimeOnHold().toString());
		TextView tv15 = (TextView) findViewById(R.id.adroppedsmall);
		tv15.setText(record.getAnimeDropped().toString());
		TextView tv16 = (TextView) findViewById(R.id.aplantowatchsmall);
		tv16.setText(record.getAnimePlanToWatch().toString());
		TextView tv17 = (TextView) findViewById(R.id.atotalentriessmall);
		tv17.setText(record.getAnimeTotalEntries().toString());
		
		TextView tv18 = (TextView) findViewById(R.id.mtimedayssmall);
		tv18.setText(record.getMangatimedaysD().toString());
		TextView tv19 = (TextView) findViewById(R.id.mwatchingsmall);
		tv19.setText(record.getMangaReading().toString());
		TextView tv20 = (TextView) findViewById(R.id.mcompletedpostssmall);
		tv20.setText(record.getMangaCompleted().toString());
		TextView tv21 = (TextView) findViewById(R.id.monholdsmall);
		tv21.setText(record.getMangaOnHold().toString());
		TextView tv22 = (TextView) findViewById(R.id.mdroppedsmall);
		tv22.setText(record.getMangaDropped().toString());
		TextView tv23 = (TextView) findViewById(R.id.mplantowatchsmall);
		tv23.setText(record.getMangaPlanToRead().toString());
		TextView tv24 = (TextView) findViewById(R.id.mtotalentriessmall);
		tv24.setText(record.getMangaTotalEntries().toString());
		
		if (tv36.getWidth()- tv25.getWidth() - tv25.getWidth() < 265){
			tv25.setTextSize(14);
		}
		if (tv36.getWidth()- tv25.getWidth() - tv25.getWidth() < 265 && tv25.getTextSize() == 14){
			tv25.setTextSize(12);
		}
		if (tv36.getWidth()- tv25.getWidth() - tv25.getWidth() < 265 && tv25.getTextSize() == 12){
			tv25.setTextSize(10);
		}
		if (tv36.getWidth()- tv25.getWidth() - tv25.getWidth() < 265 && tv25.getTextSize() == 10){
			tv25.setTextSize(8);
		}
    }
    
    public void refresh(Boolean crouton){
    	if (crouton == true){
			Crouton.makeText(this, R.string.crouton_UserRecord_updated, Style.CONFIRM).show();
    	}
    	if (record == null){
    		if (!isNetworkAvailable()){
    			Crouton.makeText(this, R.string.crouton_noUserRecord , Style.ALERT).show();
    		}
    	}else{
			Picasso.with(context).load(record.getAvatar())
				.error(R.drawable.cover_error)
				.placeholder(R.drawable.cover_loading)
				.into((ImageView) findViewById(R.id.Image));
			card();
			Settext();
			setcolor();
    	}
    }
    
	  public class getProfileRecordsTask extends AsyncTask<String, Void, ArrayList<UserRecord>> {
		  Boolean download = false;
		  
		  @Override
		  protected ArrayList<UserRecord> doInBackground(String... user) {
			 
			  if (forcesync == true){
				  mManager.downloadAndStoreProfile(UserRecord.username);
				  forcesync = false;
				  download = true;
			  }
			  record = mManager.getProfileRecordsFromDB();
			  if (record == null && isNetworkAvailable()){
	        	  mManager.downloadAndStoreProfile(UserRecord.username);
	        	  record = mManager.getProfileRecordsFromDB();
	          }
	          return null;
	      }

	      @Override
	      protected void onPostExecute(ArrayList<UserRecord> result) {
	    	  if (download == true){
	    		  refresh(true);
	    	  }else{
	    		  refresh(false);
	    	  }
	      }
	  }
    
	void choosedialog(final boolean share){ //as the name says
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (share == true){
			builder.setTitle(R.string.share_title);
			builder.setMessage(R.string.share_message);
		}else{
			builder.setTitle(R.string.view_title);
			builder.setMessage(R.string.view_message);
		}

		builder.setPositiveButton(R.string.dialog_label_anime, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        if (share == true){
		        	Share(true);
		        }else{
		        	Uri mallisturlanime = Uri.parse("http://myanimelist.net/animelist/" + UserRecord.username);
	            	startActivity(new Intent(Intent.ACTION_VIEW, mallisturlanime));
		        }
		    }
		});
		builder.setNeutralButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    }
		});
		builder.setNegativeButton(R.string.dialog_label_manga, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	if (share == true){
		        	Share(false);
		        }else{
		        	Uri mallisturlmanga = Uri.parse("http://myanimelist.net/mangalist/" + UserRecord.username);
	            	startActivity(new Intent(Intent.ACTION_VIEW, mallisturlmanga));
		        }
		    }
		});
		builder.show();
	}
}
