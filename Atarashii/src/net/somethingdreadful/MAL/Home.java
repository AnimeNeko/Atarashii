package net.somethingdreadful.MAL;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.freshdesk.mobihelp.Mobihelp;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;
import net.somethingdreadful.MAL.dialog.InputDialogFragment;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Home extends AppCompatActivity implements ChooseDialogFragment.onClickListener, SwipeRefreshLayout.OnRefreshListener, IGF.IGFCallbackListener, View.OnClickListener, UserNetworkTask.UserNetworkTaskListener, ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener, InputDialogFragment.onClickListener {
    private IGF af;
    private IGF mf;
    private Menu menu;
    private BroadcastReceiver networkReceiver;

    private String username;
    private boolean networkAvailable = true;
    private boolean myList = true; //tracks if the user is on 'My List' or not
    private int callbackCounter = 0;
    @Bind(R.id.navigationView)
    NavigationView navigationView;
    @Bind(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initializing activity and application
        Theme.context = getApplicationContext();

        if (AccountService.AccountExists(this)) {
            //The following is state handling code
            if (savedInstanceState != null) {
                myList = savedInstanceState.getBoolean("myList");
                networkAvailable = savedInstanceState.getBoolean("networkAvailable", true);
            }

            //Initializing Theme
            Theme.setTheme(this, R.layout.activity_home, false);

            //Initializing IGF
            Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));

            //Initializing ButterKnife
            ButterKnife.bind(this);

            //setup navigation profile information
            username = AccountService.getUsername();
            new UserNetworkTask(false, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username);

            //Initializing NavigationView
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().findItem(R.id.nav_list).setChecked(true);
            View view = navigationView.getHeaderView(0);
            ((TextView) view.findViewById(R.id.name)).setText(username);
            ((TextView) view.findViewById(R.id.siteName)).setText(getString(AccountService.isMAL() ? R.string.init_hint_myanimelist : R.string.init_hint_anilist));

            //Initializing navigation toggle button
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.actionbar), R.string.drawer_open, R.string.drawer_close) {
            };
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();

            //Applying dark theme
            if (Theme.darkTheme)
                applyDarkTheme();

            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkNetworkAndDisplayCrouton();
                    myListChanged();
                }
            };
        } else {
            Intent firstRunInit = new Intent(this, FirstTimeInit.class);
            startActivity(firstRunInit);
            finish();
        }
        NfcHelper.disableBeam(this);
    }

    /**
     * Apply dark theme if an user enabled it in the settings.
     */
    private void applyDarkTheme() {
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_checked} // checked
        };

        int[] colors = new int[]{
                ContextCompat.getColor(this, R.color.bg_light_card),
                ContextCompat.getColor(this, R.color.primary)
        };

        ColorStateList myList = new ColorStateList(states, colors);
        navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_dark));
        navigationView.setItemTextColor(myList);
        navigationView.setItemIconTintList(myList);
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
            case R.id.sort_title:
                sortRecords(1, item);
                break;
            case R.id.sort_score:
                sortRecords(2, item);
                break;
            case R.id.sort_type:
                sortRecords(3, item);
                break;
            case R.id.sort_status:
                sortRecords(4, item);
                break;
            case R.id.sort_progress:
                sortRecords(5, item);
                break;
            case R.id.menu_details:
                item.setChecked(!item.isChecked());
                if (af != null && mf != null) {
                    af.details();
                    mf.details();
                }
                break;
            case R.id.menu_inverse:
                item.setChecked(!item.isChecked());
                if (af != null && mf != null) {
                    af.inverse();
                    mf.inverse();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortRecords(int sortType, MenuItem item) {
        setChecked(item);
        if (af != null && mf != null) {
            af.sort(sortType);
            mf.sort(sortType);
        }
    }

    private void getRecords(boolean clear, TaskJob task, int list) {
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

    @Override
    public void onPause() {
        super.onPause();
        if (menu != null)
            menu.findItem(R.id.action_search).collapseActionView();
        unregisterReceiver(networkReceiver);
    }

    private void synctask(boolean clear) {
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
        menu.findItem(R.id.sort_title).setChecked(true);
        myListChanged();
        return true;
    }

    private void setChecked(MenuItem item) {
        if (item != null)
            item.setChecked(true);
    }

    private void myListChanged() {
        if (menu != null) {
            if (af != null && mf != null)
                menu.findItem(R.id.menu_details).setChecked(myList && af.getDetails());
            menu.findItem(R.id.menu_listType).setVisible(myList);
            menu.findItem(R.id.menu_sort).setVisible(myList);
            menu.findItem(R.id.menu_inverse).setVisible(myList || (!AccountService.isMAL() && af.taskjob == TaskJob.GETMOSTPOPULAR));
            menu.findItem(R.id.forceSync).setVisible(myList && networkAvailable);
            menu.findItem(R.id.action_search).setVisible(networkAvailable);
        }
    }

    /**
     * Creates the sync notification.
     */
    private void syncNotify() {
        Intent notificationIntent = new Intent(this, Home.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.toast_info_SyncMessage))
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.id.notification_sync, mBuilder.build());
    }

    private void showLogoutDialog() {
        ChooseDialogFragment lcdf = new ChooseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.dialog_label_logout));
        bundle.putString("message", getString(R.string.dialog_message_logout));
        bundle.putString("positive", getString(R.string.dialog_label_logout));
        lcdf.setArguments(bundle);
        lcdf.setCallback(this);
        lcdf.show(getFragmentManager(), "fragment_LogoutConfirmationDialog");
    }

    private void checkNetworkAndDisplayCrouton() {
        if (APIHelper.isNetworkAvailable(this) && !networkAvailable)
            synctask(false);
        networkAvailable = APIHelper.isNetworkAvailable(this);
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
        if (igf.isAnime())
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

        if (callbackCounter >= 2) {
            callbackCounter = 0;

            if (job.equals(TaskJob.FORCESYNC)) {
                NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
            }
        }
    }

    @Override
    public void onItemClick(int id, MALApi.ListType listType, String username) {
        Intent startDetails = new Intent(this, DetailView.class);
        startDetails.putExtra("recordID", id);
        startDetails.putExtra("recordType", listType);
        startDetails.putExtra("username", username);
        startActivity(startDetails);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Image:
                Intent Profile = new Intent(this, ProfileActivity.class);
                Profile.putExtra("username", username);
                startActivity(Profile);
                break;
            case R.id.NDimage:
                InputDialogFragment lcdf = new InputDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id", R.id.NDimage);
                bundle.putString("title", getString(R.string.dialog_title_update_navigation));
                bundle.putString("hint", getString(R.string.dialog_message_update_navigation));
                bundle.putString("message", PrefManager.getNavigationBackground());
                lcdf.setArguments(bundle);
                lcdf.setCallback(this);
                lcdf.show(getFragmentManager(), "fragment_InputDialogFragment");
                break;
        }
    }

    @Override
    public void onUserNetworkTaskFinished(Profile result) {
        ImageView image = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.Image);
        ImageView image2 = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.NDimage);
        try {
            Picasso.with(this)
                    .load(result.getImageUrl())
                    .transform(new RoundedTransformation(result.getUsername()))
                    .into(image);
            if (PrefManager.getNavigationBackground() != null)
                Picasso.with(this)
                        .load(PrefManager.getNavigationBackground())
                        .into(image2);
            image.setOnClickListener(this);
            image2.setOnClickListener(this);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "Home.onUserNetworkTaskFinished(): " + e.getMessage());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (menu != null)
            menu.findItem(R.id.listType_rewatching).setTitle(getString(position == 0 ? R.string.listType_rewatching : R.string.listType_rereading));
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPositiveButtonClicked() {
        AccountService.clearData();
        startActivity(new Intent(this, FirstTimeInit.class));
        System.exit(0);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Checking if the item should be checked & if the list status has been changed
        switch (item.getItemId()) {
            case R.id.nav_profile:
            case R.id.nav_friends:
            case R.id.nav_forum:
            case R.id.nav_schedule:
            case R.id.nav_charts:
            case R.id.nav_settings:
            case R.id.nav_support:
            case R.id.nav_about:
                break;
            default:
                // Set the list tracker to false. It will be updated later in the code.
                myList = false;
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                break;
        }

        // disable swipeRefresh for other lists
        af.setSwipeRefreshEnabled(myList);
        mf.setSwipeRefreshEnabled(myList);

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        //Performing the action
        switch (item.getItemId()) {
            case R.id.nav_list:
                getRecords(true, TaskJob.GETLIST, af.list);
                myList = true;
                break;
            case R.id.nav_profile:
                Intent Profile = new Intent(this, ProfileActivity.class);
                Profile.putExtra("username", username);
                startActivity(Profile);
                break;
            case R.id.nav_friends:
                Intent Friends = new Intent(this, ProfileActivity.class);
                Friends.putExtra("username", username);
                Friends.putExtra("friends", username);
                startActivity(Friends);
                break;
            case R.id.nav_forum:
                if (networkAvailable)
                    startActivity(new Intent(this, ForumActivity.class));
                else
                    Theme.Snackbar(this, R.string.toast_error_noConnectivity);
                break;
            case R.id.nav_schedule:
                startActivity(new Intent(this, ScheduleActivity.class));
                break;
            case R.id.nav_charts:
                startActivity(new Intent(this, ChartActivity.class));
                break;
            case R.id.nav_logout: // Others subgroup
                showLogoutDialog();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.nav_support:
                Mobihelp.showSupport(this);
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        myListChanged();
        return false;
    }

    @Override
    public void onPosInputButtonClicked(String text, int id) {
        Picasso.with(this)
                .load(text)
                .placeholder(R.drawable.atarashii_background)
                .error(R.drawable.atarashii_background)
                .into((ImageView) findViewById(R.id.NDimage));
        PrefManager.setNavigationBackground(text);
        PrefManager.commitChanges();
    }

    @Override
    public void onNegInputButtonClicked(String text, int id) {
        Picasso.with(this)
                .load(R.drawable.atarashii_background)
                .placeholder(R.drawable.atarashii_background)
                .error(R.drawable.atarashii_background)
                .into((ImageView) findViewById(R.id.NDimage));
        PrefManager.setNavigationBackground(null);
        PrefManager.commitChanges();
    }
}
