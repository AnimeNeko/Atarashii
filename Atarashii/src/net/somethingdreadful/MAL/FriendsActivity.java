package net.somethingdreadful.MAL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

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
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FriendsActivity extends SherlockFragmentActivity implements BaseItemGridFragment.IBaseItemGridFragment, ActionBar.TabListener {
	
    SearchSectionsPagerAdapter mSectionsPagerAdapter;
    SharedPreferences preferences;
    ViewPager mViewPager;
    Context context;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> UserList;
    boolean remove = false;
    int indexp; //position
    String selected; //get selected username clicked
    String text; //get selected username long
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();   
        setContentView(R.layout.activity_friends);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true); //go to home to actionbar
        setTitle("My friends"); //set title
        UserList = new ArrayList<String>();
        
        restorelist(); //restore users from preferences
        ListView userList=(ListView)findViewById(R.id.listview);
        refresh(false); // set listview

         userList.setOnItemClickListener(new OnItemClickListener(){ //start the profile with your friend
        	 public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3){      
					String selected = UserList.get(position);
	        		Editor editor1 = getSharedPreferences("Profile", MODE_PRIVATE).edit().putString("Profileuser", selected);editor1.commit();
	        		Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
	 				startActivity(profile);
             }
         });
         userList.setOnItemLongClickListener(new OnItemLongClickListener(){ //longclick = remove selected friend
        	 public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3){
        		 selected = UserList.get(index);
        		 removedialog(selected); //show confirm dialog
        		 indexp = index;
        		 return true;
        	 }
         });
    }
	
    public void remove(){ //removes a user
    	UserList.remove(indexp);
    	refresh(true);
    	preferences = getSharedPreferences("Profile_" + selected, MODE_PRIVATE);
    	preferences.edit().clear();
    }
    
    public void refresh(boolean save){ //refresh list , if boolean is true than also save
    	ListView userList=(ListView)findViewById(R.id.listview);
    	Collections.sort(UserList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_friends_with_text_item, R.id.userName  ,UserList);
        userList.setAdapter(arrayAdapter); 
        if (save == true){
        	savelist();
        }
    }
    
    public void savelist(){//save the arraylist
    	try{
    		SharedPreferences.Editor sEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
    		Collections.sort(UserList);
    		for(int i=0;i <UserList.size();i++){
    			sEdit.putString("val"+i,UserList.get(i));
    		}
    	 	sEdit.putInt("size",UserList.size());
    	 	sEdit.commit();
    	 	maketext("Userprofile saved!", 3);
    	}catch (Exception e){
    		maketext("Error while saving the list!", 2);
    	}
    }
    
    public void restorelist(){ //restore the list(get the arrays and restore them)
    	try{
    		preferences = PreferenceManager.getDefaultSharedPreferences(context);
    		int size = preferences.getInt("size",0);
    		for(int j=0;j<size;j++)
    		{
    			UserList.add(preferences.getString("val"+j,"Error"));
    		}
    	}catch (Exception e){
    		maketext("Error while restoring the list!", 2);
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_friends_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			finish();
		} else if (itemId == R.id.action_addToList) {
			inputdialog();
		}
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void fragmentReady() {
		
	}
	
    public class Verify extends AsyncTask<String, Void, String> { //check username
    	protected String doInBackground(String... urls) {
    				HttpClient client = new DefaultHttpClient();
    				String json = "";
    				String appa = "";
    				try {
    					String line = "";
    					HttpGet request = new HttpGet(urls[0]);
    					HttpResponse response = client.execute(request);//get response
    					BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    					while ((line = rd.readLine()) != null) {
    						json += line + System.getProperty("line.separator"); //save response
    						JSONObject jsonObject = new JSONObject(json);
    						appa = jsonObject.getString("avatar_url");//get the avatar URL for check
    					}
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
            return appa; // appa == "" or appa == the avatar URL
        }

        protected void onProgressUpdate(Void... progress) {
			
        }

		protected void onPostExecute(String check) { //check = avatar
			if (check =="" && isNetworkAvailable()){ //checks if avatar == "" (This method also works when the api is offline)
				maketext("Invailid username!",2);
			}else if (check =="" && !isNetworkAvailable()){
				maketext("No network connection available!",2);
			}else{
				UserList.add(text); //user exist, add the user
				refresh(true); //refresh the list
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
		        text =m_Text;
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

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	}
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}
