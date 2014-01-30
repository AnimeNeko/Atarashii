package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.Collection;

import net.somethingdreadful.MAL.api.response.Friend;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTaskFinishedListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class FriendsActivity extends SherlockFragmentActivity implements FriendsNetworkTaskFinishedListener {
	
    Context context;
    ArrayList<Friend> listarray = new ArrayList<Friend>();
    ListViewAdapter<Friend> listadapter;
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
        
        listadapter = new ListViewAdapter<Friend>(context, recource);
        mManager = new MALManager(context);
        prefs = new PrefManager(context);
        
        new FriendsNetworkTask(context, forcesync, this).execute(prefs.getUser());
        refresh(false);
        
        Gridview.setOnItemClickListener(new OnItemClickListener(){ //start the profile with your friend
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
    		Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
    		profile.putExtra("username", listarray.get(position).getName());
			startActivity(profile);
		}
        });
    }
    
    public void refresh(Boolean crouton){
    	if (crouton == true){
			Crouton.makeText(this, R.string.toast_SyncDone, Style.CONFIRM).show();
    	}
        Gridview.setAdapter(listadapter);	
        listadapter.clear();
        try{
        	listadapter.supportAddAll(listarray);
        }catch (Exception e){
        	if (isNetworkAvailable()){
    			Crouton.makeText(this, R.string.crouton_UserRecord_noFriends, Style.ALERT).show();
    		}else{
    			Crouton.makeText(this, R.string.crouton_noConnectivity, Style.ALERT).show();
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
				Crouton.makeText(this, R.string.crouton_SyncMessage, Style.INFO).show();
				forcesync = true;
				new FriendsNetworkTask(context, forcesync, this).execute(prefs.getUser());
			}else{
				Crouton.makeText(this, R.string.crouton_noConnectivity, Style.ALERT).show();
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
            final Friend record;
            record = ((Friend) listarray.get(position));
            
            try{
            	if (view == null) {
            		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            		view = inflater.inflate(R.layout.list_friends_with_text_item, parent, false);
                
            		String username =  record.getName();
            		TextView Username = (TextView) view.findViewById(R.id.userName);
            		Username.setText(username);
            		if (User.isDeveloperRecord(username)) {
            			Username.setTextColor(Color.parseColor("#8CD4D3")); //Developer
            		}
            		String last_online = record.getProfile().getDetails().getLastOnline();
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
            		since.setText(record.getFriendSince());
            		TextView lastonline = (TextView) view.findViewById(R.id.lastonline);
            		lastonline.setText(last_online);
            		Picasso picasso =  Picasso.with(context);
	            	picasso.load(record.getProfile().getAvatarUrl())
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

    @Override
    public void onFriendsNetworkTaskFinished(ArrayList<Friend> result) {
        if ( result != null ) {
            listarray = result;
            refresh(forcesync); // show crouton only if sync was forced
        } else {
            Crouton.makeText(this, R.string.crouton_UserRecord_Friends_error, Style.ALERT).show();
        }
    }
}
