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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.ProfilePagerAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.dialog.ShareDialogFragment;
import net.somethingdreadful.MAL.profile.ProfileDetailsAL;
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL;
import net.somethingdreadful.MAL.profile.ProfileFriends;
import net.somethingdreadful.MAL.profile.ProfileHistory;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements UserNetworkTask.UserNetworkTaskListener {
    private Context context;
    public Profile record;
    private ProfileFriends friends;
    private ProfileDetailsAL detailsAL;
    private ProfileDetailsMAL detailsMAL;
    private ProfileHistory history;
    private ProfileFriends followers;
    private boolean isLoading = false;

    @Bind(R.id.pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Theme.setTheme(this, R.layout.theme_viewpager, true);
        Theme.setActionBar(this, new ProfilePagerAdapter(getFragmentManager(), this));
        ButterKnife.bind(this);

        context = getApplicationContext();

        setTitle(R.string.title_activity_profile); //set title

        if (bundle != null) {
            record = (Profile) bundle.getSerializable("record");
            setText();
        } else {
            refreshing(true);
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("Profile")
                    .putContentType("Profile")
                    .putContentId("P" + getIntent().getStringExtra("username")));
            getActivity(1);
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
                if (APIHelper.isNetworkAvailable(context)) {
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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getProfileURL() + record.getUsername())));
                break;
            case R.id.View:
                showShareDialog(false);
                break;
            case R.id.Share:
                showShareDialog(true);
        }
        return true;
    }

    private String getProfileURL() {
        return AccountService.isMAL() ? "http://myanimelist.net/profile/" : "http://anilist.co/user/";
    }

    private void refresh() {
        if (record == null) {
            if (APIHelper.isNetworkAvailable(context)) {
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
        if (record == null || record.getUsername() == null) {
            Theme.Snackbar(this, R.string.toast_info_hold_on);
        } else {
            ShareDialogFragment share = new ShareDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", record.getUsername());
            args.putBoolean("share", type);
            share.setArguments(args);
            share.show(getFragmentManager(), "fragment_share");
        }
    }

    @Override
    public void onUserNetworkTaskFinished(Profile result) {
        record = result;
        refresh();
        refreshing(false);
        isLoading = false;
    }

    private void setText() {
        if (detailsMAL != null)
            detailsMAL.refresh();
        if (detailsAL != null)
            detailsAL.refresh();
        if (friends != null)
            friends.getRecords();
        if (followers != null)
            followers.getRecords();
        if (history != null && record.getActivity() != null && record.getActivity().size() > 0)
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
        if (followers != null) {
            followers.swipeRefresh.setRefreshing(loading);
            followers.swipeRefresh.setEnabled(!loading);
        }
    }

    public void getRecords() {
        if (!isLoading) {
            isLoading = true;
            String username = record != null ? record.getUsername() : getIntent().getStringExtra("username");
            new UserNetworkTask(true, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username);
        }
    }

    public void getActivity(int page) {
        if (!isLoading) {
            isLoading = true;
            String username = record != null ? record.getUsername() : getIntent().getStringExtra("username");
            new UserNetworkTask(false, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, String.valueOf(page));
        }
    }

    public void setDetails(ProfileDetailsMAL details) {
        this.detailsMAL = details;
        if (record != null)
            details.refresh();
    }

    public void setFriends(ProfileFriends friends) {
        this.friends = friends;
        if (record != null && friends.getListarray() == null)
            friends.getRecords();
        if (getIntent().getExtras().containsKey("friends"))
            viewPager.setCurrentItem(1);
    }

    private void toggle(int number) {
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

    public void setFollowers(ProfileFriends followers) {
        this.followers = followers;
        if (record != null && followers.getListarray() == null)
            followers.getRecords();
        if (getIntent().getExtras().containsKey("friends"))
            viewPager.setCurrentItem(1);
    }
}
