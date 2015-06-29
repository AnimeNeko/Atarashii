package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.adapters.NavigationDrawerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.UserProfile.User;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;
import net.somethingdreadful.MAL.dialog.UpdateImageDialogFragment;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;
import net.somethingdreadful.MAL.tasks.UserNetworkTaskFinishedListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class Home extends ActionBarActivity implements ChooseDialogFragment.onClickListener, SwipeRefreshLayout.OnRefreshListener, IGF.IGFCallbackListener, APIAuthenticationErrorListener, View.OnClickListener, UserNetworkTaskFinishedListener, ViewPager.OnPageChangeListener {
    IGF af;
    IGF mf;
    Menu menu;
    Context context;
    View mPreviousView;
    ActionBar actionBar;
    DrawerLayout DrawerLayout;
    IGFPagerAdapter mIGFPagerAdapter;
    BroadcastReceiver networkReceiver;
    ActionBarDrawerToggle mDrawerToggle;
    NavigationDrawerAdapter mNavigationDrawerAdapter;

    @InjectView(R.id.about) RelativeLayout about;
    @InjectView(R.id.pager) ViewPager mViewPager;
    @InjectView(R.id.listview) ListView DrawerList;
    @InjectView(R.id.logout) RelativeLayout logout;
    @InjectView(R.id.settings) RelativeLayout settings;

    String username;

    boolean networkAvailable;
    boolean myList = true; //tracks if the user is on 'My List' or not
    boolean callbackAnimeError = false;
    boolean callbackMangaError = false;
    int callbackCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        if (AccountService.getAccount() != null) {
            actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            //The following is state handling code
            networkAvailable = savedInstanceState == null || savedInstanceState.getBoolean("networkAvailable", true);
            if (savedInstanceState != null)
                myList = savedInstanceState.getBoolean("myList");

            setContentView(R.layout.activity_home);
            // Creates the adapter to return the Animu and Mango fragments
            mIGFPagerAdapter = new IGFPagerAdapter(getFragmentManager(), true);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DrawerLayout = (DrawerLayout) inflater.inflate(R.layout.record_home_navigationdrawer, (DrawerLayout) findViewById(R.id.drawer_layout));
            ButterKnife.inject(this);

            DrawerLayout.setDrawerListener(new DrawerListener());
            DrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            username = AccountService.getUsername();
            ((TextView) DrawerLayout.findViewById(R.id.name)).setText(username);
            ((TextView) DrawerLayout.findViewById(R.id.siteName)).setText(getString(AccountService.isMAL() ? R.string.init_hint_myanimelist : R.string.init_hint_anilist));
            new UserNetworkTask(context, false, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username);

            logout.setOnClickListener(this);
            settings.setOnClickListener(this);
            about.setOnClickListener(this);
            Theme.setBackground(this, logout);
            Theme.setBackground(this, settings);
            Theme.setBackground(this, about);

            if (Theme.darkTheme) {
                DrawerLayout.findViewById(R.id.scrollView).setBackgroundColor(getResources().getColor(R.color.bg_dark));
                DrawerLayout.findViewById(R.id.divider).setBackgroundColor(getResources().getColor(R.color.bg_dark_card));
                ((TextView) DrawerLayout.findViewById(R.id.logoutText)).setTextColor(getResources().getColor(R.color.text_dark));
                ((TextView) DrawerLayout.findViewById(R.id.settingsText)).setTextColor(getResources().getColor(R.color.text_dark));
                ((TextView) DrawerLayout.findViewById(R.id.aboutText)).setTextColor(getResources().getColor(R.color.text_dark));
            }

            NavigationItems mNavigationContent = new NavigationItems(DrawerList, context);
            mNavigationDrawerAdapter = new NavigationDrawerAdapter(this, mNavigationContent.ITEMS);
            DrawerList.setAdapter(mNavigationDrawerAdapter);
            DrawerList.setOnItemClickListener(new DrawerItemClickListener());
            DrawerList.setOverScrollMode(View.OVER_SCROLL_NEVER);

            mDrawerToggle = new ActionBarDrawerToggle(this, DrawerLayout, R.string.drawer_open, R.string.drawer_close);
            mDrawerToggle.syncState();

            // Set up the ViewPager with the sections adapter.
            mViewPager.setAdapter(mIGFPagerAdapter);
            mViewPager.setPageMargin(32);
            mViewPager.setOnPageChangeListener(this);

            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkNetworkAndDisplayCrouton();
                }
            };
        } else {
            Intent firstRunInit = new Intent(this, FirstTimeInit.class);
            startActivity(firstRunInit);
            finish();
        }
        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        ComponentName cn = new ComponentName(this, SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        checkIGF();
        switch (item.getItemId()) {
            case R.id.listType_all:
                getRecords(true, TaskJob.GETLIST, 0);
                setChecked(item);
                break;
            case R.id.listType_inprogress:
                getRecords(true, TaskJob.GETLIST, 1);
                setChecked(item);
                break;
            case R.id.listType_completed:
                getRecords(true, TaskJob.GETLIST, 2);
                setChecked(item);
                break;
            case R.id.listType_onhold:
                getRecords(true, TaskJob.GETLIST, 3);
                setChecked(item);
                break;
            case R.id.listType_dropped:
                getRecords(true, TaskJob.GETLIST, 4);
                setChecked(item);
                break;
            case R.id.listType_planned:
                getRecords(true, TaskJob.GETLIST, 5);
                setChecked(item);
                break;
            case R.id.listType_rewatching:
                getRecords(true, TaskJob.GETLIST, 6);
                setChecked(item);
                break;
            case R.id.forceSync:
                synctask(true);
                break;
            case R.id.menu_inverse:
                if (af != null && mf != null) {
                    if (!AccountService.isMAL() && af.taskjob == TaskJob.GETMOSTPOPULAR) {
                        af.toggleAiringTime();
                    } else {
                        af.inverse();
                        mf.inverse();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On some devices the af & mf will change into null due inactivity.
     * This is a check to prevent any crashes and set it again.
     */
    public void checkIGF() {
        if (af == null || mf == null) {
            af = (IGF) mIGFPagerAdapter.getIGF(mViewPager, 0);
            mf = (IGF) mIGFPagerAdapter.getIGF(mViewPager, 1);
        }
    }

    public void getRecords(boolean clear, TaskJob task, int list) {
        checkIGF();
        if (af != null && mf != null) {
            af.getRecords(clear, task, list);
            mf.getRecords(clear, task, list);
            if (task == TaskJob.FORCESYNC)
                syncNotify();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNetworkAndDisplayCrouton();
        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();
        if (menu != null)
            menu.findItem(R.id.action_search).collapseActionView();
        unregisterReceiver(networkReceiver);
    }

    public void synctask(boolean clear) {
        getRecords(clear, TaskJob.FORCESYNC, af.list);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("networkAvailable", networkAvailable);
        state.putBoolean("myList", myList);
        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        if (af != null) {
            //All this is handling the ticks in the switch list menu
            switch (af.list) {
                case 0:
                    setChecked(menu.findItem(R.id.listType_all));
                    break;
                case 1:
                    setChecked(menu.findItem(R.id.listType_inprogress));
                    break;
                case 2:
                    setChecked(menu.findItem(R.id.listType_completed));
                    break;
                case 3:
                    setChecked(menu.findItem(R.id.listType_onhold));
                    break;
                case 4:
                    setChecked(menu.findItem(R.id.listType_dropped));
                    break;
                case 5:
                    setChecked(menu.findItem(R.id.listType_planned));
                    break;
                case 6:
                    setChecked(menu.findItem(R.id.listType_rewatching));
                    break;
            }
        }
        return true;
    }

    public void setChecked(MenuItem item) {
        item.setChecked(true);
    }

    public void myListChanged() {
        menu.findItem(R.id.menu_listType).setVisible(myList);
        menu.findItem(R.id.menu_inverse).setVisible(myList || (!AccountService.isMAL() && af.taskjob == TaskJob.GETMOSTPOPULAR));
        menu.findItem(R.id.forceSync).setVisible(myList && networkAvailable);
        menu.findItem(R.id.action_search).setVisible(networkAvailable);
    }

    private void syncNotify() {
        Intent notificationIntent = new Intent(context, Home.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder syncNotificationBuilder = new Notification.Builder(context).setOngoing(true)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.toast_info_SyncMessage));
        Notification syncNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                syncNotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            syncNotification = syncNotificationBuilder.build();
        } else {
            syncNotification = syncNotificationBuilder.getNotification();
        }
        nm.notify(R.id.notification_sync, syncNotification);
    }

    private void showLogoutDialog() {
        ChooseDialogFragment lcdf = new ChooseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.dialog_title_restore));
        bundle.putString("message", getString(R.string.dialog_message_logout));
        bundle.putString("positive", getString(R.string.dialog_label_logout));
        lcdf.setArguments(bundle);
        lcdf.setCallback(this);
        lcdf.show(getFragmentManager(), "fragment_LogoutConfirmationDialog");
    }

    public void checkNetworkAndDisplayCrouton() {
        if (MALApi.isNetworkAvailable(context) && !networkAvailable)
            synctask(false);
        networkAvailable = MALApi.isNetworkAvailable(context);
    }

    @Override
    public void onRefresh() {
        if (networkAvailable)
            synctask(false);
        else {
            if (af != null && mf != null) {
                af.toggleSwipeRefreshAnimation(false);
                mf.toggleSwipeRefreshAnimation(false);
            }
            Theme.Snackbar(this, R.string.toast_error_noConnectivity);
        }
    }

    @Override
    public void onIGFReady(IGF igf) {
        igf.setUsername(AccountService.getUsername());
        if (igf.listType.equals(MALApi.ListType.ANIME))
            af = igf;
        else
            mf = igf;
        // do forced sync after FirstInit
        if (PrefManager.getForceSync()) {
            if (af != null && mf != null) {
                PrefManager.setForceSync(false);
                PrefManager.commitChanges();
                synctask(true);
            }
        } else {
            if (igf.taskjob == null) {
                igf.getRecords(true, TaskJob.GETLIST, PrefManager.getDefaultList());
            }
        }
    }

    @Override
    public void onRecordsLoadingFinished(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {
        if (cancelled && !job.equals(TaskJob.FORCESYNC)) {
            return;
        }

        callbackCounter++;

        if (type.equals(MALApi.ListType.ANIME))
            callbackAnimeError = error;
        else
            callbackMangaError = error;

        if (callbackCounter >= 2) {
            callbackCounter = 0;

            if (job.equals(TaskJob.FORCESYNC)) {
                NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
                if (callbackAnimeError && callbackMangaError) // the sync failed completely
                    Theme.Snackbar(this, R.string.toast_error_SyncFailed);
                else if (callbackAnimeError || callbackMangaError) // one list failed to sync
                    Theme.Snackbar(this, callbackAnimeError ? R.string.toast_error_Anime_Sync : R.string.toast_error_Manga_Sync);
            } else {
                if (callbackAnimeError && callbackMangaError) // the sync failed completely
                    Theme.Snackbar(this, R.string.toast_error_Records);
                else if (callbackAnimeError || callbackMangaError) // one list failed to sync
                    Theme.Snackbar(this, callbackAnimeError ? R.string.toast_error_Anime_Records : R.string.toast_error_Manga_Records);
                // no else here, there is nothing to be shown when everything went well
            }
        }
    }

    @Override
    public void onItemClick(int id, MALApi.ListType listType, String username) {
        Intent startDetails = new Intent(context, DetailView.class);
        startDetails.putExtra("recordID", id);
        startDetails.putExtra("recordType", listType);
        startDetails.putExtra("username", username);
        startActivity(startDetails);
    }

    @Override
    public void onAPIAuthenticationError(MALApi.ListType type, TaskJob job) {
        startActivity(new Intent(this, Home.class).putExtra("updatePassword", true));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                showLogoutDialog();
                break;
            case R.id.settings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.Image:
                Intent Profile = new Intent(context, ProfileActivity.class);
                Profile.putExtra("username", username);
                startActivity(Profile);
                break;
            case R.id.NDimage:
                UpdateImageDialogFragment lcdf = new UpdateImageDialogFragment();
                lcdf.show(getFragmentManager(), "fragment_NDImage");
                break;
        }
        DrawerLayout.closeDrawers();
    }

    @Override
    public void onUserNetworkTaskFinished(User result) {
        ImageView image = (ImageView) findViewById(R.id.Image);
        ImageView image2 = (ImageView) findViewById(R.id.NDimage);
        try {
            Picasso.with(context)
                    .load(result.getProfile().getAvatarUrl())
                    .transform(new RoundedTransformation(result.getName()))
                    .into(image);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "Home.onUserNetworkTaskFinished(): " + e.getMessage());
        }
        if (PrefManager.getNavigationBackground() != null)
            Picasso.with(context)
                    .load(PrefManager.getNavigationBackground())
                    .into(image2);
        image.setOnClickListener(this);
        image2.setOnClickListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (menu != null)
            menu.findItem(R.id.listType_rewatching).setTitle(getString(position == 0 ? R.string.listType_rewatching : R.string.listType_rereading));
    }

    @Override
    public void onPageSelected(int position) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPositiveButtonClicked() {
        AccountService.clearData(true);
        startActivity(new Intent(this, Home.class));
        finish();
    }

    public class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!networkAvailable && position > 2) {
                position = 0;
                Theme.Snackbar(Home.this, R.string.toast_error_noConnectivity);
            }
            myList = ((position <= 3 && myList) || position == 0);
            checkIGF();
            // disable swipeRefresh for other lists
            af.setSwipeRefreshEnabled(myList);
            mf.setSwipeRefreshEnabled(myList);
            switch (position) {
                case 0:
                    getRecords(true, TaskJob.GETLIST, af.list);
                    break;
                case 1:
                    Intent Profile = new Intent(context, ProfileActivity.class);
                    Profile.putExtra("username", username);
                    startActivity(Profile);
                    break;
                case 2:
                    Intent Friends = new Intent(context, ProfileActivity.class);
                    Friends.putExtra("username", username);
                    Friends.putExtra("friends", username);
                    startActivity(Friends);
                    break;
                case 3:
                    if (AccountService.isMAL()) {
                        Intent Forum = new Intent(context, ForumActivity.class);
                        startActivity(Forum);
                    } else {
                        Theme.Snackbar(Home.this, R.string.toast_info_disabled);
                    }
                    break;
                case 4:
                    getRecords(true, TaskJob.GETTOPRATED, af.list);
                    break;
                case 5:
                    getRecords(true, TaskJob.GETMOSTPOPULAR, af.list);
                    break;
                case 6:
                    getRecords(true, TaskJob.GETJUSTADDED, af.list);
                    break;
                case 7:
                    getRecords(true, TaskJob.GETUPCOMING, af.list);
                    break;
            }
            myListChanged();

            /*
             * This part is for figuring out which item in the nav drawer is selected and highlighting it with colors.
             */
            if (position != 1 && position != 2 && position != 3) {
                if (mPreviousView != null)
                    mPreviousView.setBackgroundColor(Color.parseColor("#00000000"));
                if (Theme.darkTheme)
                    view.setBackgroundColor(getResources().getColor(R.color.bg_dark_card));
                else
                    view.setBackgroundColor(Color.parseColor("#E8E8E8"));
                mPreviousView = view;
            }

            DrawerLayout.closeDrawers();
        }
    }

    private class DrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {
            mDrawerToggle.onDrawerOpened(drawerView);
            actionBar.setTitle(getTitle());
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            mDrawerToggle.onDrawerClosed(drawerView);
            actionBar.setTitle(getTitle());
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
}
