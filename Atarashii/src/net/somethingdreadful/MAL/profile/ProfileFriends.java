package net.somethingdreadful.MAL.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.adapters.FriendsGridviewAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.UserProfile.User;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTaskFinishedListener;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileFriends extends Fragment implements FriendsNetworkTaskFinishedListener, SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {
    GridView Gridview;
    private ProfileActivity activity;
    FriendsGridviewAdapter<User> listadapter;
    ArrayList<User> listarray = new ArrayList<>();

    @InjectView(R.id.network_Card) Card networkCard;
    @InjectView(R.id.progressBar) ProgressBar progressBar;
    @InjectView(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;

    boolean forcesync = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.friends, container, false);
        Theme.setBackground(activity, view, Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        ButterKnife.inject(this, view);

        Gridview = (GridView) view.findViewById(R.id.listview);
        Gridview.setOnItemClickListener(this);
        listadapter = new FriendsGridviewAdapter<>(activity, listarray);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        activity.setFriends(this);
        toggle(1);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
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

    @Override
    public void onFriendsNetworkTaskFinished(ArrayList<User> result) {
        if (result != null) {
            listarray = result;
            if (result.size() == 0 && !MALApi.isNetworkAvailable(activity))
                toggle(2);
            else
                refresh(); // show toast only if sync was forced
        } else {
            Theme.Snackbar(activity, R.string.toast_error_Friends);
        }
        activity.refreshing(false);
    }

    public void getRecords() {
        activity.refreshing(true);
        new FriendsNetworkTask(activity, forcesync, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity.record.getName());
    }

    @Override
    public void onRefresh() {
        forcesync = true;
        getRecords();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent profile = new Intent(activity, net.somethingdreadful.MAL.ProfileActivity.class);
        if (listarray.get(position).getProfile().getDetails().getAccessRank() == null)
            profile.putExtra("username", listarray.get(position).getName());
        else
            profile.putExtra("user", listarray.get(position));
        startActivity(profile);
    }
}
