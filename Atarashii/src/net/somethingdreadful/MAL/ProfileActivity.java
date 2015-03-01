package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import net.somethingdreadful.MAL.adapters.ProfilePagerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.dialog.ShareDialogFragment;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;
import net.somethingdreadful.MAL.tasks.UserNetworkTaskFinishedListener;

public class ProfileActivity extends ActionBarActivity implements UserNetworkTaskFinishedListener {
    Context context;
    User record;
    ProfileDetails details;
    ProfileFriends friends;

    boolean forcesync = false;
    private ViewPager viewPager;
    private ProfilePagerAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();

        setTitle(R.string.title_activity_profile); //set title

        viewPager = (ViewPager) findViewById(R.id.pager);
        pageAdapter = new ProfilePagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(pageAdapter);

        if (getIntent().getExtras().containsKey("user")) {
            record = (User) getIntent().getExtras().get("user");
        } else {
            refreshing(true);
            new UserNetworkTask(context, forcesync, this).execute(getIntent().getStringExtra("username"));
        }

        NfcHelper.disableBeam(this);
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.forceSync:
                if (MALApi.isNetworkAvailable(context)) {
                    refreshing(true);
                    String username;
                    if (record != null)
                        username = record.getName();
                    else
                        username = getIntent().getStringExtra("username");
                    new UserNetworkTask(context, true, this).execute(username);
                } else {
                    Toast.makeText(context, R.string.toast_error_noConnectivity, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_ViewMALPage:
                if (record == null)
                    Toast.makeText(context, R.string.toast_info_hold_on, Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://myanimelist.net/profile/" + record.getName())));
                break;
            case R.id.View:
                showShareDialog(false);
                break;
            case R.id.Share:
                showShareDialog(true);
        }
        return true;
    }

    public void refresh() {
        if (record == null) {
            if (MALApi.isNetworkAvailable(context)) {
                Toast.makeText(context, R.string.toast_error_UserRecord, Toast.LENGTH_SHORT).show();
            } else {
                toggle(2);
            }
        } else {
            setText();
            toggle(0);
        }
    }

    private void showShareDialog(boolean type) {
        ShareDialogFragment share = new ShareDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", record.getName());
        args.putBoolean("share", type);
        share.setArguments(args);
        share.show(getFragmentManager(), "fragment_share");
    }

    @Override
    public void onUserNetworkTaskFinished(User result) {
        record = result;
        refresh();
        refreshing(false);
    }

    public void setText() {
        if (details != null)
            details.refresh();
        if (friends != null)
            friends.getRecords();
    }

    public void refreshing(boolean loading) {
        if (details != null) {
            details.swipeRefresh.setRefreshing(loading);
            details.swipeRefresh.setEnabled(!loading);
        }
        if (friends != null) {
            friends.swipeRefresh.setRefreshing(loading);
            friends.swipeRefresh.setEnabled(!loading);
        }
    }

    public void getRecords() {
        String username;
        if (record != null)
            username = record.getName();
        else
            username = getIntent().getStringExtra("username");
        new UserNetworkTask(context, true, this).execute(username);
    }

    public void setDetails(ProfileDetails details) {
        this.details = details;
        if (record != null)
            details.refresh();
    }

    public void setFriends(ProfileFriends friends) {
        this.friends = friends;
        if (record != null)
            friends.getRecords();
        if (getIntent().getExtras().containsKey("friends"))
            viewPager.setCurrentItem(1);
    }

    public void toggle(int number) {
        if (details != null)
            details.toggle(number);
    }
}
