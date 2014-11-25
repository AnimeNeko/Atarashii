package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.adapters.NavigationDrawerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.LogoutConfirmationDialogFragment;
import net.somethingdreadful.MAL.dialog.UpdatePasswordDialogFragment;
import net.somethingdreadful.MAL.sql.MALSqlHelper;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Home extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener, IGFCallbackListener, APIAuthenticationErrorListener {

    IGF af;
    IGF mf;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    IGFPagerAdapter mIGFPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Context context;
    PrefManager mPrefManager;
    Menu menu;
    BroadcastReceiver networkReceiver;
    DrawerLayout DrawerLayout;
    ListView DrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    View mPreviousView;
    ActionBar actionBar;
    NavigationDrawerAdapter mNavigationDrawerAdapter;
    SearchView searchView;

    boolean instanceExists;
    boolean networkAvailable;
    boolean myList = true; //tracks if the user is on 'My List' or not

    boolean callbackAnimeError = false;
    boolean callbackMangaError = false;
    int callbackCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        if (AccountService.getAccount(context) != null) {
            mPrefManager = new PrefManager(context);
            actionBar = getSupportActionBar();
            //setSupportActionBar(toolbar);
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);

                //actionBar.setHomeButtonEnabled(true);
            }
            //The following is state handling code
            instanceExists = savedInstanceState != null && savedInstanceState.getBoolean("instanceExists", false);
            networkAvailable = savedInstanceState == null || savedInstanceState.getBoolean("networkAvailable", true);
            if (savedInstanceState != null) {
                myList = savedInstanceState.getBoolean("myList");
            }

            setContentView(R.layout.activity_home);
            // Creates the adapter to return the Animu and Mango fragments
            mIGFPagerAdapter = new IGFPagerAdapter(getFragmentManager());

            DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            DrawerLayout.setDrawerListener(new DemoDrawerListener());
            DrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

            DrawerList = (ListView) findViewById(R.id.left_drawer);

            NavigationItems mNavigationContent = new NavigationItems();
            mNavigationDrawerAdapter = new NavigationDrawerAdapter(this, mNavigationContent.ITEMS);
            DrawerList.setAdapter(mNavigationDrawerAdapter);
            DrawerList.setOnItemClickListener(new DrawerItemClickListener());
            DrawerList.setCacheColorHint(0);
            DrawerList.setScrollingCacheEnabled(false);
            DrawerList.setScrollContainer(false);
            DrawerList.setFastScrollEnabled(true);
            DrawerList.setSmoothScrollbarEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, DrawerLayout, R.string.drawer_open, R.string.drawer_close);
            mDrawerToggle.syncState();

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mIGFPagerAdapter);
            mViewPager.setPageMargin(32);

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
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
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
                    af.getRecords(true, TaskJob.GETLIST, 0);
                    mf.getRecords(true, TaskJob.GETLIST, 0);
                    setChecked(item);
                }
                break;
            case R.id.listType_inprogress:
                if (af != null && mf != null) {
                    af.getRecords(true, TaskJob.GETLIST, 1);
                    mf.getRecords(true, TaskJob.GETLIST, 1);
                    setChecked(item);
                }
                break;
            case R.id.listType_completed:
                if (af != null && mf != null) {
                    af.getRecords(true, TaskJob.GETLIST, 2);
                    mf.getRecords(true, TaskJob.GETLIST, 2);
                    setChecked(item);
                }
                break;
            case R.id.listType_onhold:
                if (af != null && mf != null) {
                    af.getRecords(true, TaskJob.GETLIST, 3);
                    mf.getRecords(true, TaskJob.GETLIST, 3);
                    setChecked(item);
                }
                break;
            case R.id.listType_dropped:
                if (af != null && mf != null) {
                    af.getRecords(true, TaskJob.GETLIST, 4);
                    mf.getRecords(true, TaskJob.GETLIST, 4);
                    setChecked(item);
                }
                break;
            case R.id.listType_planned:
                if (af != null && mf != null) {
                    af.getRecords(true, TaskJob.GETLIST, 5);
                    mf.getRecords(true, TaskJob.GETLIST, 5);
                    setChecked(item);
                }
                break;
            case R.id.forceSync:
                synctask(true, true);
                break;
            case R.id.menu_inverse:
                if (af != null && mf != null) {
                    af.inverse();
                    mf.inverse();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
        instanceExists = true;
        unregisterReceiver(networkReceiver);
    }

    public void synctask(boolean clear, boolean notify) {
        if (af != null && mf != null) {
            af.getRecords(clear, TaskJob.FORCESYNC, af.list);
            mf.getRecords(clear, TaskJob.FORCESYNC, mf.list);
            syncNotify(notify);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("instanceExists", true);
        state.putBoolean("networkAvailable", networkAvailable);
        state.putBoolean("myList", myList);
        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        myListChanged();
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
            }
        }
        return true;
    }

    public void setChecked(MenuItem item) {
        item.setChecked(true);
    }

    public void myListChanged() {
        MenuItem item = menu.findItem(R.id.menu_listType);
        if (!myList) {//if not on my list then disable menu items like listType, etc
            item.setEnabled(false);
            item.setVisible(false);
        } else {
            item.setEnabled(true);
            item.setVisible(true);
        }
        if (networkAvailable) {
            if (myList) {
                menu.findItem(R.id.menu_inverse).setVisible(true);
                menu.findItem(R.id.forceSync).setVisible(true);
            } else {
                menu.findItem(R.id.menu_inverse).setVisible(false);
                menu.findItem(R.id.forceSync).setVisible(false);
            }
            menu.findItem(R.id.action_search).setVisible(true);
        } else {
            menu.findItem(R.id.forceSync).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        }
    }

    @SuppressLint("NewApi")
    public void onLogoutConfirmed() {
        if (af != null)
            af.cancelNetworkTask();
        if (mf != null)
            mf.cancelNetworkTask();
        MALSqlHelper.getHelper(context).deleteDatabase(context);
        AccountService.deleteAccount(context);
        startActivity(new Intent(this, Home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void syncNotify(boolean showCrouton) {
        if (showCrouton)
            Crouton.makeText(this, R.string.crouton_info_SyncMessage, Style.INFO).show();

        Intent notificationIntent = new Intent(context, Home.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder syncNotificationBuilder = new Notification.Builder(context).setOngoing(true)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.crouton_info_SyncMessage));
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
        FragmentManager fm = getFragmentManager();
        LogoutConfirmationDialogFragment lcdf = new LogoutConfirmationDialogFragment();
        lcdf.show(fm, "fragment_LogoutConfirmationDialog");
    }

    public void checkNetworkAndDisplayCrouton() {
        if (!MALApi.isNetworkAvailable(context) && networkAvailable) {
            Crouton.makeText(this, R.string.crouton_error_noConnectivityOnRun, Style.ALERT).show();
        } else if (MALApi.isNetworkAvailable(context) && !networkAvailable) {
            Crouton.makeText(this, R.string.crouton_info_connectionRestored, Style.INFO).show();
            synctask(false, true);
        }
        networkAvailable = MALApi.isNetworkAvailable(context);
    }

    @Override
    public void onRefresh() {
        if (networkAvailable)
            synctask(false, false);
        else {
            if (af != null && mf != null) {
                af.toggleSwipeRefreshAnimation(false);
                mf.toggleSwipeRefreshAnimation(false);
            }
            Crouton.makeText(Home.this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
        }
    }

    @Override
    public void onIGFReady(IGF igf) {
        igf.setUsername(AccountService.getUsername(context));
        if (igf.listType.equals(MALApi.ListType.ANIME))
            af = igf;
        else
            mf = igf;
        // do forced sync after FirstInit
        if (mPrefManager.getForceSync()) {
            if (af != null && mf != null) {
                mPrefManager.setForceSync(false);
                mPrefManager.commitChanges();
                synctask(true, true);
            }
        } else {
            if (igf.taskjob == null) {
                igf.getRecords(true, TaskJob.GETLIST, mPrefManager.getDefaultList());
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
                    Crouton.makeText(this, R.string.crouton_error_SyncFailed, Style.ALERT).show();
                else if (callbackAnimeError || callbackMangaError) // one list failed to sync
                    Crouton.makeText(this, callbackAnimeError ? R.string.crouton_error_Anime_Sync : R.string.crouton_error_Manga_Sync, Style.ALERT).show();
                else // everything went well
                    Crouton.makeText(this, R.string.crouton_info_SyncDone, Style.CONFIRM).show();
            } else {
                if (callbackAnimeError && callbackMangaError) // the sync failed completely
                    Crouton.makeText(this, R.string.crouton_error_Records, Style.ALERT).show();
                else if (callbackAnimeError || callbackMangaError) // one list failed to sync
                    Crouton.makeText(this, callbackAnimeError ? R.string.crouton_error_Anime_Records : R.string.crouton_error_Manga_Records, Style.ALERT).show();
                // no else here, there is nothing to be shown when everything went well
            }
        }
    }

    @Override
    public void onAPIAuthenticationError(MALApi.ListType type, TaskJob job) {
        // check if it is already showing
        if (getFragmentManager().findFragmentByTag("fragment_updatePassword") == null) {
            FragmentManager fm = getFragmentManager();
            UpdatePasswordDialogFragment passwordFragment = new UpdatePasswordDialogFragment();
            passwordFragment.show(fm, "fragment_updatePassword");
        }
    }

    public class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!networkAvailable && position > 2) {
                position = 1;
                Crouton.makeText(Home.this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
            }
            myList = ((position <= 2 && myList) || position == 1);
            myListChanged();
            // disable swipeRefresh for other lists
            af.setSwipeRefreshEnabled(myList);
            mf.setSwipeRefreshEnabled(myList);
            switch (position) {
                case 0:
                    Intent Profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
                    Profile.putExtra("username", AccountService.getUsername(context));
                    startActivity(Profile);
                    break;
                case 1:
                    af.getRecords(true, TaskJob.GETLIST, af.list);
                    mf.getRecords(true, TaskJob.GETLIST, mf.list);
                    break;
                case 2:
                    Intent Friends = new Intent(context, net.somethingdreadful.MAL.FriendsActivity.class);
                    startActivity(Friends);
                    break;
                case 3:
                    af.getRecords(true, TaskJob.GETTOPRATED, af.list);
                    mf.getRecords(true, TaskJob.GETTOPRATED, mf.list);
                    break;
                case 4:
                    af.getRecords(true, TaskJob.GETMOSTPOPULAR, af.list);
                    mf.getRecords(true, TaskJob.GETMOSTPOPULAR, mf.list);
                    break;
                case 5:
                    af.getRecords(true, TaskJob.GETJUSTADDED, af.list);
                    mf.getRecords(true, TaskJob.GETJUSTADDED, mf.list);
                    break;
                case 6:
                    af.getRecords(true, TaskJob.GETUPCOMING, af.list);
                    mf.getRecords(true, TaskJob.GETUPCOMING, mf.list);
                    break;
            }

            /*
             * This part is for figuring out which item in the nav drawer is selected and highlighting it with colors.
             */
            if (position != 0 && position != 2) {
                if (mPreviousView != null)
                    mPreviousView.setBackgroundColor(getResources().getColor(R.color.bg_dark)); //normal color
                view.setBackgroundColor(getResources().getColor(android.R.color.black)); // dark color
                mPreviousView = view;
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.bg_dark));
            }

            DrawerLayout.closeDrawer(DrawerList);
        }
    }

    private class DemoDrawerListener implements DrawerLayout.DrawerListener {
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
