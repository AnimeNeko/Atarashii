package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.NetworkTaskCallbackListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class IGF extends Fragment implements OnScrollListener, OnItemClickListener, NetworkTaskCallbackListener, RecordStatusUpdatedListener {

    public ListType listType = ListType.ANIME; // just to have it proper initialized
    Context context;
    TaskJob taskjob;

    GridView Gridview;
    ViewFlipper viewflipper;
    SwipeRefreshLayout swipeRefresh;
    Activity activity;
    ArrayList<GenericRecord> gl = new ArrayList<GenericRecord>();
    ListViewAdapter<GenericRecord> ga;
    IGFCallbackListener callback;

    NetworkTask networkTask;

    RecordStatusUpdatedReceiver recordStatusReceiver;

    int page = 1;
    int list = -1;
    int resource;
    int height = 0;
    boolean useSecondaryAmounts;
    boolean loading = true;
    boolean clearAfterLoading = false;
    boolean hasmorepages = false;
    /* setSwipeRefreshEnabled() may be called before swipeRefresh exists (before onCreateView() is
     * called), so save it and apply it in onCreateView() */
    boolean swipeRefreshEnabled = true;

    String query;

    // use setter to change this!
    private String username;
    private boolean ownList = false; // not set directly, is set by setUsername()

    /**
     * Set the watched/read count & status on the covers.
     */
    public static void setStatus(String myStatus, TextView textview, TextView progressCount, ImageView actionButton) {
        actionButton.setVisibility(View.GONE);
        progressCount.setVisibility(View.GONE);
        if (myStatus == null) {
            textview.setText("");
        } else if (myStatus.equals("watching")) {
            textview.setText(R.string.cover_Watching);
            progressCount.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.VISIBLE);
        } else if (myStatus.equals("reading")) {
            textview.setText(R.string.cover_Reading);
            progressCount.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.VISIBLE);
        } else if (myStatus.equals("completed")) {
            textview.setText(R.string.cover_Completed);
        } else if (myStatus.equals("on-hold")) {
            textview.setText(R.string.cover_OnHold);
            progressCount.setVisibility(View.VISIBLE);
        } else if (myStatus.equals("dropped")) {
            textview.setText(R.string.cover_Dropped);
        } else if (myStatus.equals("plan to watch")) {
            textview.setText(R.string.cover_PlanningToWatch);
        } else if (myStatus.equals("plan to read")) {
            textview.setText(R.string.cover_PlanningToRead);
        } else {
            textview.setText("");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.record_igf_layout, container, false);
        viewflipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        Gridview = (GridView) view.findViewById(R.id.gridview);
        Gridview.setOnItemClickListener(this);
        Gridview.setOnScrollListener(this);

        context = getActivity();
        activity = getActivity();
        setColumns();
        useSecondaryAmounts = PrefManager.getUseSecondaryAmountsEnabled();
        if (PrefManager.getTraditionalListEnabled()) {
            Gridview.setNumColumns(1); //remain in the listview mode
            resource = R.layout.record_igf_listview;
        } else {
            resource = R.layout.record_igf_gridview;
        }

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        if (isOnHomeActivity()) {
            swipeRefresh.setOnRefreshListener((Home) getActivity());
        }
        swipeRefresh.setColorScheme(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        swipeRefresh.setEnabled(swipeRefreshEnabled);

        if (gl.size() > 0) // there are already records, fragment has been rotated
            refresh();

        NfcHelper.disableBeam(activity);

        if (callback != null)
            callback.onIGFReady(this);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        if (IGFCallbackListener.class.isInstance(activity))
            callback = (IGFCallbackListener) activity;
        recordStatusReceiver = new RecordStatusUpdatedReceiver(this);
        IntentFilter filter = new IntentFilter(RecordStatusUpdatedReceiver.RECV_IDENT);
        LocalBroadcastManager.getInstance(activity).registerReceiver(recordStatusReceiver, filter);
    }

    @Override
    public void onDetach() {
        if (recordStatusReceiver != null)
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(recordStatusReceiver);
        super.onDetach();
    }

    /**
     * Set the numbers columns for the best overview.
     */
    @SuppressLint("InlinedApi")
    public void setColumns() {
        float density = (context.getResources().getDisplayMetrics().densityDpi / 160f);
        int screenWidth = (int) (context.getResources().getConfiguration().screenWidthDp * density);
        float minWidth = 225 * density;
        int columns = (int) Math.ceil(screenWidth / minWidth);
        int width = screenWidth / columns;
        height = (int) (width / 0.7);
        Gridview.setNumColumns(columns);
    }

    /**
     * Check if the parent activity is Home.
     *
     * @return boolean If true then the parent activity is home
     */
    private boolean isOnHomeActivity() {
        return getActivity() != null && getActivity().getClass() == Home.class;
    }

    /**
     * Add +1 episode/volume/chapters to the anime/manga.
     *
     * Use null if the other record isn't available
     *
     * @param anime The Anime record that should increase by one
     * @param manga The manga record that should increase by one
     */
    public void setProgressPlusOne(Anime anime, Manga manga) {
        if (listType.equals(ListType.ANIME)) {
            anime.setWatchedEpisodes(anime.getWatchedEpisodes() + 1);
            if (anime.getWatchedEpisodes() == anime.getEpisodes())
                anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            new WriteDetailTask(listType, TaskJob.UPDATE, context, getAuthErrorCallback()).execute(anime);
        } else {
            manga.setProgress(useSecondaryAmounts, manga.getProgress(useSecondaryAmounts) + 1);
            if (manga.getProgress(useSecondaryAmounts) == manga.getTotal(useSecondaryAmounts))
                manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
            new WriteDetailTask(listType, TaskJob.UPDATE, context, getAuthErrorCallback()).execute(manga);
        }
        refresh();
    }

    /**
     * Mark the anime/manga as completed.
     *
     * Use null if the other record isn't available
     *
     * @param anime The Anime record that should be marked as complete
     * @param manga The manga record that should be marked as complete
     */
    public void setMarkAsComplete(Anime anime, Manga manga) {
        if (listType.equals(ListType.ANIME)) {
            anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            if (anime.getEpisodes() > 0)
                anime.setWatchedEpisodes(anime.getEpisodes());
            anime.setDirty(true);
            gl.remove(anime);
            new WriteDetailTask(listType, TaskJob.UPDATE, context, getAuthErrorCallback()).execute(anime);
        } else {
            manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
            manga.setDirty(true);
            gl.remove(manga);
            new WriteDetailTask(listType, TaskJob.UPDATE, context, getAuthErrorCallback()).execute(manga);
        }
        refresh();
    }

    /**
     * Handle the loading indicator.
     *
     * @param show If true then the IGF will show the indiacator
     */
    private void toggleLoadingIndicator(boolean show) {
        if (viewflipper != null) {
            viewflipper.setDisplayedChild(show ? 1 : 0);
        }
    }

    /**
     * Handle the SwipeRefresh animantion.
     *
     * @param show If true then the IGF will show the animation
     */
    public void toggleSwipeRefreshAnimation(boolean show) {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(show);
        }
    }

    /**
     * Handle the SwipeRefreshView.
     *
     * @param enabled If true then the SwipeRefreshView will be enabled
     */
    public void setSwipeRefreshEnabled(boolean enabled) {
        swipeRefreshEnabled = enabled;
        if (swipeRefresh != null) {
            swipeRefresh.setEnabled(enabled);
        }
    }

    private APIAuthenticationErrorListener getAuthErrorCallback() {
        return (APIAuthenticationErrorListener.class.isInstance(getActivity()) ? ((APIAuthenticationErrorListener) getActivity()) : null);
    }

    /**
     * Get the anime/manga lists.
     *
     * @param clear If true then the whole list will be cleared and loaded
     * @param task  Which list should be shown (top, popular, upcoming...)
     * @param list  Which list type should be shown (completed, dropped, in progress...)
     */
    public void getRecords(boolean clear, TaskJob task, int list) {
        if (task != null) {
            taskjob = task;
        }
        if (list != this.list) {
            this.list = list;
        }
        /* only show loading indicator if
         * - is not own list and on page 1
         * - force sync and list is empty (only show swipe refresh animation if not empty)
         * - clear is set
         */
        boolean isEmpty = gl.isEmpty();
        toggleLoadingIndicator((page == 1 && !isList()) || (taskjob.equals(TaskJob.FORCESYNC) && isEmpty) || clear);
        /* show swipe refresh animation if
         * - loading more pages
         * - forced update
         * - clear is unset
         */
        toggleSwipeRefreshAnimation((page > 1 && !isList() || taskjob.equals(TaskJob.FORCESYNC)) && !clear);
        loading = true;
        try {
            if (clear) {
                resetPage();
                gl.clear();
                if (ga == null) {
                    setAdapter();
                }
                ga.clear();
            }
            Bundle data = new Bundle();
            data.putInt("page", page);
            cancelNetworkTask();
            networkTask = new NetworkTask(taskjob, listType, context, data, this, getAuthErrorCallback());
            ArrayList<String> args = new ArrayList<String>();
            if (!username.equals("") && isList()) {
                args.add(username);
                if (isList()) {
                    args.add(MALManager.listSortFromInt(list, listType));
                }
            } else {
                args.add(query);
            }
            networkTask.execute(args.toArray(new String[args.size()]));
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "IGF.getRecords(): " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    /**
     * Get the search results of the query.
     *
     * @param search The query that should be searched for
     */
    public void searchRecords(String search) {
        if (search != null && !search.equals(query) && !search.isEmpty()) { // no need for searching the same again or empty string
            query = search;
            page = 1;
            setSwipeRefreshEnabled(false);
            getRecords(true, TaskJob.SEARCH, 0);
        }
    }

    /**
     * Reset the page number of anime/manga lists.
     */
    public void resetPage() {
        page = 1;
        if (Gridview != null) {
            Gridview.requestFocusFromTouch();
            Gridview.post(new Runnable() {
                @Override
                public void run() {
                    Gridview.setSelection(0);
                }
            });
        }
    }

    /**
     * Set the adapter anime/manga.
     */
    public void setAdapter() {
        ga = new ListViewAdapter<GenericRecord>(context, resource);
        ga.setNotifyOnChange(true);
    }

    /**
     * Refresh all the covers.
     */
    public void refresh() {
        try {
            filterTime();
            if (ga == null)
                setAdapter();
            ga.clear();
            ga.supportAddAll(gl);
            if (Gridview.getAdapter() == null)
                Gridview.setAdapter(ga);
        } catch (Exception e) {
            if (MALApi.isNetworkAvailable(context)) {
                Crashlytics.log(Log.ERROR, "MALX", "IGF.refresh(): " + e.getMessage());
                Crashlytics.logException(e);
                if (taskjob.equals(TaskJob.SEARCH)) {
                    Toast.makeText(activity.getApplicationContext(), R.string.toast_error_Search, Toast.LENGTH_SHORT).show();
                } else {
                    if (listType.equals(ListType.ANIME)) {
                        Toast.makeText(activity.getApplicationContext(), R.string.toast_error_Anime_Sync, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity.getApplicationContext(), R.string.toast_error_Manga_Sync, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(activity.getApplicationContext(), R.string.toast_error_noConnectivity, Toast.LENGTH_SHORT).show();
            }
        }
        loading = false;
    }

    /**
     * Check if the taskjob is my personal anime/manga list.
     *
     * @param job The current taskjob to compare with
     * @return boolean If true then the list is of the logged in user
     */
    public boolean isList(TaskJob job) {
        return job != null && (job.equals(TaskJob.GETLIST) || job.equals(TaskJob.FORCESYNC));
    }

    /**
     * Check if the taskjob is my personal anime/manga list.
     *
     * @return boolean If true then the list is of the logged in user
     */
    public boolean isList() {
        return isList(taskjob);
    }

    /**
     * Check if the taskjob will return paged results
     *
     * @param job The current taskjob to compare with
     * @return boolean If true then it will return paged results
     */
    private boolean jobReturnsPagedResults(TaskJob job) {
        return !isList(job);
    }

    /**
     * Cancel the networktask.
     */
    public void cancelNetworkTask() {
        if (networkTask != null)
            networkTask.cancelTask();
    }

    /**
     * Inverse the list and refresh it.
     */
    public void inverse() {
        Collections.reverse(gl);
        refresh();
    }

    /**
     * Hide airing dates for anilist airing list.
     */
    private void filterTime() {
        if (!AccountService.isMAL() && taskjob == TaskJob.GETMOSTPOPULAR && PrefManager.getAiringOnly() && listType == ListType.ANIME) {
            ArrayList<GenericRecord> record = new ArrayList<GenericRecord>();
            for (GenericRecord gr : gl)
                if (((Anime) gr).getAiring() != null)
                    record.add(gr);
            gl = record;
        }
    }

    /**
     * Set and hide airing dates for anilist airing list.
     */
    public void toggleAiringTime() {
        PrefManager.setAiringOnly(!PrefManager.getAiringOnly());
        PrefManager.commitChanges();
        filterTime();
        refresh();
    }

    /**
     * Set the list with the new page/list.
     */
    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type, Bundle data, boolean cancelled) {
        if (!cancelled || job.equals(TaskJob.FORCESYNC)) { // forced sync tasks are completed even after cancellation
            ArrayList resultList;
            try {
                if (type == ListType.ANIME) {
                    resultList = (ArrayList<Anime>) result;
                } else {
                    resultList = (ArrayList<Manga>) result;
                }
            } catch (ClassCastException e) {
                Crashlytics.log(Log.ERROR, "MALX", "IGF.onNetworkTaskFinished(): " + result.getClass().toString());
                Crashlytics.logException(e);
                resultList = null;
            }
            if (resultList != null) {
                if (resultList.size() == 0 && taskjob.equals(TaskJob.SEARCH)) {
                    if (this.page == 1)
                        doRecordsLoadedCallback(type, job, false, true, cancelled);
                } else {
                    if (job.equals(TaskJob.FORCESYNC))
                        doRecordsLoadedCallback(type, job, false, false, cancelled);
                    if (!cancelled) {  // only add results if not cancelled (on FORCESYNC)
                        if (clearAfterLoading || job.equals(TaskJob.FORCESYNC)) { // a forced sync always reloads all data, so clear the list
                            gl.clear();
                            clearAfterLoading = false;
                        }
                        if (jobReturnsPagedResults(job))
                            hasmorepages = resultList.size() > 0;
                        gl.addAll(resultList);
                        refresh();
                    }
                }
            } else {
                doRecordsLoadedCallback(type, job, true, false, cancelled); // no resultList ? something went wrong
            }
        }
        networkTask = null;
        toggleSwipeRefreshAnimation(false);
        toggleLoadingIndicator(false);
    }

    @Override
    public void onNetworkTaskError(TaskJob job, ListType type, Bundle data, boolean cancelled) {
        doRecordsLoadedCallback(type, job, true, true, false);
        toggleSwipeRefreshAnimation(false);
        toggleLoadingIndicator(false);
    }

    /**
     * Trigger to the parent activity that the records are loaded.
     *
     * @param type        The ListType
     * @param job         Which list should be shown (top, popular, upcoming...)
     * @param error       If true then there was an error
     * @param resultEmpty If true then the result we got is empty
     * @param cancelled   If true then the user/activity canceled the request
     */
    private void doRecordsLoadedCallback(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {
        if (callback != null)
            callback.onRecordsLoadingFinished(type, job, error, resultEmpty, cancelled);
    }

    /**
     * Handle the gridview click by navigating to the detailview.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent startDetails = new Intent(getView().getContext(), DetailView.class);
        startDetails.putExtra("recordID", gl.get(position).getId());
        startDetails.putExtra("recordType", listType);
        startDetails.putExtra("username", username);
        startActivity(startDetails);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /**
     * Load more pages if we are almost on the bottom.
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // don't do anything if there is nothing in the list
        if (firstVisibleItem == 0 && visibleItemCount == 0 && totalItemCount == 0)
            return;
        if (totalItemCount - firstVisibleItem <= (visibleItemCount * 2) && !loading && hasmorepages) {
            loading = true;
            if (jobReturnsPagedResults(taskjob)) {
                page++;
                getRecords(false, null, list);
            }
        }
    }

    /**
     * Set the username.
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
        ownList = !(username == null || username.equals("")) && AccountService.getUsername().equals(username);
    }

    // user updated record on DetailsView, so update the list if necessary
    @Override
    public void onRecordStatusUpdated(ListType type) {
        // broadcast received
        if (type != null && type.equals(listType) && isList()) {
            clearAfterLoading = true;
            getRecords(false, TaskJob.GETLIST, list);
        }
    }

    static class ViewHolder {
        TextView label;
        TextView progressCount;
        TextView flavourText;
        ImageView cover;
        ImageView actionButton;
    }

    /**
     * The custom adapter for the covers anime/manga.
     */
    public class ListViewAdapter<T> extends ArrayAdapter<T> {

        public ListViewAdapter(Context context, int resource) {
            super(context, resource);
        }

        @SuppressWarnings("deprecation")
        public View getView(int position, View view, ViewGroup parent) {
            final GenericRecord record = gl.get(position);
            ViewHolder viewHolder;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(resource, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.label = (TextView) view.findViewById(R.id.animeName);
                viewHolder.progressCount = (TextView) view.findViewById(R.id.watchedCount);
                viewHolder.cover = (ImageView) view.findViewById(R.id.coverImage);
                viewHolder.actionButton = (ImageView) view.findViewById(R.id.popUpButton);
                viewHolder.flavourText = (TextView) view.findViewById(R.id.stringWatched);

                view.setTag(viewHolder);
                if (resource != R.layout.record_igf_listview)
                    view.getLayoutParams().height = height;
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            try {

                if (taskjob.equals(TaskJob.GETMOSTPOPULAR) || taskjob.equals(TaskJob.GETTOPRATED)) {
                    viewHolder.actionButton.setVisibility(View.GONE);
                    if (AccountService.isMAL()) {
                        viewHolder.progressCount.setVisibility(View.VISIBLE);
                        viewHolder.progressCount.setText(Integer.toString(position + 1));
                        viewHolder.flavourText.setText(R.string.label_Number);
                    } else if (listType.equals(ListType.ANIME) && ((Anime) record).getAiring() != null) {
                        viewHolder.progressCount.setVisibility(View.GONE);
                        viewHolder.flavourText.setText(MALDateTools.formatDateString(((Anime) record).getAiring().getTime(), context, true));
                    } else {
                        viewHolder.progressCount.setVisibility(View.GONE);
                        viewHolder.flavourText.setText(getString(R.string.unknown));
                    }
                } else {
                    // only show actionbutton on own list
                    viewHolder.actionButton.setVisibility(ownList ? View.VISIBLE : View.GONE);
                    if (listType.equals(ListType.ANIME)) {
                        viewHolder.progressCount.setText(Integer.toString(((Anime) record).getWatchedEpisodes()));
                        setStatus(((Anime) record).getWatchedStatus(), viewHolder.flavourText, viewHolder.progressCount, viewHolder.actionButton);
                    } else {
                        if (useSecondaryAmounts)
                            viewHolder.progressCount.setText(Integer.toString(((Manga) record).getVolumesRead()));
                        else
                            viewHolder.progressCount.setText(Integer.toString(((Manga) record).getChaptersRead()));
                        setStatus(((Manga) record).getReadStatus(), viewHolder.flavourText, viewHolder.progressCount, viewHolder.actionButton);
                    }
                }
                viewHolder.label.setText(record.getTitle());

                Picasso.with(context)
                        .load(record.getImageUrl())
                        .error(R.drawable.cover_error)
                        .placeholder(R.drawable.cover_loading)
                        .into(viewHolder.cover);

                if (viewHolder.actionButton.getVisibility() == View.VISIBLE) {
                    viewHolder.actionButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popup = new PopupMenu(context, v);
                            popup.getMenuInflater().inflate(R.menu.record_popup, popup.getMenu());
                            if (!listType.equals(ListType.ANIME))
                                popup.getMenu().findItem(R.id.plusOne).setTitle(R.string.action_PlusOneRead);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.plusOne:
                                            if (listType.equals(ListType.ANIME))
                                                setProgressPlusOne((Anime) record, null);
                                            else
                                                setProgressPlusOne(null, (Manga) record);
                                            break;
                                        case R.id.markCompleted:
                                            if (listType.equals(ListType.ANIME))
                                                setMarkAsComplete((Anime) record, null);
                                            else
                                                setMarkAsComplete(null, (Manga) record);
                                            break;
                                    }
                                    return true;
                                }
                            });
                            popup.show();
                        }
                    });
                }
            } catch (Exception e) {
                Crashlytics.log(Log.ERROR, "MALX", "IGF.ListViewAdapter(): " + e.getMessage());
                Crashlytics.logException(e);
            }
            return view;
        }

        public void supportAddAll(Collection<? extends T> collection) {
            for (T record : collection) {
                this.add(record);
            }
        }
    }
}