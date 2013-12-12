package net.somethingdreadful.MAL;

import java.util.Collections;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.record.ProfileMALRecord;

import org.json.JSONArray;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FriendsActivity extends SherlockFragmentActivity {
	
    Context context;
    ListViewAdapter listadapter;
    GridView userList;
    boolean forcesync = false;
    PrefManager prefs;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();   
        ProfileMALRecord.context = getApplicationContext(); //ProfileMALRecord has a static record!
        prefs = new PrefManager(context);
        setContentView(R.layout.activity_friends);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true); //go to home to actionbar
        setTitle("My friends"); //set title
        userList = (GridView)findViewById(R.id.listview);
        listadapter = new ListViewAdapter();
        
        restorelist(false); //restore users from preferences
        refresh(false);
        
        userList.setOnItemClickListener(new OnItemClickListener(){ //start the profile with your friend
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
    		ProfileMALRecord.username = ProfileMALRecord.Friendrecord.get(arg2);
    		ProfileMALRecord.Clearrecord(false);
    		Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
			startActivity(profile);
		}
        });
    }
    
    public void refresh(boolean save){ //refresh list , if boolean is true than also save
    	Collections.sort(ProfileMALRecord.Friendrecord);
        if (save == true){ // save the list
        	ProfileMALRecord.SaveFriendrecord();
        	maketext("Userprofile saved!", 3);
        }
        if (ProfileMALRecord.Friendrecord.size() == 0){
        	new Startparse().execute();
        }else{
        	userList.setAdapter(listadapter);	
			listadapter.notifyDataSetChanged();
        }
    }
    
    public void restorelist(boolean Refresh){ //restore the list(get the arrays and restore them), boolean true will parse
    	ProfileMALRecord.LoadFriendrecord();
    	if (Refresh){
    		new Startparse().execute();
    	}
    }
    
    public boolean isConnectedWifi() {
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo Wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (Wifi.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_friends_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;
		case R.id.forceSync:
			if (isNetworkAvailable()){
				maketext("Sync started!", 1);
				forcesync = true;
				restorelist(true);
				refresh(false);
			}else{
				maketext("No network connection available!", 2);
			}
			break;
		}
        return super.onOptionsItemSelected(item);
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
	
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
	
	 class ListViewAdapter extends BaseAdapter {
		  
	        public int getCount() {
	            return ProfileMALRecord.Friendrecord.size();
	        }
	        public String getItem(int position) {
	            return null;
	        }
	        public long getItemId(int position) {
	            return position;
	        }
	        public int getItemViewType(int position) {
	            return position;
	        }
	        public int getViewTypeCount() {
	            return ProfileMALRecord.Friendrecord.size();
	        }
	        
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = convertView;
	            try{
	            	if (view == null) {
	            		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            		view = inflater.inflate(R.layout.list_friends_with_text_item, parent, false);
	                
	            		//Set username
	            		String username =  ProfileMALRecord.Friendrecord.get(position).toString();
	            		TextView Username = (TextView) view.findViewById(R.id.userName);
	            		Username.setText(username);
	            		if (ProfileMALRecord.Developerrecord(username)) {
	            			Username.setTextColor(Color.parseColor("#8CD4D3")); //Developer
	            		}
	            		SharedPreferences Read = PreferenceManager.getDefaultSharedPreferences(context);
	            		String last_online = Read.getString("last_online" + username, "Unknown");
	            		//Set online or offline status
	            		TextView Status = (TextView) view.findViewById(R.id.status);
	            		if (last_online.equals("Now")){
	            			Status.setText("Online");
	            			Status.setTextColor(Color.parseColor("#0D8500"));
	            		}else{
	            			Status.setText("Offline");
	            			Status.setTextColor(Color.parseColor("#D10000"));
	            		}
	            		TextView since = (TextView) view.findViewById(R.id.since);
	            		since.setText(Read.getString("since" + username, "Unknown"));
	            		TextView lastonline = (TextView) view.findViewById(R.id.lastonline);
	            		lastonline.setText(Read.getString("last_online" + username, "Unknown"));
	            		Picasso picasso =  Picasso.with(context);
		            	picasso.load(Read.getString("avatar_url" + username, "http://cdn.myanimelist.net/images/na.gif"))
		            		.error(R.drawable.cover_error)
		            		.placeholder(R.drawable.cover_loading)
		            		.fit()
		            		.into((ImageView) view.findViewById(R.id.profileImg));
	            	}
	            }catch (Exception e){
	            	e.printStackTrace();
	            }
	            return view;
	        }
	  }
	  
	  public class Startparse extends AsyncTask<String, Void, String> {
		  protected String doInBackground(String... urls) {
			  if (isNetworkAvailable() && forcesync || isNetworkAvailable()){ // settings check
				 try{
					MALApi api = new MALApi(context);
					JSONArray jsonArray = api.getFriends(prefs.getUser());
						  
					int length = jsonArray.length() - 1;
					ProfileMALRecord.Friendrecord.clear();
					for(int i=0;i<=length;i++){
						ProfileMALRecord.GrabFriendrecord(jsonArray.getJSONObject(i));
					}
				} catch (Exception e) {
				}
			  }
			  return null;
		  }

		  protected void onProgressUpdate(Void... progress) {
		  }

		  protected void onPostExecute(String bind) {
			  refresh(true);
		  }
	  }
}
