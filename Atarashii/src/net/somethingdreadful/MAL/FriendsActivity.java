package net.somethingdreadful.MAL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import net.somethingdreadful.MAL.record.ProfileMALRecord;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
    ArrayList<String> UsernameList;
    ListViewAdapter listadapter;
    int indexp; //position
    String text; //get selected username long
    ListView userList;
    boolean network;
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
        UsernameList = new ArrayList<String>();
        userList = (ListView)findViewById(R.id.listview);
        
        listadapter = new ListViewAdapter();
        
        restorelist(true); //restore users from preferences
        refresh(false);
        clicklistener();
    }
	
    public void remove(){ //removes a user
    	UsernameList.remove(indexp);
    	refresh(true);
    	listadapter.notifyDataSetChanged();
        restorelist(false);
    }
    
    public void refresh(boolean save){ //refresh list , if boolean is true than also save
    	Collections.sort(UsernameList);
        if (save == true){ // save the list
        	try{
        		SharedPreferences.Editor sEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        		Collections.sort(UsernameList);
        		for(int i=0;i <UsernameList.size();i++){
        			sEdit.putString("val"+i,UsernameList.get(i));
        		}
        	 	sEdit.putInt("size",UsernameList.size()).commit();
        	 	maketext("Userprofile saved!", 3);
        	}catch (Exception e){
        		maketext("Error while saving the list!", 2);
        	}
        }
        try{
			userList.setAdapter(listadapter);	
			listadapter.notifyDataSetChanged();
        }catch(Exception e){
        	maketext("You have no friends at your list!", 3);
        }
    }
    
    public void restorelist(boolean R){ //restore the list(get the arrays and restore them), boolean true will parse
    	network = isNetworkAvailable();
		ProfileMALRecord.parse = prefs.friendlistsync();
		Boolean onlywifi = prefs.friendlistonlywifi();
		int size = PreferenceManager.getDefaultSharedPreferences(context).getInt("size",0);
		UsernameList.clear();
    	try{
    		for(int position=0;position<size;position++){
    			UsernameList.add(PreferenceManager.getDefaultSharedPreferences(context).getString("val"+position,"Error"));
    		}
    	}catch (Exception e){
    		maketext("Error while restoring the list!", 2);
    		e.printStackTrace();
    	}
    	
    	try{// if the mal api is down this part will only crash.
    		if (ProfileMALRecord.parse && onlywifi && isConnectedWifi() && R || network && ProfileMALRecord.parse && !onlywifi || network && forcesync && R){
    			for(int position=0;position<size;position++){
    				new Startparse().execute(UsernameList.get(position));
    			}
    		}
    	}catch (Exception e){
    		maketext("Atarashii could not receive the profile!", 2);
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
		case R.id.action_addToList:
			inputdialog();
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
	
    public class Verify extends AsyncTask<String, Void, String> { //check username
    	protected String doInBackground(String... urls) {
    		ProfileMALRecord.Clearrecord(false);
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
    					ProfileMALRecord.username = text;
    					ProfileMALRecord.Grabrecord(jsonObject); //send the object to ProfileMALRecord
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            return ""; // Avatar == "" or Avatar == the avatar URL
        }

        protected void onProgressUpdate(Void... progress) {
        }

		protected void onPostExecute(String check) { //check = avatar
			if (ProfileMALRecord.avatar_url =="" && isNetworkAvailable()){ //checks if avatar == "" (This method also works when the api is offline)
				maketext("Invailid username!",2);
			}else if (check =="" && !isNetworkAvailable()){
				maketext("No network connection available!",2);
			}else if (!UsernameList.contains(text)){
				UsernameList.add(text); //user exist, add the user
				ProfileMALRecord.Saverecord();
				refresh(true); //refresh the list
			}else if (UsernameList.contains(text)){
				maketext("User is already in your list!",1);
			}
        }
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
	
	void removedialog(String text){ //before removing a user, show a alert
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Remove " + text + " from my friendlist?");
		builder.setMessage("Are you sure that you want to delete "+ text + " from my friendlist?");

		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        remove();//remove user
		    }
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		        maketext("Userprofile not Removed!", 1);
		    }
		});
		builder.show();
	}
	
	void inputdialog(){ //as the name says
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Username of friend?");

		final EditText input = new EditText(this);

		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        String m_Text = input.getText().toString();
		        text = m_Text;
		        new Verify().execute("http://mal-api.com/profile/" + m_Text); // send url to the background
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		        maketext("Userprofile not added!", 2);
		    }
		});
		builder.show();
	}
	void clicklistener(){
		userList.setOnItemClickListener(new OnItemClickListener(){ //start the profile with your friend
       	 public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3){   
       		 		ProfileMALRecord.parse = false;
       		 		ProfileMALRecord.username =  UsernameList.get(position);
       		 		ProfileMALRecord.Clearrecord(true);
	        		Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
	 				startActivity(profile);
            }
        });
        userList.setOnItemLongClickListener(new OnItemLongClickListener(){ //longclick = remove selected friend
       	 public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3){
       		 ProfileMALRecord.username = UsernameList.get(position);
       		 removedialog(ProfileMALRecord.username); //show confirm dialog
       		 indexp = position;
       		 return true;
       	 }
        });
	}
	  class ListViewAdapter extends BaseAdapter {
		  
	        public int getCount() {
	            return PreferenceManager.getDefaultSharedPreferences(context).getInt("size",0);
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
	            return PreferenceManager.getDefaultSharedPreferences(context).getInt("size",0);
	        }
	        
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = convertView;
	            try{
	            	if (view == null) {
	            		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            		view = inflater.inflate(R.layout.list_friends_with_text_item, parent, false);
	                
	            		//Set username
	            		ProfileMALRecord.username =  UsernameList.get(position).toString();
	            		ProfileMALRecord.Clearrecord(true);
	            		TextView Username = (TextView) view.findViewById(R.id.userName);
	            		Username.setText(ProfileMALRecord.username);
	            		if (ProfileMALRecord.Developerrecord(ProfileMALRecord.username)) {
	            			Username.setTextColor(Color.parseColor("#8CD4D3")); //Developer
	            		}
	            		
	            		//Set online or offline status
	            		TextView Status = (TextView) view.findViewById(R.id.status);
	            		if (ProfileMALRecord.last_online.equals("Now")){
	            			Status.setText("Online");
	            			Status.setTextColor(Color.parseColor("#0D8500"));
	            		}else{
	            			Status.setText("Offline");
	            			Status.setTextColor(Color.parseColor("#D10000"));
	            		}
	            		Picasso picasso =  Picasso.with(context);
		            	picasso.load(ProfileMALRecord.avatar_url).error(R.drawable.cover_error).placeholder(R.drawable.cover_loading).fit().into((ImageView) view.findViewById(R.id.profileImg));
	            	}
	            }catch (Exception e){
	            	e.printStackTrace();
	            }
	            return view;
	        }
	  }
	 
	  public class Startparse extends AsyncTask<String, Void, String> {
		  protected String doInBackground(String... urls) {
			  if (isNetworkAvailable() && forcesync || isNetworkAvailable() && ProfileMALRecord.parse){ // settings check
				  HttpClient client = new DefaultHttpClient();
				  String json = "";
				  try {
					  String line = "";
					  HttpGet request = new HttpGet("http://mal-api.com/profile/" + urls[0]);
					  HttpResponse response = client.execute(request);//get response
					  BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					  while ((line = rd.readLine()) != null) {
						  json += line + System.getProperty("line.separator"); //save response
						  JSONObject jsonObject = new JSONObject(json);
						  ProfileMALRecord.username = urls[0];
						  ProfileMALRecord.Clearrecord(true);
						  ProfileMALRecord.Grabrecord(jsonObject); //send the object to ProfileMALRecord
						  ProfileMALRecord.Saverecord();
					  }
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
			  }
			  return "";
		  }

		  protected void onProgressUpdate(Void... progress) {
		  }

		  protected void onPostExecute(String bind) {
			  refresh(false);
		  }
	  }
}
