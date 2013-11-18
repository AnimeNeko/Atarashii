package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.BaseMALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.MangaRecord;
import net.somethingdreadful.MAL.record.ProfileMALRecord;
import net.somethingdreadful.MAL.sql.MALSqlHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Home extends BaseActionBarSearchView
implements ActionBar.TabListener, ItemGridFragment.IItemGridFragment,
LogoutConfirmationDialogFragment.LogoutConfirmationDialogListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    HomeSectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Context context;
    PrefManager mPrefManager;
    public MALManager mManager;
    private boolean init = false;
    ItemGridFragment af;
    ItemGridFragment mf;
    public boolean instanceExists;
    boolean networkAvailable;
    BroadcastReceiver networkReceiver;
    MenuItem searchItem;
    
    int AutoSync = 0; //run or not to run.
    
    private DrawerLayout mDrawerLayout;
    private ListView listView;
    private SherlockActionBarDrawerToggle mDrawerToggle;
    private ActionBarHelper mActionBar;
    View mActiveView;
    View mPreviousView;
    boolean myList = true; //tracks if the user is on 'My List' or not
    public static final String[] DRAWER_OPTIONS = 
        {
                "My Profile",   
                "My List",
                "My Friends",
                "Top Rated",
                "Most Popular",
                "Just Added",
                "Upcoming"
        };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        
        mPrefManager = new PrefManager(context);
        init = mPrefManager.getInit();

        //The following is state handling code
        instanceExists = savedInstanceState != null && savedInstanceState.getBoolean("instanceExists", false);
        networkAvailable = savedInstanceState == null || savedInstanceState.getBoolean("networkAvailable", true);
        
        if (init) {
            setContentView(R.layout.activity_home);
            // Creates the adapter to return the Animu and Mango fragments
            mSectionsPagerAdapter = new HomeSectionsPagerAdapter(getSupportFragmentManager());
            
            mDrawerLayout= (DrawerLayout)findViewById(R.id.drawer_layout);
            mDrawerLayout.setDrawerListener(new DemoDrawerListener());
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            HomeListViewArrayAdapter adapter = new HomeListViewArrayAdapter(this,R.layout.list_item,DRAWER_OPTIONS);
            
            listView = (ListView)findViewById(R.id.left_drawer);
            listView.setAdapter(adapter);
    		listView.setOnItemClickListener(new DrawerItemClickListener());
    		listView.setCacheColorHint(0);
    		listView.setScrollingCacheEnabled(false);
    		listView.setScrollContainer(false);
    		listView.setFastScrollEnabled(true);
    		listView.setSmoothScrollbarEnabled(true);
            	
    		mActionBar = createActionBarHelper();
    		mActionBar.init();
    		
    		mDrawerToggle = new SherlockActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer_light, R.string.drawer_open, R.string.drawer_close);
    		mDrawerToggle.syncState();

            mManager = new MALManager(context);

            // Set up the action bar.
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setPageMargin(32);

            // When swiping between different sections, select the corresponding
            // tab.
            // We can also use ActionBar.Tab#select() to do this if we have a
            // reference to the
            // Tab.
            mViewPager
            .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
            });

            // Add tabs for the animu and mango lists
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title
                // defined by the adapter.
                // Also specify this Activity object, which implements the
                // TabListener interface, as the
                // listener for when this tab is selected.
                actionBar.addTab(actionBar.newTab()
                        .setText(mSectionsPagerAdapter.getPageTitle(i))
                        .setTabListener(this));
            }
            
            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkNetworkAndDisplayCrouton();
                }
            };
            
            if (savedInstanceState != null) {
                AutoSync = savedInstanceState.getInt("AutoSync");
            }
        } else { //If the app hasn't been configured, take us to the first run screen to sign in.
            Intent firstRunInit = new Intent(this, FirstTimeInit.class);
            firstRunInit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(firstRunInit);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_home, menu);
        searchItem = menu.findItem(R.id.action_search);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public BaseMALApi.ListType getCurrentListType() {
        String listName = getSupportActionBar().getSelectedTab().getText().toString();
        return BaseMALApi.getListTypeByString(listName);
    }
    
	public boolean isConnectedWifi() {
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo Wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (Wifi.isConnected()&& mPrefManager.getonly_wifiEnabled() ) {
            return true;
        } else {
            return false;
        }
    }
    


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, Settings.class));
                break;

            case R.id.menu_logout:
                showLogoutDialog();
                break;

            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.listType_all:
                if (af != null && mf != null) {
                    af.getRecords(0, "anime", false, this.context);
                    mf.getRecords(0, "manga", false, this.context);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_inprogress:
                if (af != null && mf != null) {
                    af.getRecords(1, "anime", false, this.context);
                    mf.getRecords(1, "manga", false, this.context);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_completed:
                if (af != null && mf != null) {
                    af.getRecords(2, "anime", false, this.context);
                    mf.getRecords(2, "manga", false, this.context);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_onhold:
                if (af != null && mf != null) {
                    af.getRecords(3, "anime", false, this.context);
                    mf.getRecords(3, "manga", false, this.context);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_dropped:
                if (af != null && mf != null) {
                    af.getRecords(4, "anime", false, this.context);
                    mf.getRecords(4, "manga", false, this.context);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_planned:
                if (af != null && mf != null) {
                    af.getRecords(5, "anime", false, this.context);
                    mf.getRecords(5, "manga", false, this.context);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.forceSync:
                if (af != null && mf != null) {
                    af.getRecords(af.currentList, "anime", true, this.context);
                    mf.getRecords(mf.currentList, "manga", true, this.context);
                    syncNotify();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNetworkAndDisplayCrouton();
        if (instanceExists) {
        	af.getRecords(af.currentList, "anime", false, Home.this.context);
            mf.getRecords(mf.currentList, "manga", false, Home.this.context);
        }
        registerReceiver(networkReceiver,  new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        if (mSearchView != null) {
        	mSearchView.clearFocus();
            mSearchView.setFocusable(false);
            mSearchView.setQuery("", false);
            searchItem.collapseActionView();      
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        instanceExists = true;
        unregisterReceiver(networkReceiver);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void fragmentReady() {
        //Interface implementation for knowing when the dynamically created fragment is finished loading
        //We use instantiateItem to return the fragment. Since the fragment IS instantiated, the method returns it.
    	af = (net.somethingdreadful.MAL.ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
		mf = (net.somethingdreadful.MAL.ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 1);
		try {
			if (AutoSync == 0 && isNetworkAvailable() && networkAvailable == true && mPrefManager.getsynchronisationEnabled()){ 
        		if (mPrefManager.getsynchronisationEnabled() && mPrefManager.getonly_wifiEnabled() == false){ //connected to Wi-Fi and sync only on Wi-Fi checked.
        			synctask();
        		}else if (mPrefManager.getonly_wifiEnabled() && isConnectedWifi() && mPrefManager.getsynchronisationEnabled()){ //connected and sync always.
        			synctask();
        		}
        	}else if (mPrefManager.getInitsync() && AutoSync == 0 && isNetworkAvailable()){
        		mPrefManager.setInitsync(false);
        		mPrefManager.commitChanges();
        		synctask();
        	}else{
        		//will do nothing, sync is turned off or (sync only on Wi-Fi checked) and there is no Wi-Fi.
        	}
        }catch (Exception e){
        	Crouton.makeText(this, "Error: autosynctask faild!", Style.ALERT).show();
        }
    }
    
    public void synctask(){
        af.getRecords(af.currentList, "anime", true, this.context);
        mf.getRecords(mf.currentList, "manga", true, this.context);
        syncNotify();
        AutoSync = 1;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("instanceExists", true);
        state.putBoolean("networkAvailable", networkAvailable);
        state.putInt("AutoSync", AutoSync);
        state.putInt("myList", af.currentList);
        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem item  = menu.findItem(R.id.menu_listType);
    	if(!myList){//if not on my list then disable menu items like listType, etc
    		item.setEnabled(false);
    		item.setVisible(false);
    	}
    	else{
    		item.setEnabled(true);
    		item.setVisible(true);
    	}
        if (af != null) {
            //All this is handling the ticks in the switch list menu
            switch (af.currentList) {
                case 0:
                    menu.findItem(R.id.listType_all).setChecked(true);
                    break;
                case 1:
                    menu.findItem(R.id.listType_inprogress).setChecked(true);
                    break;
                case 2:
                    menu.findItem(R.id.listType_completed).setChecked(true);
                    break;
                case 3:
                    menu.findItem(R.id.listType_onhold).setChecked(true);
                    break;
                case 4:
                    menu.findItem(R.id.listType_dropped).setChecked(true);
                    break;
                case 5:
                    menu.findItem(R.id.listType_planned).setChecked(true);
            }
        }

        if (networkAvailable) {
        	if (myList){
        		menu.findItem(R.id.forceSync).setVisible(true);
        	}else{
        		menu.findItem(R.id.forceSync).setVisible(false);
        	}
            menu.findItem(R.id.action_search).setVisible(true);
        }
        else {
            menu.findItem(R.id.forceSync).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
            AutoSync = 1; 
        }
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public void onLogoutConfirmed() {
        mPrefManager.setInit(false);
        mPrefManager.setUser("");
        mPrefManager.setPass("");
        mPrefManager.commitChanges();
        context.deleteDatabase(MALSqlHelper.getHelper(context).getDatabaseName());
        startActivity(new Intent(this, Home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void syncNotify() {
        Crouton.makeText(this, R.string.toast_SyncMessage, Style.INFO).show();

        Intent notificationIntent = new Intent(context, Home.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification syncNotification = new NotificationCompat.Builder(context).setOngoing(true)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.toast_SyncMessage))
                .getNotification();
        nm.notify(R.id.notification_sync, syncNotification);
        myList = true;
        supportInvalidateOptionsMenu();
    }

    private void showLogoutDialog() {
        FragmentManager fm = getSupportFragmentManager();

        LogoutConfirmationDialogFragment lcdf = new LogoutConfirmationDialogFragment();

        if (Build.VERSION.SDK_INT >= 11) {
            lcdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        } else {
            lcdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        lcdf.show(fm, "fragment_LogoutConfirmationDialog");
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
    
    public void maketext(String string) { //for the private class
    	Crouton.makeText(this, string, Style.INFO).show();
    }
    

    public void checkNetworkAndDisplayCrouton() {
    	af = (net.somethingdreadful.MAL.ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
		mf = (net.somethingdreadful.MAL.ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 1);
        if (!isNetworkAvailable() && networkAvailable == true) {
    		Crouton.makeText(this, R.string.crouton_noConnectivityOnRun, Style.ALERT).show();
			if (af.getMode() > 0) {
	            af.setMode(0);
				mf.setMode(0);
				af.getRecords(af.currentList, "anime", false, Home.this.context);
	            mf.getRecords(mf.currentList, "manga", false, Home.this.context);
	            myList = true;
	        }
        } else if (isNetworkAvailable() && networkAvailable == false) {
            Crouton.makeText(this, R.string.crouton_connectionRestored, Style.INFO).show();
            af.getRecords(af.currentList, "anime", true, this.context);
            mf.getRecords(mf.currentList, "manga", true, this.context);
            syncNotify();
        }

        if (!isNetworkAvailable()) {
            networkAvailable = false;
        } else {
            networkAvailable = true;
        }
        supportInvalidateOptionsMenu();
    }
    
    /* thread & methods to fetch most popular anime/manga*/
    //in order to reuse the code , 1 signifies a getPopular job and 2 signifies a getTopRated job. Probably a better way to do this
    public void getMostPopular(BaseMALApi.ListType listType){
    	networkThread animethread = new networkThread(1);
        animethread.setListType(BaseMALApi.ListType.ANIME);
        animethread.execute(query);
                  
        /*networkThread mangathread = new networkThread(1);
        mangathread.setListType(BaseMALApi.ListType.MANGA);
        mangathread.execute(query);*/
        //API doesn't support getting popular manga :/  
    }
    public void getTopRated(BaseMALApi.ListType listType){
    	networkThread animethread = new networkThread(2);
        animethread.setListType(BaseMALApi.ListType.ANIME);
        animethread.execute(query);
        
        /*networkThread mangathread = new networkThread(2);
        mangathread.setListType(BaseMALApi.ListType.MANGA);
        mangathread.execute(query);*/
        //API doesn't support getting top rated manga :/  
    }
    public void getJustAdded(BaseMALApi.ListType listType){
    	networkThread animethread = new networkThread(3);
    	animethread.setListType(BaseMALApi.ListType.ANIME);
    	animethread.execute(query);
              
    	/*networkThread mangathread = new networkThread(3);
     	mangathread.setListType(BaseMALApi.ListType.MANGA);
     	mangathread.execute(query);*/
    	//API doesn't support getting popular manga :/  
    }
    public void getUpcoming(BaseMALApi.ListType listType){
    	networkThread animethread = new networkThread(4);
    	animethread.setListType(BaseMALApi.ListType.ANIME);
    	animethread.execute(query);
              
    	/*networkThread mangathread = new networkThread(4);
     	mangathread.setListType(BaseMALApi.ListType.MANGA);
     	mangathread.execute(query);*/
    	//API doesn't support getting popular manga :/  
    }
    
    public class networkThread extends AsyncTask<String, Void, Void> {
        JSONArray _result;
        int job;
        public networkThread(int job){
        	this.job = job;
        }

        public MALApi.ListType getListType() {
            return listType;
        }

        public void setListType(MALApi.ListType listType) {
            this.listType = listType;
        }

        MALApi.ListType listType;

        @Override
        protected Void doInBackground(String... params) {
        	try{
        		String query = params[0];
        		MALApi api = new MALApi(context);
        		switch (job){
        		case 1:
        			_result = api.getMostPopular(getListType(),1); //if job == 1 then get the most popular
        			break;
        		case 2:
        			_result = api.getTopRated(getListType(),1); //if job == 2 then get the top rated
        			break;
        		case 3:
        			_result = api.getJustAdded(getListType(),1); //if job == 3 then get the Just Added
        			break;
        		case 4:
        			_result = api.getUpcoming(getListType(),1); //if job == 4 then get the upcoming
        			break;
        		}
        	}catch (Exception e){
				Log.e("MALX", "Exception caught in doInBackground() in Home.java");
        	}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String type = MALApi.getListTypeString(getListType());
            try {
                switch (listType) {
                    case ANIME: {
                        ArrayList<AnimeRecord> list = new ArrayList<AnimeRecord>();
                        
                        if (_result.length() == 0) {
							Log.w("MALX", "No anime records, trying to fetch again.");
                        	af.scrollToTop();
                			mf.scrollToTop();
                			if (af.getMode()== 1){
                				getTopRated(BaseMALApi.ListType.ANIME);
                			} else if (af.getMode()== 2){
                				getMostPopular(BaseMALApi.ListType.ANIME);
                			} else if (af.getMode()== 3){
                				getJustAdded(BaseMALApi.ListType.ANIME);
                			} else if (af.getMode()== 4){
                				getUpcoming(BaseMALApi.ListType.ANIME);
                			}
                			af.scrollListener.resetPageNumber();
                        } else {
                        	for (int i = 0; i < _result.length(); i++) {
                                JSONObject genre = (JSONObject) _result.get(i);
                                AnimeRecord record = new AnimeRecord(mManager.getRecordDataFromJSONObject(genre, type));
                                list.add(record);
                            }
                        }
                        af.setAnimeRecords(list);
                        break;
                    }
                    case MANGA: {
                        ArrayList<MangaRecord> list = new ArrayList<MangaRecord>();
                        
                        if (_result.length() == 0) {
							Log.w("MALX", "No manga records.");
                        }
                        else {
                        	for (int i = 0; i < _result.length(); i++) {
                                JSONObject genre =  (JSONObject) _result.get(i);
                                MangaRecord record = new MangaRecord(mManager.getRecordDataFromJSONObject(genre, type));
                                list.add(record);
                            }	
                        }
                        mf.setMangaRecords(list);
                        break;
                    }
                }
                
            } catch (Exception e) {
                Log.e(SearchActivity.class.getName(), Log.getStackTraceString(e));
            }
            Home.this.af.scrollListener.notifyMorePages();
        }
    }
    
    /*private classes for nav drawer*/
    private ActionBarHelper createActionBarHelper() {
		return new ActionBarHelper();
	}
     
    public class DrawerItemClickListener implements ListView.OnItemClickListener {
    	
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			/* do stuff when drawer item is clicked here */
			af.scrollToTop();
			mf.scrollToTop();
			if (!isNetworkAvailable()) {
				if (position==0 || position==1 || position==2){
				}else{
					position = 1;
					maketext("No network connection available!");
				}
	        }
			switch (position){
			case 0:
				ProfileMALRecord.username = mPrefManager.getUser();
				Intent Profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
				startActivity(Profile);
				break;
			case 1:
				af.getRecords(af.currentList, "anime", false, Home.this.context);
                mf.getRecords(mf.currentList, "manga", false, Home.this.context);
                myList = true;
                af.setMode(0);
				mf.setMode(0);
				break;
			case 2:
				Intent Friends = new Intent(context, net.somethingdreadful.MAL.FriendsActivity.class);
				startActivity(Friends);
				break;
			case 3:
				getTopRated(BaseMALApi.ListType.ANIME);
				mf.setMangaRecords(new ArrayList<MangaRecord>()); ////basically, since you can't get popular manga this is just a temporary measure to make the manga set empty, otherwise it would continue to display YOUR manga list 
				myList = false;
				af.setMode(1);
				mf.setMode(1);
				af.scrollListener.resetPageNumber();
				mf.scrollListener.resetPageNumber();
				break;
			case 4:
				getMostPopular(BaseMALApi.ListType.ANIME);
				mf.setMangaRecords(new ArrayList<MangaRecord>()); //basically, since you can't get popular manga this is just a temporary measure to make the manga set empty, otherwise it would continue to display YOUR manga list 
				myList = false;
				af.setMode(2);
				mf.setMode(2);
				af.scrollListener.resetPageNumber();
				mf.scrollListener.resetPageNumber();
				break;
			case 5:
				getJustAdded(BaseMALApi.ListType.ANIME);
				mf.setMangaRecords(new ArrayList<MangaRecord>()); //basically, since you can't get Just Added manga this is just a temporary measure to make the manga set empty, otherwise it would continue to display YOUR manga list 
				myList = false;
				af.setMode(3);
				mf.setMode(3);
				af.scrollListener.resetPageNumber();
				mf.scrollListener.resetPageNumber();
				break;
			case 6:
				getUpcoming(BaseMALApi.ListType.ANIME);
				mf.setMangaRecords(new ArrayList<MangaRecord>()); //basically, since you can't get Upcoming manga this is just a temporary measure to make the manga set empty, otherwise it would continue to display YOUR manga list 
				myList = false;
				af.setMode(4);
				mf.setMode(4);
				af.scrollListener.resetPageNumber();
				mf.scrollListener.resetPageNumber();
				break;
			}
			Home.this.supportInvalidateOptionsMenu();
			//This part is for figuring out which item in the nav drawer is selected and highlighting it with colors
			mPreviousView = mActiveView;
			if (mPreviousView != null)
				mPreviousView.setBackgroundColor(Color.parseColor("#333333")); //dark color
			mActiveView = view;
			mActiveView.setBackgroundColor(Color.parseColor("#38B2E1")); //blue color
			mDrawerLayout.closeDrawer(listView);
		}
	}
    
    private class DemoDrawerListener implements DrawerLayout.DrawerListener {
    	final ActionBar actionBar = getSupportActionBar();
		@Override
		public void onDrawerOpened(View drawerView) {
			mDrawerToggle.onDrawerOpened(drawerView);
			mActionBar.onDrawerOpened();
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			mDrawerToggle.onDrawerClosed(drawerView);
			mActionBar.onDrawerClosed();
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			mDrawerToggle.onDrawerStateChanged(newState);
		}
	}
    
    private class ActionBarHelper {
		private final ActionBar mActionBar;
		private CharSequence mDrawerTitle;
		private CharSequence mTitle;

		private ActionBarHelper() {
			mActionBar = getSupportActionBar();
		}

		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
			mTitle = mDrawerTitle = getTitle();
		}

		/**
		 * When the drawer is closed we restore the action bar state reflecting
		 * the specific contents in view.
		 */
		public void onDrawerClosed() {
			mActionBar.setTitle(mTitle);	
		}

		/**
		 * When the drawer is open we set the action bar to a generic title. The
		 * action bar should only contain data relevant at the top level of the
		 * nav hierarchy represented by the drawer, as the rest of your content
		 * will be dimmed down and non-interactive.
		 */
		public void onDrawerOpened() {
			mActionBar.setTitle(mDrawerTitle);
		}
	}
}
