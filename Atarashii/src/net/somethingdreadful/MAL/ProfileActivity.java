package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.record.ProfileMALRecord;
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
import com.actionbarsherlock.view.MenuItem;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends SherlockFragmentActivity   {
Context context;
ImageView Image;
PrefManager prefs; 
LinearLayout animecard;
LinearLayout mangacard;

boolean forcesync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        context = getApplicationContext();
        ProfileMALRecord.context = getApplicationContext(); //ProfileMALRecord has a static record!
        prefs = new PrefManager(context);
        animecard =(LinearLayout)findViewById(R.id.Anime_card);
        mangacard =(LinearLayout)findViewById(R.id.Manga_card);
        setTitle("User profile"); //set title

        card(); //check the settings
        new Startparse().execute(); // send url to the background
        
        TextView tv25 = (TextView) findViewById(R.id.websitesmall);
    	tv25.setOnClickListener(new View.OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
    	    	Uri webstiteclick = Uri.parse(ProfileMALRecord.website);
            	startActivity(new Intent(Intent.ACTION_VIEW, webstiteclick));
    	    }
    	});
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.forceSync:
            	if (isNetworkAvailable()){
            		forcesync = true;
            		new Startparse().execute();
            	}else{
            		Crouton.makeText(this, "No network connection available!", Style.ALERT).show();
            	}
                break;
            case R.id.action_ViewMALPage:
            	Uri malurl = Uri.parse("http://myanimelist.net/profile/" + ProfileMALRecord.username);
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
    	if (prefs.anime_manga_zero() && ProfileMALRecord.M_total_entries < 1){ //if manga (total entry) is beneath the int then hide
    		mangacard.setVisibility(View.GONE);
    	}
    	if (prefs.anime_manga_zero() && ProfileMALRecord.A_total_entries < 1){ //if anime (total entry) is beneath the int then hide
    		animecard.setVisibility(View.GONE);
    	}
    	TextView namecard = (TextView) findViewById(R.id.name_text);
    	namecard.setText(ProfileMALRecord.username);
    }
    
    public boolean isConnectedWifi() {
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo Wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (Wifi.isConnected() && prefs.Wifisyncdisable()) {
            return true;
        } else {
            return false;
        }
    }
    
    public void setcolor(){
    	TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
    	String name = ProfileMALRecord.username;
    	if (prefs.Textcolordisable() == false){
    		setcolor(true);
    		setcolor(false);
    		if (ProfileMALRecord.access_rank.contains("Administrator")){
    			tv8.setTextColor(Color.parseColor("#850000"));
    		}else if (ProfileMALRecord.access_rank.contains("Moderator")) {
    			tv8.setTextColor(Color.parseColor("#003385"));
    		}else if (ProfileMALRecord.Developerrecord(name)) {
    				tv8.setTextColor(Color.parseColor("#008583")); //Developer
    		}else{
    				tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
    		}    
    		TextView tv11 = (TextView) findViewById(R.id.websitesmall);
    		tv11.setTextColor(Color.parseColor("#002EAB"));
    	}
    	if (ProfileMALRecord.Developerrecord(name)) {
			tv8.setText("Atarashii developer"); //Developer
		}
    }
    
    public void Share(boolean anime) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        if (anime == true){
        	sharingIntent.putExtra(Intent.EXTRA_TEXT, ProfileMALRecord.username + " shared an anime list using Atarashii : http://myanimelist.net/animelist/" + ProfileMALRecord.username + "!");
        }else{
        	sharingIntent.putExtra(Intent.EXTRA_TEXT, ProfileMALRecord.username + " shared a manga list using Atarashii : http://myanimelist.net/mangalist/" + ProfileMALRecord.username + "!");
        }
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
    
    public void setcolor(boolean type){
    	int time = 0;
    	TextView textview = null;
    	if (type){ // true = anime, else = manga
    		textview = (TextView) findViewById(R.id.atimedayssmall); //anime
    		time= ProfileMALRecord.A_time_daysint / 3;
    	}else{
    		textview = (TextView) findViewById(R.id.mtimedayssmall); // manga
    		time= ProfileMALRecord.M_time_daysint / 2;
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
    	if (ProfileMALRecord.birthday.equals("null")){
    		tv1.setText("Not specified");
    	}else{
    		tv1.setText(ProfileMALRecord.birthday);
    	}
		TextView tv2 = (TextView) findViewById(R.id.locationsmall);
		if (ProfileMALRecord.location.equals("null")){
    		tv2.setText("Not specified");
    	}else{
    		tv2.setText(ProfileMALRecord.location);
    	}
		TextView tv25 = (TextView) findViewById(R.id.websitesmall);
		TextView tv26 = (TextView) findViewById(R.id.websitefront);
		LinearLayout tv36 = (LinearLayout) findViewById(R.id.details_card);
		if (ProfileMALRecord.website.contains("http://") && ProfileMALRecord.website.contains(".")){ // filter fake websites
    		tv25.setText(ProfileMALRecord.website);
    	}else{
    		tv25.setVisibility(View.GONE);
    		tv26.setVisibility(View.GONE);
    	}
		TextView tv3 = (TextView) findViewById(R.id.commentspostssmall);
		tv3.setText(Integer.toString(ProfileMALRecord.comments));
		TextView tv4 = (TextView) findViewById(R.id.forumpostssmall);
		tv4.setText(Integer.toString(ProfileMALRecord.forum_posts));
		TextView tv5 = (TextView) findViewById(R.id.lastonlinesmall);
		tv5.setText(ProfileMALRecord.last_online);
		TextView tv6 = (TextView) findViewById(R.id.gendersmall);
		tv6.setText(ProfileMALRecord.gender);
		TextView tv7 = (TextView) findViewById(R.id.joindatesmall);
		tv7.setText(ProfileMALRecord.join_date);
		TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
		tv8.setText(ProfileMALRecord.access_rank);
		TextView tv9 = (TextView) findViewById(R.id.animelistviewssmall);
		tv9.setText(Integer.toString(ProfileMALRecord.anime_list_views));
		TextView tv10 = (TextView) findViewById(R.id.mangalistviewssmall);
		tv10.setText(Integer.toString(ProfileMALRecord.manga_list_views));
		
		TextView tv11 = (TextView) findViewById(R.id.atimedayssmall);
		tv11.setText(ProfileMALRecord.A_time_days);
		TextView tv12 = (TextView) findViewById(R.id.awatchingsmall);
		tv12.setText(Integer.toString(ProfileMALRecord.A_watching));
		TextView tv13 = (TextView) findViewById(R.id.acompletedpostssmall);
		tv13.setText(Integer.toString(ProfileMALRecord.A_completed));
		TextView tv14 = (TextView) findViewById(R.id.aonholdsmall);
		tv14.setText(Integer.toString(ProfileMALRecord.A_on_hold));
		TextView tv15 = (TextView) findViewById(R.id.adroppedsmall);
		tv15.setText(Integer.toString(ProfileMALRecord.A_dropped));
		TextView tv16 = (TextView) findViewById(R.id.aplantowatchsmall);
		tv16.setText(Integer.toString(ProfileMALRecord.A_plan_to_watch));
		TextView tv17 = (TextView) findViewById(R.id.atotalentriessmall);
		tv17.setText(Integer.toString(ProfileMALRecord.A_total_entries));
		
		TextView tv18 = (TextView) findViewById(R.id.mtimedayssmall);
		tv18.setText(ProfileMALRecord.M_time_days);
		TextView tv19 = (TextView) findViewById(R.id.mwatchingsmall);
		tv19.setText(Integer.toString(ProfileMALRecord.M_reading));
		TextView tv20 = (TextView) findViewById(R.id.mcompletedpostssmall);
		tv20.setText(Integer.toString(ProfileMALRecord.M_completed));
		TextView tv21 = (TextView) findViewById(R.id.monholdsmall);
		tv21.setText(Integer.toString(ProfileMALRecord.M_on_hold));
		TextView tv22 = (TextView) findViewById(R.id.mdroppedsmall);
		tv22.setText(Integer.toString(ProfileMALRecord.M_dropped));
		TextView tv23 = (TextView) findViewById(R.id.mplantowatchsmall);
		tv23.setText(Integer.toString(ProfileMALRecord.M_plan_to_read));
		TextView tv24 = (TextView) findViewById(R.id.mtotalentriessmall);
		tv24.setText(Integer.toString(ProfileMALRecord.M_total_entries));
		
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
    
    public class Startparse extends AsyncTask<String, Void, String> {
    	protected String doInBackground(String... urls) {
    		if (isConnectedWifi() && prefs.Wifisyncdisable() || forcesync == true || !prefs.Wifisyncdisable() && isNetworkAvailable()){ // settings check
        		MALApi api = new MALApi(context);
        		ProfileMALRecord.Grabrecord(api.getProfile(ProfileMALRecord.username));
    		}
    		forcesync = false;
            return "";
        }

        protected void onProgressUpdate(Void... progress) {
        }

		protected void onPostExecute(String check) {
			if (ProfileMALRecord.avatar_url == ""){ //IF MAL IS OFFLINE THIS WILL START OFFLINE.
				ProfileMALRecord.Loadrecord();
		    	if (ProfileMALRecord.avatar_url==""){
		    		maketext("No offline record available!",2 );
		    	}else{
		    		Settext();
		    	}
			}else{
				Settext();
				ProfileMALRecord.Saverecord();
			}
			try{
				Picasso.with(context).load(ProfileMALRecord.avatar_url)
					.error(R.drawable.cover_error)
					.placeholder(R.drawable.cover_loading)
					.into((ImageView) findViewById(R.id.Image));
				card();
				setcolor();
			}catch(Exception e){
				e.printStackTrace();
			}
        }
    }
    
	void choosedialog(final boolean share){ //as the name says
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (share == true){
			builder.setTitle("Share");
			builder.setMessage("Which list do you want to share?");
		}else{
			builder.setTitle("View");
			builder.setMessage("Which list do you want to view?");
		}

		builder.setPositiveButton("My animelist", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        if (share == true){
		        	Share(true);
		        }else{
		        	Uri mallisturlanime = Uri.parse("http://myanimelist.net/animelist/" + ProfileMALRecord.username);
	            	startActivity(new Intent(Intent.ACTION_VIEW, mallisturlanime));
		        }
		    }
		});
		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    }
		});
		builder.setNegativeButton("My mangalist", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	if (share == true){
		        	Share(false);
		        }else{
		        	Uri mallisturlmanga = Uri.parse("http://myanimelist.net/mangalist/" + ProfileMALRecord.username);
	            	startActivity(new Intent(Intent.ACTION_VIEW, mallisturlmanga));
		        }
		    }
		});
		builder.show();
	}
}
