package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.api.BaseMALApi;
import net.somethingdreadful.MAL.sql.MALSqlHelper;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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
            mSectionsPagerAdapter = new HomeSectionsPagerAdapter(
                    getSupportFragmentManager());

            mManager = new MALManager(context);

            if (!instanceExists) {

            }

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

        } else //If the app hasn't been configured, take us to the first run screen to sign in
        {
            Intent firstRunInit = new Intent(this, FirstTimeInit.class);
            firstRunInit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(firstRunInit);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_home, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public BaseMALApi.ListType getCurrentListType() {
        String listName = getSupportActionBar().getSelectedTab().getText().toString();
        return BaseMALApi.getListTypeByString(listName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                    af.getRecords(0, "anime", false);
                    mf.getRecords(0, "manga", false);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_inprogress:
                if (af != null && mf != null) {
                    af.getRecords(1, "anime", false);
                    mf.getRecords(1, "manga", false);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_completed:
                if (af != null && mf != null) {
                    af.getRecords(2, "anime", false);
                    mf.getRecords(2, "manga", false);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_onhold:
                if (af != null && mf != null) {
                    af.getRecords(3, "anime", false);
                    mf.getRecords(3, "manga", false);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_dropped:
                if (af != null && mf != null) {
                    af.getRecords(4, "anime", false);
                    mf.getRecords(4, "manga", false);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.listType_planned:
                if (af != null && mf != null) {
                    af.getRecords(5, "anime", false);
                    mf.getRecords(5, "manga", false);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.forceSync:
                if (af != null && mf != null) {
                    af.getRecords(af.currentList, "anime", true);
                    mf.getRecords(af.currentList, "manga", true);
                    syncNotify();
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (instanceExists) {
            af.getRecords(af.currentList, "anime", false);
            mf.getRecords(af.currentList, "manga", false);
        }

        checkNetworkAndDisplayCrouton();

        registerReceiver(networkReceiver,  new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
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
        af = (ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
        mf = (ItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 1);

        //Logic to check if we have just signed in. If yes, automatically do a sync
        if (getIntent().getBooleanExtra("net.somethingdreadful.MAL.firstSync", false)) {
            af.getRecords(af.currentList, "anime", true);
            mf.getRecords(mf.currentList, "manga", true);
            getIntent().removeExtra("net.somethingdreadful.MAL.firstSync");
            syncNotify();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("instanceExists", true);
        state.putBoolean("networkAvailable", networkAvailable);

        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
            menu.findItem(R.id.forceSync).setEnabled(true);
        }
        else {
            menu.findItem(R.id.forceSync).setEnabled(false);
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
        new ImageDownloader(context).wipeCache();
        startActivity(new Intent(this, Home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void syncNotify() {
        Toast.makeText(context, R.string.toast_SyncMessage, Toast.LENGTH_LONG).show();

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

    public void checkNetworkAndDisplayCrouton() {
        if (!isNetworkAvailable() && networkAvailable == true) {
            Crouton.makeText(this, R.string.crouton_noConnectivityOnRun, Style.ALERT).show();
        }

        if (isNetworkAvailable() && networkAvailable == false) {
            Crouton.makeText(this, R.string.crouton_connectionRestored, Style.INFO).show();
            //TODO: Sync here, but first sync any records marked DIRTY
        }

        if (!isNetworkAvailable()) {
            networkAvailable = false;
        }
        else {
            networkAvailable = true;
        }

        supportInvalidateOptionsMenu();
    }
}
