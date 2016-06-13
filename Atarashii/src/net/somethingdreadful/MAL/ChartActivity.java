package net.somethingdreadful.MAL;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.TaskJob;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IGF.IGFCallbackListener, NavigationView.OnNavigationItemSelectedListener {
    private IGF af;
    private IGF mf;
    private MenuItem drawerItem;

    private String username;
    private boolean networkAvailable = true;
    @Bind(R.id.navigationView)
    NavigationView navigationView;
    @Bind(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.setTheme(this, R.layout.activity_charts, false);
        Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));
        ButterKnife.bind(this);

        //setup navigation profile information
        username = AccountService.getUsername();

        //Initializing NavigationView
        navigationView.setNavigationItemSelectedListener(this);
        navDrawer(navigationView.getHeaderView(0));

        //Initializing navigation toggle button
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.actionbar), R.string.drawer_open, R.string.drawer_close) {
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerLayout.openDrawer(ViewCompat.getLayoutDirection(drawerLayout) == ViewCompat.LAYOUT_DIRECTION_RTL ? Gravity.RIGHT : Gravity.LEFT);
        //Applying dark theme
        if (Theme.darkTheme)
            applyDarkTheme();
        NfcHelper.disableBeam(this);
    }

    /**
     * Init the navigationDrawer
     *
     * @param view The navigationDrawer header
     */
    private void navDrawer(View view) {
        ((TextView) view.findViewById(R.id.name)).setText(username);
        ((TextView) view.findViewById(R.id.siteName)).setText(getString(AccountService.isMAL() ? R.string.init_hint_myanimelist : R.string.init_hint_anilist));
        ImageView image2 = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.NDimage);
        ImageView image = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.Image);
        if (PrefManager.getNavigationBackground() != null)
            Picasso.with(this)
                    .load(PrefManager.getNavigationBackground())
                    .into(image2);
        image.setVisibility(View.GONE);
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

    private void getRecords(boolean clear, TaskJob task, int list) {
        if (af != null && mf != null) {
            af.getRecords(clear, task, list);
            mf.getRecords(clear, task, list);
        }
    }

    private void synctask(boolean clear) {
        getRecords(clear, TaskJob.FORCESYNC, af.list);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("networkAvailable", networkAvailable);
        super.onSaveInstanceState(state);
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
    }

    @Override
    public void onRecordsLoadingFinished(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {

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
