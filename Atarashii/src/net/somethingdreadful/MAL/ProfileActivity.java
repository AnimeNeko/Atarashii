package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.ProfilePagerAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.ShareDialogFragment;
import net.somethingdreadful.MAL.profile.ProfileDetailsAL;
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL;
import net.somethingdreadful.MAL.profile.ProfileFriends;
import net.somethingdreadful.MAL.profile.ProfileHistory;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements UserNetworkTask.UserNetworkTaskListener, IGF.IGFCallbackListener, ViewPager.OnPageChangeListener {
    private Context context;
    public Profile record;
    private ProfileFriends friends;
    private ProfileDetailsAL detailsAL;
    private ProfileDetailsMAL detailsMAL;
    private ProfileHistory history;
    private ProfileFriends followers;
    private IGF animeList;
    private IGF mangaList;
    Menu menu;
    ProfilePagerAdapter adapter;
    private boolean isLoading = false;

    @Bind(R.id.pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //TODO v2.3: Enable scroll tabs support for Atarashii
        // Theme.setTheme(this, R.layout.theme_viewpager_scrolltabs, true);
        Theme.setTheme(this, R.layout.theme_viewpager, true);
        adapter = (ProfilePagerAdapter) Theme.setActionBar(this, new ProfilePagerAdapter(getFragmentManager(), this));
        ButterKnife.bind(this);

        context = getApplicationContext();
        viewPager.addOnPageChangeListener(this);

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
        this.menu = menu;
        if (menu.findItem(R.id.sort_title) != null)
            menu.findItem(R.id.sort_title).setChecked(true);
        if (menu.findItem(R.id.menu_sort) != null)
            menu.findItem(R.id.menu_sort).setVisible(false);
        if (menu.findItem(R.id.listType_all) != null)
            menu.findItem(R.id.listType_all).setChecked(true);
        if (menu.findItem(R.id.menu_listType) != null)
            menu.findItem(R.id.menu_listType).setVisible(false);
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
                break;
            case R.id.listType_all:
                filterRecords(1, item);
                break;
            case R.id.listType_inprogress:
                filterRecords(2, item);
                break;
            case R.id.listType_completed:
                filterRecords(3, item);
                break;
            case R.id.listType_onhold:
                filterRecords(4, item);
                break;
            case R.id.listType_dropped:
                filterRecords(5, item);
                break;
            case R.id.listType_planned:
                filterRecords(6, item);
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
                if (!animeList.isLoading() && !mangaList.isLoading()) {
                    item.setChecked(!item.isChecked());
                    if (animeList != null && mangaList != null) {
                        animeList.details();
                        mangaList.details();
                    }
                } else {
                    Theme.Snackbar(this, R.string.toast_info_hold_on);
                }
                break;
            case R.id.menu_inverse:
                if (!animeList.isLoading() && !mangaList.isLoading()) {
                    item.setChecked(!item.isChecked());
                    if (animeList != null && mangaList != null) {
                        animeList.inverse();
                        mangaList.inverse();
                    }
                } else {
                    Theme.Snackbar(this, R.string.toast_info_hold_on);
                }
                break;
        }
        return true;
    }

    private void sortRecords(int sortType, MenuItem item) {
        if (item != null)
            item.setChecked(true);
        if (animeList != null && mangaList != null) {
            animeList.sort(sortType);
            mangaList.sort(sortType);
        }
    }

    private void filterRecords(int filterType, MenuItem item) {
        if (item != null)
            item.setChecked(true);
        if (animeList != null && mangaList != null) {
            animeList.filter(filterType);
            mangaList.filter(filterType);
        }
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
        if (animeList != null && animeList.getUsername() == null) {
            animeList.setUsername(record.getUsername());
            animeList.getRecords(true, TaskJob.GETFRIENDLIST, PrefManager.getDefaultList());
        }
        if (mangaList != null && mangaList.getUsername() == null) {
            mangaList.setUsername(record.getUsername());
            mangaList.getRecords(true, TaskJob.GETFRIENDLIST, PrefManager.getDefaultList());
        }
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

    @Override
    public void onIGFReady(IGF igf) {
        try {
            if (igf.isAnime()) {
                animeList = igf;
                animeList.Gridview.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark));
                if (record != null) {
                    animeList.setUsername(record.getUsername());
                    animeList.getRecords(true, TaskJob.GETFRIENDLIST, PrefManager.getDefaultList());
                }
            } else {
                mangaList = igf;
                mangaList.Gridview.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark));
                if (record != null) {
                    mangaList.setUsername(record.getUsername());
                    mangaList.getRecords(true, TaskJob.GETFRIENDLIST, PrefManager.getDefaultList());
                }
            }
        } catch (Exception e) {
            Crashlytics.log(Log.INFO, "Atarashii", "ProfileActivity.onIGFReady()");
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onRecordsLoadingFinished(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {

    }

    @Override
    public void onItemClick(int id, MALApi.ListType listType, String username) {
        Intent startDetails = new Intent(this, DetailView.class);
        startDetails.putExtra("recordID", id);
        startDetails.putExtra("recordType", listType);
        startDetails.putExtra("username", AccountService.getUsername()); // do not use the provided username which is the friends username
        startActivity(startDetails);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (menu != null) {
            boolean isIGF = adapter.getItem(position) instanceof IGF;
            menu.findItem(R.id.menu_listType).setVisible(isIGF);
            menu.findItem(R.id.menu_sort).setVisible(isIGF);
            menu.findItem(R.id.forceSync).setVisible(!isIGF);
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
