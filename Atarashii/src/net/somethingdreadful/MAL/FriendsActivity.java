package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.Collection;

import net.somethingdreadful.MAL.record.UserRecord;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
    ArrayList<UserRecord> listarray = new ArrayList<UserRecord>();
    ListViewAdapter<UserRecord> listadapter;
    GridView Gridview;
    boolean forcesync = false;
    PrefManager prefs;
    MALManager mManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();   
        
        setContentView(R.layout.activity_friends);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true); //go to home to actionbar
        setTitle("My friends"); //set title
        
        Gridview = (GridView)findViewById(R.id.listview);
        int recource = R.layout.list_friends_with_text_item;
        
        listadapter = new ListViewAdapter<UserRecord>(context, recource);
        mManager = new MALManager(this.context);
        prefs = new PrefManager(context);
        
        new getFriendsRecordsTask().execute();
        refresh(false);
        
        Gridview.setOnItemClickListener(new OnItemClickListener(){ //start the profile with your friend
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
    		UserRecord.username = listarray.get(position).getUsername();
    		Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
			startActivity(profile);
		}
        });
    }
    
    public void refresh(Boolean crouton){
    	if (crouton == true){
			Crouton.makeText(this, "FriendList updated!", Style.CONFIRM).show();
    	}
        Gridview.setAdapter(listadapter);	
        listadapter.clear();
        try{
        	listadapter.supportAddAll(listarray);
        }catch (Exception e){
        	if (isNetworkAvailable()){
    			Crouton.makeText(this, "You have no friends on MAL!", Style.ALERT).show();
    		}else{
    			Crouton.makeText(this, "No network connection!", Style.ALERT).show();
    		}
        }
        listadapter.notifyDataSetChanged();
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
				Crouton.makeText(this, "Sync started!", Style.INFO).show();
				forcesync = true;
	    		new getFriendsRecordsTask().execute();
			}else{
				Crouton.makeText(this, "No network connection available!", Style.ALERT).show();
			}
			break;
		}
        return super.onOptionsItemSelected(item);
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
	
    public class ListViewAdapter<T> extends ArrayAdapter<T> {


		public ListViewAdapter(Context context, int resource) {
            super(context, resource);
        }
	    
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = convertView;
	            final UserRecord record;
	            record = ((UserRecord) listarray.get(position));
	            
	            try{
	            	if (view == null) {
	            		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            		view = inflater.inflate(R.layout.list_friends_with_text_item, parent, false);
	                
	            		String username =  record.getUsername();
	            		TextView Username = (TextView) view.findViewById(R.id.userName);
	            		Username.setText(username);
	            		if (UserRecord.developerRecord(username)) {
	            			Username.setTextColor(Color.parseColor("#8CD4D3")); //Developer
	            		}
	            		String last_online = record.getLast();
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
	            		since.setText(record.getSince());
	            		TextView lastonline = (TextView) view.findViewById(R.id.lastonline);
	            		lastonline.setText(last_online);
	            		Picasso picasso =  Picasso.with(context);
		            	picasso.load(record.getAvatar())
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

	        public void supportAddAll(Collection<? extends T> collection) {
				for (T record : collection) {
		            this.add(record);
		        }
			}
	  }
	  
	  public class getFriendsRecordsTask extends AsyncTask<String, Void, ArrayList<UserRecord>> {
		  Boolean download = false;
		  
		  @Override
		  protected ArrayList<UserRecord> doInBackground(String... user) {
			  if (forcesync == true){
				  mManager.downloadAndStoreFriends(prefs.getUser());
				  forcesync = false;
				  download = true;
			  }
	          listarray = mManager.getFriendsRecordsFromDB();
	          if (listarray == null && isNetworkAvailable()){
	        	  mManager.downloadAndStoreFriends(prefs.getUser());
	        	  listarray = mManager.getFriendsRecordsFromDB();
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
}
