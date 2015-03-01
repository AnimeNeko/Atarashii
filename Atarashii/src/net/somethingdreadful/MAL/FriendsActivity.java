package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.FriendsGridviewAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTaskFinishedListener;

import java.util.ArrayList;

public class FriendsActivity extends ActionBarActivity implements FriendsNetworkTaskFinishedListener, SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {

    Context context;
    ArrayList<User> listarray = new ArrayList<User>();
    FriendsGridviewAdapter<User> listadapter;
    GridView Gridview;
    SwipeRefreshLayout swipeRefresh;
    ProgressBar progressBar;
    Card networkCard;
    boolean forcesync = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_friends);
        setTitle(AccountService.isMAL() ? R.string.title_activity_friends : R.string.nav_item_my_followers); //set title

        Gridview = (GridView) findViewById(R.id.listview);
        Gridview.setOnItemClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        networkCard = (Card) findViewById(R.id.network_Card);
        listadapter = new FriendsGridviewAdapter<User>(context, listarray);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        toggle(1);
        sync();

        NfcHelper.disableBeam(this);
    }

    private void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void refresh() {
        Gridview.setAdapter(listadapter);
        try {
            listadapter.supportAddAll(listarray);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Crashlytics.log(Log.ERROR, "MALX", "FriendsActivity.refresh(): " + e.getMessage());
        }
        listadapter.notifyDataSetChanged();
        toggle(0);
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
                forcesync = true;
                sync();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFriendsNetworkTaskFinished(ArrayList<User> result) {
        if (result != null) {
            listarray = result;
            if (result.size() == 0 && !MALApi.isNetworkAvailable(context)) {
                toggle(2);
            } else {
                refresh(); // show toast only if sync was forced
            }
        } else {
            Toast.makeText(context, R.string.toast_error_Friends, Toast.LENGTH_SHORT).show();
        }
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
    }

    public void sync() {
        swipeRefresh.setEnabled(false);
        swipeRefresh.setRefreshing(true);
        new FriendsNetworkTask(context, forcesync, this).execute(AccountService.getUsername());
    }

    @Override
    public void onRefresh() {
        forcesync = true;
        sync();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
        if (listarray.get(position).getProfile().getDetails().getAccessRank() == null) {
            profile.putExtra("username", listarray.get(position).getName());
        } else
            profile.putExtra("user", listarray.get(position));
        startActivity(profile);
    }
}
