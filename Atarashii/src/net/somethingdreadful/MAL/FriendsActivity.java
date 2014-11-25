package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.FriendsGridviewAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTaskFinishedListener;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FriendsActivity extends ActionBarActivity implements FriendsNetworkTaskFinishedListener, SwipeRefreshLayout.OnRefreshListener {

    Context context;
    ArrayList<User> listarray = new ArrayList<User>();
    FriendsGridviewAdapter<User> listadapter;
    GridView Gridview;
    SwipeRefreshLayout swipeRefresh;
    boolean forcesync = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_friends);
        setTitle(R.string.title_activity_friends); //set title

        Gridview = (GridView) findViewById(R.id.listview);
        listadapter = new FriendsGridviewAdapter<User>(context, listarray);

        new FriendsNetworkTask(context, forcesync, this).execute(AccountService.getUsername(context));
        refresh(false);

        Gridview.setOnItemClickListener(new OnItemClickListener() { //start the profile with your friend
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
                if (listarray.get(position).getProfile().getDetails().getAccessRank() == null) {
                    profile.putExtra("username", listarray.get(position).getName());
                } else
                    profile.putExtra("user", listarray.get(position));
                startActivity(profile);
            }
        });

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        NfcHelper.disableBeam(this);
    }

    public void refresh(Boolean crouton) {
        if (crouton) {
            Crouton.makeText(this, R.string.crouton_info_SyncDone, Style.CONFIRM).show();
        }
        Gridview.setAdapter(listadapter);
        try {
            listadapter.supportAddAll(listarray);
        } catch (Exception e) {
            if (MALApi.isNetworkAvailable(context)) {
                Crouton.makeText(this, R.string.crouton_error_noFriends, Style.ALERT).show();
            } else {
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
                sync(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFriendsNetworkTaskFinished(ArrayList<User> result) {
        if (result != null) {
            swipeRefresh.setEnabled(true);
            listarray = result;
            if (listarray.size() == 0)
                Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
            refresh(forcesync); // show crouton only if sync was forced
        } else {
            Crouton.makeText(this, R.string.crouton_error_Friends, Style.ALERT).show();
        }
        swipeRefresh.setRefreshing(false);
    }

    public void sync(Boolean swipe) {
        swipeRefresh.setEnabled(false);
        if (MALApi.isNetworkAvailable(context)) {
            if (!swipe)
                Crouton.makeText(this, R.string.crouton_info_SyncMessage, Style.INFO).show();
            forcesync = true;
            new FriendsNetworkTask(context, true, this).execute(AccountService.getUsername(context));
        } else {
            swipeRefresh.setRefreshing(false);
            Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
        }
    }

    @Override
    public void onRefresh() {
        sync(true);
    }
}
