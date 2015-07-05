package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.ProfilePagerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.UserProfile.User;
import net.somethingdreadful.MAL.dialog.ShareDialogFragment;
import net.somethingdreadful.MAL.profile.ProfileDetailsAL;
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL;
import net.somethingdreadful.MAL.profile.ProfileFriends;
import net.somethingdreadful.MAL.profile.ProfileHistory;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;
import net.somethingdreadful.MAL.tasks.UserNetworkTaskFinishedListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileActivity extends AppCompatActivity implements UserNetworkTaskFinishedListener {
    Context context;
    public User record;
    ProfileFriends friends;
    ProfileDetailsAL detailsAL;
    ProfileDetailsMAL detailsMAL;
    ProfileHistory history;
    boolean isLoading = false;

    @InjectView(R.id.pager) ViewPager viewPager;

    boolean forcesync = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Theme.setTheme(this, R.layout.theme_viewpager, true);
        Theme.setActionBar(this, new ProfilePagerAdapter(getFragmentManager(), this));
        ButterKnife.inject(this);

        context = getApplicationContext();

        setTitle(R.string.title_activity_profile); //set title

        if (getIntent().getExtras().containsKey("user")) {
            record = (User) getIntent().getExtras().get("user");
        }

        if (bundle != null) {
            record = (User) bundle.getSerializable("record");
            setText();
        } else {
            if (getIntent().getExtras().containsKey("user")) {
                record = (User) getIntent().getExtras().get("user");
                setText();
            } else {
                refreshing(true);
                new UserNetworkTask(context, forcesync, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getIntent().getStringExtra("username"));
            }
        }

        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile_view, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("record", record);
        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.forceSync:
                if (MALApi.isNetworkAvailable(context)) {
                    refreshing(true);
                    getRecords();
                } else {
                    Theme.Snackbar(this, R.string.toast_error_noConnectivity);
                }
                break;
            case R.id.action_ViewMALPage:
                if (record == null)
                    Theme.Snackbar(this, R.string.toast_info_hold_on);
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getProfileURL() + record.getName())));
                break;
            case R.id.View:
                showShareDialog(false);
                break;
            case R.id.Share:
                showShareDialog(true);
        }
        return true;
    }

    public String getProfileURL() {
        return AccountService.isMAL() ? "http://myanimelist.net/profile/" : "http://anilist.co/user/";
    }

    public void refresh() {
        if (record == null) {
            if (MALApi.isNetworkAvailable(context)) {
                Theme.Snackbar(this, R.string.toast_error_UserRecord);
            } else {
                toggle(2);
            }
        } else {
            setText();
            toggle(0);
        }
    }

    private void showShareDialog(boolean type) {
        if (record == null || record.getName() == null) {
            Theme.Snackbar(this, R.string.toast_info_hold_on);
        } else {
            ShareDialogFragment share = new ShareDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", record.getName());
            args.putBoolean("share", type);
            share.setArguments(args);
            share.show(getFragmentManager(), "fragment_share");
        }
    }

    @Override
    public void onUserNetworkTaskFinished(User result) {
        record = result;
        refresh();
        refreshing(false);
        isLoading = false;
    }

    public void setText() {
        if (detailsMAL != null)
            detailsMAL.refresh();
        if (detailsAL != null)
            detailsAL.refresh();
        if (friends != null)
            friends.getRecords();
        if (history != null)
            history.refresh();
    }

    public void refreshing(boolean loading) {
        if (detailsMAL != null) {
            detailsMAL.swipeRefresh.setRefreshing(loading);
            detailsMAL.swipeRefresh.setEnabled(!loading);
        }
        if (detailsAL != null) {
            detailsAL.swipeRefresh.setRefreshing(loading);
            detailsAL.swipeRefresh.setEnabled(!loading);
        }
        if (friends != null) {
            friends.swipeRefresh.setRefreshing(loading);
            friends.swipeRefresh.setEnabled(!loading);
        }
        if (history != null) {
            history.swipeRefresh.setRefreshing(loading);
            history.swipeRefresh.setEnabled(!loading);
        }
    }

    public void getRecords() {
        if (!isLoading) {
            isLoading = true;
            String username;
            if (record != null)
                username = record.getName();
            else
                username = getIntent().getStringExtra("username");
            new UserNetworkTask(context, true, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, username);
        }
    }

    public void setDetails(ProfileDetailsMAL details) {
        this.detailsMAL = details;
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
        if (detailsMAL != null)
            detailsMAL.toggle(number);
        if (detailsAL != null)
            detailsAL.toggle(number);
    }

    public void setDetails(ProfileDetailsAL details) {
        this.detailsAL = details;
        if (record != null)
            details.refresh();
    }

    public void setHistory(ProfileHistory history) {
        this.history = history;
    }
}
