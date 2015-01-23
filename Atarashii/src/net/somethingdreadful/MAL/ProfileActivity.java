package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.ProfilePagerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;
import net.somethingdreadful.MAL.tasks.UserNetworkTaskFinishedListener;

public class ProfileActivity extends ActionBarActivity implements UserNetworkTaskFinishedListener {
    Context context;
    User record;
    ProfileDetails details;

    boolean forcesync = false;
    private ViewPager viewPager;
    private ProfilePagerAdapter PageAdapter;

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
        PageAdapter = new ProfilePagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(PageAdapter);

        if (getIntent().getExtras().containsKey("user")) {
            toggle(1);
            record = (User) getIntent().getExtras().get("user");
            refresh();
        } else {
            refreshing(true);
            toggle(1);
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
                choosedialog(false);
                break;
            case R.id.Share:
                choosedialog(true);
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

    void choosedialog(final boolean share) { //as the name says
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        if (share) {
            builder.setTitle(R.string.dialog_title_share);
            builder.setMessage(R.string.dialog_message_share);
            sharingIntent.setType("text/plain");
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else {
            builder.setTitle(R.string.dialog_title_view);
            builder.setMessage(R.string.dialog_message_view);
        }

        builder.setPositiveButton(R.string.dialog_label_animelist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (share) {
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_animelist)
                            .replace("$name;", record.getName())
                            .replace("$username;", AccountService.getUsername()));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)));
                } else {
                    Uri mallisturlanime = Uri.parse("http://myanimelist.net/animelist/" + record.getName());
                    startActivity(new Intent(Intent.ACTION_VIEW, mallisturlanime));
                }
            }
        });
        builder.setNeutralButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.dialog_label_mangalist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (share) {
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_mangalist)
                            .replace("$name;", record.getName())
                            .replace("$username;", AccountService.getUsername()));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)));
                } else {
                    Uri mallisturlmanga = Uri.parse("http://myanimelist.net/mangalist/" + record.getName());
                    startActivity(new Intent(Intent.ACTION_VIEW, mallisturlmanga));
                }
            }
        });
        builder.show();
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
    }

    public void refreshing(boolean loading) {
        if (details != null) {
            details.swipeRefresh.setRefreshing(loading);
            details.swipeRefresh.setEnabled(!loading);
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
    }

    public void toggle(int number) {
        if (details != null)
            details.toggle(number);
    }
}
