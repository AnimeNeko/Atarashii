package net.somethingdreadful.MAL;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends SherlockFragmentActivity   {
Context context;
String name;
ImageView Imagdae;
boolean forcesync = false;
SharedPreferences profielprefs;
PrefManager prefs; 
LinearLayout a;
LinearLayout m;

//details
String avatar_url = "";
String birthday = ""; 
String location = "";
Integer comments = 0;
Integer forum_posts = 0;
String last_online = "";
String gender = "";
String join_date = "";
String access_rank = "";
Integer anime_list_views = 0;
Integer manga_list_views = 0;
//anime details
String A_time_days = "0";
Integer A_time_daysint = 0;
Integer A_watching = 0;
Integer A_completed = 0;
Integer A_on_hold = 0;
Integer A_dropped = 0;
Integer A_plan_to_watch = 0;
Integer A_total_entries = 0;
//manga details
String M_time_days = "0";
Integer M_time_daysint = 0;
Integer M_reading = 0;
Integer M_completed = 0;
Integer M_on_hold = 0;
Integer M_dropped = 0;
Integer M_plan_to_read = 0;
Integer M_total_entries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        context = getApplicationContext();
        prefs = new PrefManager(context);
        name = prefs.getUser();
        a =(LinearLayout)findViewById(R.id.Anime_card);
		m =(LinearLayout)findViewById(R.id.Manga_card);
        String userclicked = prefs.Getclickeduser();

        if (!name.equals(userclicked)){ //get username from friedlist/navdrawer
        	name = userclicked;
        }
        profielprefs = getSharedPreferences("Profile_" + name, MODE_PRIVATE);
        setTitle("User profile of " + name); //set title
        
        card(); //check the settings
        new RetrieveMessages().execute("http://mal-api.com/profile/" + name); // send url to the background
    }
    
    @Override
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
            		new RetrieveMessages().execute("http://mal-api.com/profile/" + name); // send url to the background
            		forcesync = true;
            	}else{
            		Crouton.makeText(this, "No network connection available!", Style.ALERT).show();
            	}
                break;
            case R.id.action_ViewMALPage:
            	Uri malurl = Uri.parse("http://myanimelist.net/profile/" + name);
            	startActivity(new Intent(Intent.ACTION_VIEW, malurl));
                break;
            case R.id.viewmala:
            	Uri mallisturlanime = Uri.parse("http://myanimelist.net/animelist/" + name);
            	startActivity(new Intent(Intent.ACTION_VIEW, mallisturlanime));
                break;
            case R.id.viewmalm:
            	Uri mallisturlmanga = Uri.parse("http://myanimelist.net/mangalist/" + name);
            	startActivity(new Intent(Intent.ACTION_VIEW, mallisturlmanga));
                break;
            case R.id.Shareprofile:
				Share();
        }
        return true;
    }
    
    public class RetrieveMessages extends AsyncTask<String, Void, String> {
    	protected String doInBackground(String... urls) {
    		if (isConnectedWifi() && prefs.autosync() || forcesync == true || !prefs.Wifisyncdisable() && isNetworkAvailable() && prefs.autosync()){ // settings check
    			HttpClient client = new DefaultHttpClient();
    			String json = "";
    			try {
    				String line = "";
    				HttpGet request = new HttpGet(urls[0]);
    				HttpResponse response = client.execute(request);//get response
    				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    				while ((line = rd.readLine()) != null) {
    					json += line + System.getProperty("line.separator"); //save response
    					JSONObject jsonObject = new JSONObject(json);
    					avatar_url = jsonObject.getString("avatar_url");
    					birthday = jsonObject.getJSONObject("details").getString("birthday"); // get birthday for check
    					location = jsonObject.getJSONObject("details").getString("location");
    					comments = jsonObject.getJSONObject("details").getInt("comments");
    					forum_posts = jsonObject.getJSONObject("details").getInt("forum_posts");
    					last_online = jsonObject.getJSONObject("details").getString("last_online");
    					gender = jsonObject.getJSONObject("details").getString("gender");
    					join_date = jsonObject.getJSONObject("details").getString("join_date");
    					access_rank = jsonObject.getJSONObject("details").getString("access_rank");
    					anime_list_views = jsonObject.getJSONObject("details").getInt("anime_list_views");
    					manga_list_views = jsonObject.getJSONObject("details").getInt("manga_list_views");
						
    					A_time_days = Double.toString(jsonObject.getJSONObject("anime_stats").getDouble("time_days"));
    					A_time_daysint = jsonObject.getJSONObject("anime_stats").getInt("time_days");//get int for colors
    					A_watching = jsonObject.getJSONObject("anime_stats").getInt("watching");
    					A_completed = jsonObject.getJSONObject("anime_stats").getInt("completed");
    					A_on_hold = jsonObject.getJSONObject("anime_stats").getInt("on_hold");
    					A_dropped = jsonObject.getJSONObject("anime_stats").getInt("dropped");
    					A_plan_to_watch = jsonObject.getJSONObject("anime_stats").getInt("plan_to_watch");
    					A_total_entries = jsonObject.getJSONObject("anime_stats").getInt("total_entries");
						
    					M_time_days = Double.toString(jsonObject.getJSONObject("manga_stats").getDouble("time_days"));
    					M_time_daysint = jsonObject.getJSONObject("manga_stats").getInt("time_days"); //get int for colors
						M_reading = jsonObject.getJSONObject("manga_stats").getInt("reading");
						M_completed = jsonObject.getJSONObject("manga_stats").getInt("completed");
						M_on_hold = jsonObject.getJSONObject("manga_stats").getInt("on_hold");
						M_dropped = jsonObject.getJSONObject("manga_stats").getInt("dropped");
						M_plan_to_read = jsonObject.getJSONObject("manga_stats").getInt("plan_to_read");
						M_total_entries = jsonObject.getJSONObject("manga_stats").getInt("total_entries");
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    		forcesync = false;
            return birthday;
        }

        protected void onProgressUpdate(Void... progress) {
        }

		protected void onPostExecute(String check) {
			if (check == ""){ //birthday check, IF MAL IS OFFLINE THIS WILL START OFFLINE.
				Offline();
			}else{
				Save();
			}
			Picasso ProfileImage = Picasso.with(context);
			ProfileImage.load(avatar_url).error(R.drawable.cover_error).into((ImageView) findViewById(R.id.Imagdae));
			autohidecard();
			setcolor();
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
    
    public void card() { //settings for hide a card
    	if (prefs.animehide()){
    		a.setVisibility(View.GONE);
    	}
    	if (prefs.mangahide()){
    		m.setVisibility(View.GONE);
    	}
    }
    
    public void autohidecard(){//settings for hide auto a card
    	if (prefs.anime_manga_zero() && M_total_entries < 1){ //if manga (total entry) is beneath the int then hide
    		m.setVisibility(View.GONE);
    	}
    	if (prefs.anime_manga_zero() && A_total_entries < 1){ //if anime (total entry) is beneath the int then hide
    		a.setVisibility(View.GONE);
    	}
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
    	if (prefs.Textcolordisable() == false){
    		setcoloranime();
    		if (access_rank.contains("Administrator")){
    			tv8.setTextColor(Color.parseColor("#850000"));
    		}else if (access_rank.contains("Moderator")) {
    			tv8.setTextColor(Color.parseColor("#003385"));
    		}else if (name.equals("Ratan12") || name.equals("AnimaSA") || name.equals("Motokochan") || name.equals("Apkawa") || name.equals("ratan12") || name.equals("animaSA") || name.equals("motokochan") || name.equals("apkawa")) {
    				tv8.setTextColor(Color.parseColor("#008583")); //Developer
    		}else{
    				tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
    		}    		
    	}
    	if (name.equals("Ratan12") || name.equals("AnimaSA") || name.equals("Motokochan") || name.equals("Apkawa") || name.equals("ratan12") || name.equals("animaSA") || name.equals("motokochan") || name.equals("apkawa")) {
			tv8.setText("Atarashii developer"); //Developer
		}
    }
    
    public void Share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, name + " has shared an anime list using Atarashii : http://myanimelist.net/animelist/" + name + "!");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
    
    public void setcoloranime(){
    	TextView tv11 = (TextView) findViewById(R.id.atimedayssmall); //anime
    	if (A_time_daysint >= 0 && A_time_daysint <= 3){
    		tv11.setTextColor(Color.parseColor("#CF0404"));
    	} else if (A_time_daysint >= 4 && A_time_daysint <= 7){
    		tv11.setTextColor(Color.parseColor("#CF1F04"));
    	} else if (A_time_daysint >= 8 && A_time_daysint <= 11){
    		tv11.setTextColor(Color.parseColor("#CF3304"));
    	} else if (A_time_daysint >= 12 && A_time_daysint <= 15){
    		tv11.setTextColor(Color.parseColor("#CF5204"));
    	} else if (A_time_daysint >= 16 && A_time_daysint <= 19){
    		tv11.setTextColor(Color.parseColor("#CF7004"));
    	} else if (A_time_daysint >= 20 && A_time_daysint <= 23){
    		tv11.setTextColor(Color.parseColor("#CF8E04"));
    	} else if (A_time_daysint >= 24 && A_time_daysint <= 27){
    		tv11.setTextColor(Color.parseColor("#CFB704"));
    	} else if (A_time_daysint >= 28 && A_time_daysint <= 31){
    		tv11.setTextColor(Color.parseColor("#C4CF04"));
    	} else if (A_time_daysint >= 32 && A_time_daysint <= 35){
    		tv11.setTextColor(Color.parseColor("#B4CF04"));
    	} else if (A_time_daysint >= 36 && A_time_daysint <= 40){
    		tv11.setTextColor(Color.parseColor("#ADCF04"));
    	} else if (A_time_daysint >= 41 && A_time_daysint <= 45){
    		tv11.setTextColor(Color.parseColor("#A3CF04"));
    	} else if (A_time_daysint >= 45 && A_time_daysint <= 49){
    		tv11.setTextColor(Color.parseColor("#95CF04"));
    	} else if (A_time_daysint >= 50 && A_time_daysint <= 54){
    		tv11.setTextColor(Color.parseColor("#7ECF04"));
    	} else if (A_time_daysint >= 55 && A_time_daysint <= 59){
    		tv11.setTextColor(Color.parseColor("#66CF04"));
    	} else if (A_time_daysint >= 60 && A_time_daysint <= 64){
    		tv11.setTextColor(Color.parseColor("#55CF04"));
    	} else if (A_time_daysint >= 65 && A_time_daysint <= 69){
    		tv11.setTextColor(Color.parseColor("#44CF04"));
    	} else if (A_time_daysint >= 70 && A_time_daysint <= 74){
    		tv11.setTextColor(Color.parseColor("#2DCF04"));
    	} else if (A_time_daysint >= 75 && A_time_daysint <= 79){
    		tv11.setTextColor(Color.parseColor("#18CF04"));
    	} else if (A_time_daysint >= 80 && A_time_daysint <= 84){
    		tv11.setTextColor(Color.parseColor("#04CF04"));
    	} else if (A_time_daysint >= 85 && A_time_daysint <= 89){
    		tv11.setTextColor(Color.parseColor("#04CF15"));
    	} else if (A_time_daysint >= 90){
    		tv11.setTextColor(Color.parseColor("#00AB2B"));
    	}
    	
    	TextView tv18 = (TextView) findViewById(R.id.mtimedayssmall); // manga
    	if (M_time_daysint >= 0 && M_time_daysint <= 2){
    		tv18.setTextColor(Color.parseColor("#CF0404"));
    	} else if (M_time_daysint >= 3 && M_time_daysint <= 5){
    		tv18.setTextColor(Color.parseColor("#CF1F04"));
    	} else if (M_time_daysint >= 6 && M_time_daysint <= 8){
    		tv18.setTextColor(Color.parseColor("#CF3304"));
    	} else if (M_time_daysint >= 9 && M_time_daysint <= 11){
    		tv18.setTextColor(Color.parseColor("#CF5204"));
    	} else if (M_time_daysint >= 12 && M_time_daysint <= 14){
    		tv18.setTextColor(Color.parseColor("#CF7004"));
    	} else if (M_time_daysint >= 15 && M_time_daysint <= 17){
    		tv18.setTextColor(Color.parseColor("#CF8E04"));
    	} else if (M_time_daysint >= 18 && M_time_daysint <= 20){
    		tv18.setTextColor(Color.parseColor("#CFB704"));
    	} else if (M_time_daysint >= 21 && M_time_daysint <= 23){
    		tv18.setTextColor(Color.parseColor("#C4CF04"));
    	} else if (M_time_daysint >= 24 && M_time_daysint <= 26){
    		tv18.setTextColor(Color.parseColor("#B4CF04"));
    	} else if (M_time_daysint >= 27 && M_time_daysint <= 29){
    		tv18.setTextColor(Color.parseColor("#ADCF04"));
    	} else if (M_time_daysint >= 30 && M_time_daysint <= 32){
    		tv18.setTextColor(Color.parseColor("#A3CF04"));
    	} else if (M_time_daysint >= 33 && M_time_daysint <= 35){
    		tv18.setTextColor(Color.parseColor("#95CF04"));
    	} else if (M_time_daysint >= 36 && M_time_daysint <= 38){
    		tv18.setTextColor(Color.parseColor("#7ECF04"));
    	} else if (M_time_daysint >= 39 && M_time_daysint <= 41){
    		tv18.setTextColor(Color.parseColor("#66CF04"));
    	} else if (M_time_daysint >= 42 && M_time_daysint <= 44){
    		tv18.setTextColor(Color.parseColor("#55CF04"));
    	} else if (M_time_daysint >= 45 && M_time_daysint <= 47){
    		tv18.setTextColor(Color.parseColor("#44CF04"));
    	} else if (M_time_daysint >= 48 && M_time_daysint <= 50){
    		tv18.setTextColor(Color.parseColor("#2DCF04"));
    	} else if (M_time_daysint >= 51 && M_time_daysint <= 53){
    		tv18.setTextColor(Color.parseColor("#18CF04"));
    	} else if (M_time_daysint >= 54 && M_time_daysint <= 56){
    		tv18.setTextColor(Color.parseColor("#04CF04"));
    	} else if (M_time_daysint >= 57 && M_time_daysint <= 59){
    		tv18.setTextColor(Color.parseColor("#04CF15"));
    	} else if (M_time_daysint >= 60){
    		tv18.setTextColor(Color.parseColor("#00AB2B"));
    	}
    }
    
    public void Save(){
    	Settext();
    	SharedPreferences.Editor profieleditor = profielprefs.edit();
		Editor editor1 = profieleditor.putString("avatar_url", avatar_url);editor1.commit();
		Editor editor2 = profieleditor.putString("birthday", birthday);editor2.commit();
		Editor editor3 = profieleditor.putString("location", location);editor3.commit();
		Editor editor4 = profieleditor.putInt("comments", comments);editor4.commit();
		Editor editor5 = profieleditor.putInt("forum_posts", forum_posts);editor5.commit();
		Editor editor6 = profieleditor.putString("last_online", last_online);editor6.commit();
		Editor editor7 = profieleditor.putString("gender", gender);editor7.commit();
		Editor editor8 = profieleditor.putString("join_date", join_date);editor8.commit();
		Editor editor9 = profieleditor.putString("access_rank", access_rank);editor9.commit();
		Editor editor10 = profieleditor.putInt("anime_list_views", anime_list_views);editor10.commit();
		Editor editor11 = profieleditor.putInt("manga_list_views", manga_list_views);editor11.commit();
		
		Editor editor12 = profieleditor.putString("A_time_days", A_time_days);editor12.commit();
		Editor editor26 = profieleditor.putInt("A_time_daysint", A_time_daysint);editor26.commit();
		Editor editor13 = profieleditor.putInt("A_watching", A_watching);editor13.commit();
		Editor editor14 = profieleditor.putInt("A_completed", A_completed);editor14.commit();
		Editor editor15 = profieleditor.putInt("A_on_hold", A_on_hold);editor15.commit();
		Editor editor16 = profieleditor.putInt("A_dropped", A_dropped);editor16.commit();
		Editor editor17 = profieleditor.putInt("A_plan_to_watch", A_plan_to_watch);editor17.commit();
		Editor editor18 = profieleditor.putInt("A_total_entries", A_total_entries);editor18.commit();
		
		Editor editor19 = profieleditor.putString("M_time_days", M_time_days);editor19.commit();
		Editor editor27 = profieleditor.putInt("M_time_daysint", M_time_daysint);editor27.commit();
		Editor editor20 = profieleditor.putInt("M_reading", M_reading);editor20.commit();
		Editor editor21 = profieleditor.putInt("M_completed", M_completed);editor21.commit();
		Editor editor22 = profieleditor.putInt("M_on_hold", M_on_hold);editor22.commit();
		Editor editor23 = profieleditor.putInt("M_dropped", M_dropped);editor23.commit();
		Editor editor24 = profieleditor.putInt("M_plan_to_read", M_plan_to_read);editor24.commit();
		Editor editor25 = profieleditor.putInt("M_total_entries", M_total_entries);editor25.commit();
    }
    
    public void Offline(){
    	try{
    		avatar_url= profielprefs.getString("avatar_url", avatar_url);
    		birthday= profielprefs.getString("birthday",birthday);
    		location= profielprefs.getString("location", location);
    		comments= profielprefs.getInt("comments",comments);
    		forum_posts= profielprefs.getInt("forum_posts",forum_posts);
    		last_online= profielprefs.getString("last_online",last_online);
    		gender= profielprefs.getString("gender",gender );
    		join_date= profielprefs.getString("join_date", join_date);
    		access_rank= profielprefs.getString("access_rank", access_rank);
    		anime_list_views= profielprefs.getInt("anime_list_views", anime_list_views);
    		manga_list_views= profielprefs.getInt("manga_list_views", manga_list_views);
		
    		A_time_days= profielprefs.getString("A_time_days", A_time_days);
    		A_time_daysint= profielprefs.getInt("A_time_daysint", A_time_daysint);
    		A_watching= profielprefs.getInt("A_watching", A_watching);
    		A_completed= profielprefs.getInt("A_completed", A_completed);
    		A_on_hold= profielprefs.getInt("A_on_hold",A_on_hold );
    		A_dropped= profielprefs.getInt("A_dropped",A_dropped );
    		A_plan_to_watch= profielprefs.getInt("A_plan_to_watch", A_plan_to_watch);
    		A_total_entries= profielprefs.getInt("A_total_entries", A_total_entries);
		
    		M_time_days= profielprefs.getString("M_time_days",M_time_days );
    		M_time_daysint= profielprefs.getInt("M_time_daysint",M_time_daysint );
    		M_reading= profielprefs.getInt("M_reading", M_reading);
    		M_completed= profielprefs.getInt("M_completed", M_completed);
    		M_on_hold= profielprefs.getInt("M_on_hold",M_on_hold );
    		M_dropped= profielprefs.getInt("M_dropped",M_dropped );
    		M_plan_to_read= profielprefs.getInt("M_plan_to_read", M_plan_to_read);
    		M_total_entries= profielprefs.getInt("M_total_entries",M_total_entries );
    		Settext();
    	}catch (Exception e){
    		Crouton.makeText(this, "No offline record available!", Style.ALERT).show();
    	}
    }
    
    public void Settext(){
    	TextView tv1 = (TextView) findViewById(R.id.birthdaysmall);
    	if (birthday == "null"){
    		tv1.setText("Not specified");
    	}else{
    		tv1.setText(birthday);
    	}
		TextView tv2 = (TextView) findViewById(R.id.locationsmall);
		if (location == "null"){
    		tv2.setText("Not specified");
    	}else{
    		tv2.setText(location);
    	}
		TextView tv3 = (TextView) findViewById(R.id.commentspostssmall);
		tv3.setText(Integer.toString(comments));
		TextView tv4 = (TextView) findViewById(R.id.forumpostssmall);
		tv4.setText(Integer.toString(forum_posts));
		TextView tv5 = (TextView) findViewById(R.id.lastonlinesmall);
		tv5.setText(last_online);
		TextView tv6 = (TextView) findViewById(R.id.gendersmall);
		tv6.setText(gender);
		TextView tv7 = (TextView) findViewById(R.id.joindatesmall);
		tv7.setText(join_date);
		TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
		tv8.setText(access_rank);
		TextView tv9 = (TextView) findViewById(R.id.animelistviewssmall);
		tv9.setText(Integer.toString(anime_list_views));
		TextView tv10 = (TextView) findViewById(R.id.mangalistviewssmall);
		tv10.setText(Integer.toString(manga_list_views));
		
		TextView tv11 = (TextView) findViewById(R.id.atimedayssmall);
		tv11.setText(A_time_days);
		TextView tv12 = (TextView) findViewById(R.id.awatchingsmall);
		tv12.setText(Integer.toString(A_watching));
		TextView tv13 = (TextView) findViewById(R.id.acompletedpostssmall);
		tv13.setText(Integer.toString(A_completed));
		TextView tv14 = (TextView) findViewById(R.id.aonholdsmall);
		tv14.setText(Integer.toString(A_on_hold));
		TextView tv15 = (TextView) findViewById(R.id.adroppedsmall);
		tv15.setText(Integer.toString(A_dropped));
		TextView tv16 = (TextView) findViewById(R.id.aplantowatchsmall);
		tv16.setText(Integer.toString(A_plan_to_watch));
		TextView tv17 = (TextView) findViewById(R.id.atotalentriessmall);
		tv17.setText(Integer.toString(A_total_entries));
		
		TextView tv18 = (TextView) findViewById(R.id.mtimedayssmall);
		tv18.setText(M_time_days);
		TextView tv19 = (TextView) findViewById(R.id.mwatchingsmall);
		tv19.setText(Integer.toString(M_reading));
		TextView tv20 = (TextView) findViewById(R.id.mcompletedpostssmall);
		tv20.setText(Integer.toString(M_completed));
		TextView tv21 = (TextView) findViewById(R.id.monholdsmall);
		tv21.setText(Integer.toString(M_on_hold));
		TextView tv22 = (TextView) findViewById(R.id.mdroppedsmall);
		tv22.setText(Integer.toString(M_dropped));
		TextView tv23 = (TextView) findViewById(R.id.mplantowatchsmall);
		tv23.setText(Integer.toString(M_plan_to_read));
		TextView tv24 = (TextView) findViewById(R.id.mtotalentriessmall);
		tv24.setText(Integer.toString(M_total_entries));
    }
}
