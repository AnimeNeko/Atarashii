package net.somethingdreadful.MAL;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends SherlockFragmentActivity   {
Context context;
String name;
ImageView Imagdae;
boolean forcesync = false;

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
Integer A_time_days = 0;
Integer A_watching = 0;
Integer A_completed = 0;
Integer A_on_hold = 0;
Integer A_dropped = 0;
Integer A_plan_to_watch = 0;
Integer A_total_entries = 0;
//manga details
Integer M_time_days = 0;
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
        name = getSharedPreferences("prefs", MODE_PRIVATE).getString("user", "?"); //get username
        setTitle("User profile of " + name); //set title
        context = getApplicationContext();
        new RetrieveMessages().execute("http://mal-api.com/profile/" + name); // send url to the background
        setcolor();
        card();
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
        }
        return true;
    }
    
    private class RetrieveMessages extends AsyncTask<String, Void, String> {
    	ImageDownloader imageDownloader = new ImageDownloader(context);
    	protected String doInBackground(String... urls) {
    		if (isConnectedWifi() || Wifisyncdisable() == false || forcesync == true){ // settings check
    			if (autosync() && isNetworkAvailable() || forcesync == true){ // settings check & network check
    				HttpClient client = new DefaultHttpClient();
    				String json = "";
    				try {
    					String line = "";
    					HttpGet request = new HttpGet(urls[0]);
    					HttpResponse response = client.execute(request);//get response
    					BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    					while ((line = rd.readLine()) != null) {
    						json += line + System.getProperty("line.separator"); //save response
    						try {
    							JSONObject jsonObject = new JSONObject(json);
    							avatar_url = jsonObject.getString("avatar_url").replaceFirst("t.jpg$", ".jpg");
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
						
    							A_time_days = jsonObject.getJSONObject("anime_stats").getInt("time_days");
    							A_watching = jsonObject.getJSONObject("anime_stats").getInt("watching");
    							A_completed = jsonObject.getJSONObject("anime_stats").getInt("completed");
    							A_on_hold = jsonObject.getJSONObject("anime_stats").getInt("on_hold");
    							A_dropped = jsonObject.getJSONObject("anime_stats").getInt("dropped");
    							A_plan_to_watch = jsonObject.getJSONObject("anime_stats").getInt("plan_to_watch");
    							A_total_entries = jsonObject.getJSONObject("anime_stats").getInt("total_entries");
						
    							M_time_days = jsonObject.getJSONObject("manga_stats").getInt("time_days");
								M_reading = jsonObject.getJSONObject("manga_stats").getInt("reading");
								M_completed = jsonObject.getJSONObject("manga_stats").getInt("completed");
								M_on_hold = jsonObject.getJSONObject("manga_stats").getInt("on_hold");
								M_dropped = jsonObject.getJSONObject("manga_stats").getInt("dropped");
								M_plan_to_read = jsonObject.getJSONObject("manga_stats").getInt("plan_to_read");
								M_total_entries = jsonObject.getJSONObject("manga_stats").getInt("total_entries");
    						} catch (Exception e) {
    						e.printStackTrace();
    						}
    					}
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		forcesync = false;
            return birthday;
        }

        protected void onProgressUpdate(Void... progress) {
			
        }

		protected void onPostExecute(String check) {
			if (check == ""){ //birthday check
				Offline();
			}else{
				Save();
			}
			//json was full of data :p
			TextView tv1 = (TextView) findViewById(R.id.birthdaysmall);
			tv1.setText(birthday);
			TextView tv2 = (TextView) findViewById(R.id.locationsmall);
			tv2.setText(location);
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
			tv11.setText(Integer.toString(A_time_days));
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
			tv18.setText(Integer.toString(M_time_days));
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
			try{
				Imagdae = (ImageView) findViewById(R.id.Imagdae);
				imageDownloader.download(avatar_url, Imagdae);
				Imagdae.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, avatar_url)));
			}catch (Exception e){	
				Log.e(this.getClass().getName(), Log.getStackTraceString(e));
			}
			setcolor(); 
        }
    }
    
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public void card() { //settings for hide a card
    	if (animehide()){
    		LinearLayout a =(LinearLayout)findViewById(R.id.Anime_card);
    		a.setVisibility(View.GONE);
    	}
    	if (mangahide()){
    		LinearLayout m =(LinearLayout)findViewById(R.id.Manga_card);
    		m.setVisibility(View.GONE);
    	}
    }
    
    public boolean isConnectedWifi() {
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo Wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (Wifi.isConnected() && Wifisyncdisable()) {
            return true;
        } else {
            return false;
        }
    }
    
    public void setcolor(){
    	TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
    	if (Textcolordisable() == false){
    		setcoloranime();
    		if (access_rank.contains("Administrator")){
    			tv8.setTextColor(Color.parseColor("#850000"));
    		}else if (access_rank.contains("Moderator")) {
    			tv8.setTextColor(Color.parseColor("#003385"));
    		}else if (access_rank.contains("Member")){
    			if (name.equals("ratan12") || name.equals("AnimaSA") || name.equals("motokochan") || name.equals("apkawa")) {
    				tv8.setTextColor(Color.parseColor("#008583")); //Developer
    			}else{
    				tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
    				tv8.setText(name);
    			}
    		}    		
    	}
    	if (name.equals("ratan12") || name.equals("AnimaSA") || name.equals("motokochan") || name.equals("apkawa")) {
			tv8.setText("Atarashii developer"); //Developer
		}
    }
    
    public boolean Textcolordisable() {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("text_colours", false);
    }
    
    public boolean animehide() {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("A_hide", false);
    }
    
    public boolean mangahide() {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("M_hide", false);
    }
    
    public boolean autosync() {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("PAutosync", true);
    }
    
    public boolean Wifisyncdisable() {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("Wifi_sync", false);
    }
    
    public void setcoloranime(){
    	TextView tv11 = (TextView) findViewById(R.id.atimedayssmall);
    	if (A_time_days >= 0 && A_time_days <= 3){
    		tv11.setTextColor(Color.parseColor("#CF0404"));
    	} else if (A_time_days >= 4 && A_time_days <= 7){
    		tv11.setTextColor(Color.parseColor("#CF1F04"));
    	} else if (A_time_days >= 8 && A_time_days <= 11){
    		tv11.setTextColor(Color.parseColor("#CF3304"));
    	} else if (A_time_days >= 12 && A_time_days <= 15){
    		tv11.setTextColor(Color.parseColor("#CF5204"));
    	} else if (A_time_days >= 16 && A_time_days <= 19){
    		tv11.setTextColor(Color.parseColor("#CF7004"));
    	} else if (A_time_days >= 20 && A_time_days <= 24){
    		tv11.setTextColor(Color.parseColor("#CF8E04"));
    	} else if (A_time_days >= 25 && A_time_days <= 28){
    		tv11.setTextColor(Color.parseColor("#CFB704"));
    	} else if (A_time_days >= 29 && A_time_days <= 35){
    		tv11.setTextColor(Color.parseColor("#C4CF04"));
    	} else if (A_time_days >= 36 && A_time_days <= 42){
    		tv11.setTextColor(Color.parseColor("#B4CF04"));
    	} else if (A_time_days >= 43 && A_time_days <= 48){
    		tv11.setTextColor(Color.parseColor("#ADCF04"));
    	} else if (A_time_days >= 49 && A_time_days <= 55){
    		tv11.setTextColor(Color.parseColor("#A3CF04"));
    	} else if (A_time_days >= 56 && A_time_days <= 62){
    		tv11.setTextColor(Color.parseColor("#95CF04"));
    	} else if (A_time_days >= 63 && A_time_days <= 70){
    		tv11.setTextColor(Color.parseColor("#7ECF04"));
    	} else if (A_time_days >= 71 && A_time_days <= 77){
    		tv11.setTextColor(Color.parseColor("#66CF04"));
    	} else if (A_time_days >= 76 && A_time_days <= 82){
    		tv11.setTextColor(Color.parseColor("#55CF04"));
    	} else if (A_time_days >= 83 && A_time_days <= 89){
    		tv11.setTextColor(Color.parseColor("#44CF04"));
    	} else if (A_time_days >= 90 && A_time_days <= 95){
    		tv11.setTextColor(Color.parseColor("#2DCF04"));
    	} else if (A_time_days >= 96 && A_time_days <= 100){
    		tv11.setTextColor(Color.parseColor("#18CF04"));
    	} else if (A_time_days >= 101 && A_time_days <= 105){
    		tv11.setTextColor(Color.parseColor("#04CF04"));
    	} else if (A_time_days >= 106 && A_time_days <= 110){
    		tv11.setTextColor(Color.parseColor("#04CF15"));
    	} else if (A_time_days >= 111){
    		tv11.setTextColor(Color.parseColor("#00AB2B"));
    	}
    	
    	TextView tv18 = (TextView) findViewById(R.id.mtimedayssmall);
    	if (M_time_days >= 0 && M_time_days <= 3){
    		tv18.setTextColor(Color.parseColor("#CF0404"));
    	} else if (M_time_days >= 4 && M_time_days <= 7){
    		tv18.setTextColor(Color.parseColor("#CF1F04"));
    	} else if (M_time_days >= 8 && M_time_days <= 11){
    		tv18.setTextColor(Color.parseColor("#CF3304"));
    	} else if (M_time_days >= 12 && M_time_days <= 15){
    		tv18.setTextColor(Color.parseColor("#CF5204"));
    	} else if (M_time_days >= 16 && M_time_days <= 19){
    		tv18.setTextColor(Color.parseColor("#CF7004"));
    	} else if (M_time_days >= 20 && M_time_days <= 24){
    		tv18.setTextColor(Color.parseColor("#CF8E04"));
    	} else if (M_time_days >= 25 && M_time_days <= 28){
    		tv18.setTextColor(Color.parseColor("#CFB704"));
    	} else if (M_time_days >= 29 && M_time_days <= 35){
    		tv18.setTextColor(Color.parseColor("#C4CF04"));
    	} else if (M_time_days >= 36 && M_time_days <= 42){
    		tv18.setTextColor(Color.parseColor("#B4CF04"));
    	} else if (M_time_days >= 43 && M_time_days <= 48){
    		tv18.setTextColor(Color.parseColor("#ADCF04"));
    	} else if (M_time_days >= 49 && M_time_days <= 55){
    		tv18.setTextColor(Color.parseColor("#A3CF04"));
    	} else if (M_time_days >= 56 && M_time_days <= 62){
    		tv18.setTextColor(Color.parseColor("#95CF04"));
    	} else if (M_time_days >= 63 && M_time_days <= 70){
    		tv18.setTextColor(Color.parseColor("#7ECF04"));
    	} else if (M_time_days >= 71 && M_time_days <= 77){
    		tv18.setTextColor(Color.parseColor("#66CF04"));
    	} else if (M_time_days >= 76 && M_time_days <= 82){
    		tv18.setTextColor(Color.parseColor("#55CF04"));
    	} else if (M_time_days >= 83 && M_time_days <= 89){
    		tv18.setTextColor(Color.parseColor("#44CF04"));
    	} else if (M_time_days >= 90 && M_time_days <= 95){
    		tv18.setTextColor(Color.parseColor("#2DCF04"));
    	} else if (M_time_days >= 96 && M_time_days <= 100){
    		tv18.setTextColor(Color.parseColor("#18CF04"));
    	} else if (M_time_days >= 101 && M_time_days <= 105){
    		tv18.setTextColor(Color.parseColor("#04CF04"));
    	} else if (M_time_days >= 106 && M_time_days <= 110){
    		tv18.setTextColor(Color.parseColor("#04CF15"));
    	} else if (M_time_days >= 111){
    		tv18.setTextColor(Color.parseColor("#00AB2B"));
    	}
    }
    
    public void Save(){
		Editor editor1 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("avatar_url", avatar_url);editor1.commit();
		Editor editor2 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("birthday", birthday);editor2.commit();
		Editor editor3 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("location", location);editor3.commit();
		Editor editor4 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("comments", comments);editor4.commit();
		Editor editor5 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("forum_posts", forum_posts);editor5.commit();
		Editor editor6 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("last_online", last_online);editor6.commit();
		Editor editor7 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("gender", gender);editor7.commit();
		Editor editor8 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("join_date", join_date);editor8.commit();
		Editor editor9 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("access_rank", access_rank);editor9.commit();
		Editor editor10 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("anime_list_views", anime_list_views);editor10.commit();
		Editor editor11 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("manga_list_views", manga_list_views);editor11.commit();
		
		Editor editor12 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_time_days", A_time_days);editor12.commit();
		Editor editor13 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_watching", A_watching);editor13.commit();
		Editor editor14 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_completed", A_completed);editor14.commit();
		Editor editor15 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_on_hold", A_on_hold);editor15.commit();
		Editor editor16 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_dropped", A_dropped);editor16.commit();
		Editor editor17 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_plan_to_watch", A_plan_to_watch);editor17.commit();
		Editor editor18 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("A_total_entries", A_total_entries);editor18.commit();
		
		Editor editor19 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_time_days", M_time_days);editor19.commit();
		Editor editor20 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_reading", M_reading);editor20.commit();
		Editor editor21 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_completed", M_completed);editor21.commit();
		Editor editor22 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_on_hold", M_on_hold);editor22.commit();
		Editor editor23 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_dropped", M_dropped);editor23.commit();
		Editor editor24 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_plan_to_read", M_plan_to_read);editor24.commit();
		Editor editor25 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putInt("M_total_entries", M_total_entries);editor25.commit();
    }
    
    public void Offline(){
    	try {
    		avatar_url= getSharedPreferences("Profile", MODE_PRIVATE).getString("avatar_url", avatar_url);
    		birthday= getSharedPreferences("Profile", MODE_PRIVATE).getString("birthday",birthday);
    		location= getSharedPreferences("Profile", MODE_PRIVATE).getString("location", location);
    		comments= getSharedPreferences("Profile", MODE_PRIVATE).getInt("comments",comments);
    		forum_posts= getSharedPreferences("Profile", MODE_PRIVATE).getInt("forum_posts",forum_posts);
    		last_online= getSharedPreferences("Profile", MODE_PRIVATE).getString("last_online",last_online);
    		gender= getSharedPreferences("Profile", MODE_PRIVATE).getString("gender",gender );
    		join_date= getSharedPreferences("Profile", MODE_PRIVATE).getString("join_date", join_date);
    		access_rank= getSharedPreferences("Profile", MODE_PRIVATE).getString("access_rank", access_rank);
    		anime_list_views= getSharedPreferences("Profile", MODE_PRIVATE).getInt("anime_list_views", anime_list_views);
    		manga_list_views= getSharedPreferences("Profile", MODE_PRIVATE).getInt("manga_list_views", manga_list_views);
		
    		A_time_days= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_time_days", A_time_days);
    		A_watching= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_watching", A_watching);
    		A_completed= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_completed", A_completed);
    		A_on_hold= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_on_hold",A_on_hold );
    		A_dropped= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_dropped",A_dropped );
    		A_plan_to_watch= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_plan_to_watch", A_plan_to_watch);
    		A_total_entries= getSharedPreferences("Profile", MODE_PRIVATE).getInt("A_total_entries", A_total_entries);
		
    		M_time_days= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_time_days",M_time_days );
    		M_reading= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_reading", M_reading);
    		M_completed= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_completed", M_completed);
    		M_on_hold= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_on_hold",M_on_hold );
    		M_dropped= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_dropped",M_dropped );
    		M_plan_to_read= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_plan_to_read", M_plan_to_read);
    		M_total_entries= getSharedPreferences("Profile", MODE_PRIVATE).getInt("M_total_entries",M_total_entries );
    	} catch (Exception e){
    		
    	}
    }
}
