package net.somethingdreadful.MAL;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.TaskJob;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IGF.IGFCallbackListener, NavigationView.OnNavigationItemSelectedListener {
    private IGF af;
    private IGF mf;
    private MenuItem drawerItem;

    @Bind(R.id.navigationView)
    NavigationView navigationView;
    @Bind(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.activity_charts, false);
        Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));
        ButterKnife.bind(this);

        //Initializing NavigationView
        navigationView.setNavigationItemSelectedListener(this);
        Theme.setNavDrawer(navigationView, this, null);

        //Initializing navigation toggle button
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.actionbar), R.string.drawer_open, R.string.drawer_close) {
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerLayout.openDrawer(ViewCompat.getLayoutDirection(drawerLayout) == ViewCompat.LAYOUT_DIRECTION_RTL ? Gravity.RIGHT : Gravity.LEFT);

        NfcHelper.disableBeam(this);
    }

    private void getRecords(boolean clear, TaskJob task, int list) {
        if (af != null && mf != null) {
            af.getRecords(clear, task, list);
            mf.getRecords(clear, task, list);
        }
    }

    @Override
    public void onRefresh() {
        if (APIHelper.isNetworkAvailable(this))
            getRecords(false, TaskJob.FORCESYNC, af.list);
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
    }

    @Override
    public void onRecordsLoadingFinished(TaskJob job) {

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
    public boolean onNavigationItemSelected(MenuItem item) {
        if (drawerItem != null)
            drawerItem.setChecked(false);
        item.setChecked(true);
        drawerItem = item;
        setTitle(item.getTitle());

        // disable swipeRefresh for other lists
        af.setSwipeRefreshEnabled(false);
        mf.setSwipeRefreshEnabled(false);

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        //Performing the action
        switch (item.getItemId()) {
            case R.id.nav_rated:
                getRecords(true, TaskJob.GETTOPRATED, af.list);
                break;
            case R.id.nav_rated_season:
                getRecords(true, TaskJob.GETTOPRATEDS, af.list);
                break;
            case R.id.nav_rated_year:
                getRecords(true, TaskJob.GETTOPRATEDY, af.list);
                break;
            case R.id.nav_popular:
                getRecords(true, TaskJob.GETMOSTPOPULAR, af.list);
                break;
            case R.id.nav_popular_season:
                getRecords(true, TaskJob.GETMOSTPOPULARS, af.list);
                break;
            case R.id.nav_popular_year:
                getRecords(true, TaskJob.GETMOSTPOPULARY, af.list);
                break;
            case R.id.nav_added:
                getRecords(true, TaskJob.GETJUSTADDED, af.list);
                break;
            case R.id.nav_upcoming:
                getRecords(true, TaskJob.GETUPCOMING, af.list);
                break;
            case R.id.nav_return:
                finish();
        }
        return false;
    }
}
