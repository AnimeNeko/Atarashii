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
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;

public class ProfileFriends extends Fragment implements FriendsNetworkTask.FriendsNetworkTaskListener, SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {
    private GridView Gridview;
    private ProfileActivity activity;
    private FriendsGridviewAdapter<Profile> listadapter;
    @Getter
    private ArrayList<Profile> listarray;

    @Bind(R.id.network_Card)
    Card networkCard;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.swiperefresh)
    public SwipeRefreshLayout swipeRefresh;
    private boolean forcesync = false;
    private int id;

    public Fragment setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.friends, container, false);
        Theme.setBackground(activity, view, Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        ButterKnife.bind(this, view);

        Gridview = (GridView) view.findViewById(R.id.listview);
        Gridview.setOnItemClickListener(this);
        listadapter = new FriendsGridviewAdapter<>(activity, listarray);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
        toggle(1);

        if (state != null) {
            id = state.getInt("id");
            listarray = (ArrayList<Profile>) state.getSerializable("listarray");
            refresh();
        }
        if (id == 0)
            activity.setFriends(this);
        else
            activity.setFollowers(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("id", id);
        state.putSerializable("listarray", listarray);
        super.onSaveInstanceState(state);
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

    private void refresh() {
        Gridview.setAdapter(listadapter);
        try {
            listadapter.supportAddAll(listarray);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Crashlytics.log(Log.ERROR, "Atarashii", "FriendsActivity.refresh(): " + e.getMessage());
        }
        listadapter.notifyDataSetChanged();
        toggle(0);
    }

    @Override
    public void onFriendsNetworkTaskFinished(ArrayList<Profile> result) {
        if (result != null) {
            listarray = result;
            if (result.size() == 0 && !APIHelper.isNetworkAvailable(activity))
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
        new FriendsNetworkTask(activity, forcesync, this, activity, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity.record.getUsername());
    }

    @Override
    public void onRefresh() {
        forcesync = true;
        getRecords();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (APIHelper.isNetworkAvailable(activity)) {
            Intent profile = new Intent(activity, net.somethingdreadful.MAL.ProfileActivity.class);
            profile.putExtra("username", listarray.get(position).getUsername());
            startActivity(profile);
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
        }
    }
}
