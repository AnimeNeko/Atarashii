package net.somethingdreadful.MAL.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.RoundedTransformation;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public ProfileActivity activity;
    private final ArrayList<History> record = new ArrayList<>();
    private activityAdapter ra;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private int page = 1;
    private boolean loading = false;
    private boolean hasmorepages = true;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_profile_history, container, false);
        Theme.setBackground(activity, view, Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        ButterKnife.bind(this, view);

        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager SGLM = new StaggeredGridLayoutManager(getMaxColumns(), 1);

        recyclerView.setLayoutManager(SGLM);
        recyclerView.addOnScrollListener(new OnScrollListener(SGLM));
        ra = new activityAdapter(activity);
        recyclerView.setAdapter(ra);

        activity.setHistory(this);
        if (activity.record != null)
            refresh();

        NfcHelper.disableBeam(activity);
        return view;
    }

    /**
     * Get the max amount of columns.
     *
     * @return int The amount of max columns
     */
    private int getMaxColumns() {
        int screen;
        if (Theme.isPortrait())
            screen = activity.getResources().getConfiguration().screenHeightDp;
        else
            screen = activity.getResources().getConfiguration().screenWidthDp;
        screen = screen / 485;
        return screen < 1 ? 1 : screen;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
    }

    /**
     * Refresh the UI for changes.
     *
     * @param result The new record
     */
    private void apply(Profile result) {
        try {
            if (result != null && result.getActivity() != null) {
                record.addAll(result.getActivity());
                ra.notifyDataSetChanged();
                loading = false;
            } else {
                hasmorepages = false;
                Theme.Snackbar(activity, R.string.toast_error_Records);
            }
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "ProfileHistory.apply(): " + e.getMessage());
            AppLog.logException(e);
            e.printStackTrace();
        }
    }

    public void refresh() {
        apply(activity.record);
    }

    @Override
    public void onRefresh() {
        activity.getActivity(1);
    }

    /**
     * The custom adapter for reviews.
     */
    public class activityAdapter extends RecyclerView.Adapter<historyAdapterHolder> {
        private final Context context;

        public activityAdapter(Context context) {
            this.context = context;
        }

        @Override
        public historyAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_history_gridview, null);
            return new historyAdapterHolder(layoutView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(historyAdapterHolder holder, int position) {
            try {
                // Get the preview part for the reviews
                History history = record.get(position);
                String image = "";

                switch (history.getActivityType()) {
                    case "message":
                    case "text":
                        holder.value.setText(history.getValue());
                        holder.title.setText(history.getUsers().get(0).getUsername());
                        holder.time.setText(history.getCreatedAt());
                        image = history.getUsers().get(0).getImageUrl();
                        break;
                    case "list":
                        if (history.getSeries() != null) {
                            holder.value.setText(history.getUsers().get(0).getUsername() + " " + history.getStatus() + " " + history.getValue());
                            holder.title.setText(history.getSeries().getTitle());
                            image = history.getSeries().getImageUrl();
                            holder.time.setText(history.getCreatedAt());
                            holder.type.setText(history.getType());
                        }
                        break;
                }

                Picasso.with(context)
                        .load(image)
                        .transform(new RoundedTransformation(image + "History")).fit()
                        .into(holder.imageView);

            } catch (Exception e) {
                AppLog.logTaskCrash("ProfileHistory", e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return record.size();
        }
    }

    /**
     * The infinite scrolling mechanism.
     */
    public class OnScrollListener extends RecyclerView.OnScrollListener {
        int firstVisibleItem, visibleItemCount, totalItemCount;
        private final StaggeredGridLayoutManager layoutManager;

        public OnScrollListener(StaggeredGridLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = layoutManager.getItemCount();
            int[] firstVisibleItems = null;
            firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0)
                firstVisibleItem = firstVisibleItems[0];

            // don't do anything if there is nothing in the list
            if (firstVisibleItem == 0 && visibleItemCount == 0 && totalItemCount == 0)
                return;
            if (totalItemCount - firstVisibleItem <= (visibleItemCount * 2) && !loading && hasmorepages && !AccountService.isMAL()) {
                loading = true;
                page++;
                activity.getActivity(page);
            }
        }
    }

    /**
     * The viewholder for performance.
     */
    public class historyAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView title;
        public final TextView time;
        public final TextView value;
        public final TextView type;
        public final ImageView imageView;

        public historyAdapterHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            time = (TextView) view.findViewById(R.id.time);
            value = (TextView) view.findViewById(R.id.value);
            type = (TextView) view.findViewById(R.id.type);
            imageView = (ImageView) view.findViewById(R.id.profileImg);

            itemView.setOnClickListener(this);

            if (Theme.darkTheme) {
                title.setTextColor(ContextCompat.getColor(activity, R.color.white));
                time.setTextColor(ContextCompat.getColor(activity, R.color.text_dark));
                value.setTextColor(ContextCompat.getColor(activity, R.color.text_dark));
            }
        }

        @Override
        public void onClick(View view) {
            History history = record.get(getAdapterPosition());

            switch (history.getActivityType()) {
                case "message":
                case "text": // Show the profile
                    if (APIHelper.isNetworkAvailable(activity)) {
                        Intent profile = new Intent(activity, net.somethingdreadful.MAL.ProfileActivity.class);
                        profile.putExtra("username", history.getUsers().get(0).getUsername());
                        startActivity(profile);
                    } else {
                        Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                    }
                    break;
                case "list": // Show the profile
                    if (APIHelper.isNetworkAvailable(activity)) {
                        Intent startDetails = new Intent(activity, DetailView.class);
                        startDetails.putExtra("recordID", history.getSeries().getId());
                        startDetails.putExtra("recordType", history.isAnime() ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
                        startActivity(startDetails);
                    } else {
                        Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                    }
                    break;
            }
        }
    }
}
