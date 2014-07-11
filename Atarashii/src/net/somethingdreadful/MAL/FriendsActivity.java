package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.app.Activity;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTaskFinishedListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FriendsActivity extends Activity implements FriendsNetworkTaskFinishedListener {
	
    Context context;
    ArrayList<User> listarray = new ArrayList<User>();
    ListViewAdapter<User> listadapter;
    GridView Gridview;
    boolean forcesync = false;
    PrefManager prefs;
    MALManager mManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();   
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_friends);
        setTitle(R.string.title_activity_friends); //set title
        
        Gridview = (GridView)findViewById(R.id.listview);
        int recource = R.layout.record_friends_gridview;
        
        listadapter = new ListViewAdapter<User>(context, recource);
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

        NfcHelper.disableBeam(this);
    }
    
    public void refresh(Boolean crouton){
    	if (crouton == true){
			Crouton.makeText(this, R.string.crouton_info_SyncDone, Style.CONFIRM).show();
    	}
        Gridview.setAdapter(listadapter);	
        listadapter.clear();
        try{
        	listadapter.supportAddAll(listarray);
        }catch (Exception e){
        	if (MALApi.isNetworkAvailable(context)){
    			Crouton.makeText(this, R.string.crouton_error_noFriends, Style.ALERT).show();
    		}else{
    			Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
    		}
        }
        listadapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.activity_friends_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        final int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;
		case R.id.forceSync:
			if (MALApi.isNetworkAvailable(context)){
				Crouton.makeText(this, R.string.crouton_info_SyncMessage, Style.INFO).show();
				forcesync = true;
				new FriendsNetworkTask(context, forcesync, this).execute(prefs.getUser());
			}else{
				Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
			}
			break;
		}
        return super.onOptionsItemSelected(item);
    }
	
    public class ListViewAdapter<T> extends ArrayAdapter<T> {


		public ListViewAdapter(Context context, int resource) {
            super(context, resource);
        }
	    
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final User record;
            record = ((User) listarray.get(position));
            
            try{
            	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	view = inflater.inflate(R.layout.record_friends_gridview, parent, false);
                
                String username =  record.getName();
                TextView Username = (TextView) view.findViewById(R.id.userName);
                Username.setText(username);
                if (User.isDeveloperRecord(username)) {
                    Username.setTextColor(Color.parseColor("#008583")); //Developer
                }
                String last_online = record.getProfile().getDetails().getLastOnline();
                //Set online or offline status
                View Status = (View) view.findViewById(R.id.status);
                if (last_online.contains("seconds")){
                    Status.setBackgroundColor(Color.parseColor("#0D8500"));
                }else if (last_online.contains("minutes") && Integer.parseInt(last_online.replace(" minutes ago", "")) < 16){
                    Status.setBackgroundColor(Color.parseColor("#0D8500"));
                }else{
                    Status.setBackgroundColor(Color.parseColor("#D10000"));
                }
                TextView since = (TextView) view.findViewById(R.id.since);
                String friendSince = "";
                if ( record.getFriendSince() != null )
                    friendSince = MALDateTools.formatDateString(record.getFriendSince(), context, true);
                else
                    friendSince = getString(R.string.unknown);
                since.setText(friendSince.equals("") ? getString(R.string.unknown) : friendSince);

                last_online = MALDateTools.formatDateString(last_online, context, true);
                TextView lastonline = (TextView) view.findViewById(R.id.lastonline);
                lastonline.setText(last_online.equals("") ? record.getProfile().getDetails().getLastOnline() : last_online);
                Picasso picasso =  Picasso.with(context);
                picasso.load(record.getProfile().getAvatarUrl())
                    .error(R.drawable.cover_error)
                    .placeholder(R.drawable.cover_loading)
                    .into((ImageView) view.findViewById(R.id.profileImg));
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
    public void onFriendsNetworkTaskFinished(ArrayList<User> result) {
        if ( result != null ) {
            listarray = result;
            refresh(forcesync); // show crouton only if sync was forced
        } else {
            Crouton.makeText(this, R.string.crouton_error_Friends, Style.ALERT).show();
        }
    }
}
