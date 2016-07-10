package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public class IGF extends Fragment implements OnScrollListener, OnItemClickListener, NetworkTask.NetworkTaskListener, RecordStatusUpdatedReceiver.RecordStatusUpdatedListener {
    private ListType listType = ListType.ANIME; // just to have it proper initialized
    private Context context;
    public TaskJob taskjob;
    private Activity activity;
    private NetworkTask networkTask;
    private IGFCallbackListener callback;
    private ListViewAdapter<GenericRecord> ga;
    private boolean popupEnabled = true;
    private ArrayList<GenericRecord> gl = new ArrayList<>();
    private ArrayList<GenericRecord> backGl = new ArrayList<>();

    @BindView(R.id.gridview)
    GridView Gridview;
    @BindView(R.id.viewFlipper)
    ViewFlipper viewflipper;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    private RecordStatusUpdatedReceiver recordStatusReceiver;

    private int page = 1;
    public int list = -1;
    private int resource;
    private int height = 0;
    private int sortType = 1;
    @Getter
    private boolean isAnime = true;
    @Getter
    private boolean isList = true;
    private boolean inverse = false;
    @Getter private boolean loading = true;
    private boolean useSecondaryAmounts;
    private boolean hasmorepages = false;
    private boolean clearAfterLoading = false;
    private boolean details = false;
    private boolean numberList = false;
    /* setSwipeRefreshEnabled() may be called before swipeRefresh exists (before onCreateView() is
     * called), so save it and apply it in onCreateView() */
    private boolean swipeRefreshEnabled = true;

    private String query;
    @Setter
    @Getter
    private String username = null;

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("gl", gl);
        state.putSerializable("backGl", backGl);
        state.putSerializable("listType", listType);
        state.putSerializable("taskjob", taskjob);
        state.putInt("page", page);
        state.putInt("list", list);
        state.putInt("sortType", sortType);
        state.putInt("resource", resource);
        state.putBoolean("inverse", inverse);
        state.putBoolean("hasmorepages", hasmorepages);
        state.putBoolean("popupEnabled", popupEnabled);
        state.putBoolean("swipeRefreshEnabled", swipeRefreshEnabled);
        state.putBoolean("useSecondaryAmounts", useSecondaryAmounts);
        state.putBoolean("details", details);
        state.putBoolean("numberList", numberList);
        state.putBoolean("isAnime", isAnime);
        state.putBoolean("isList", isList);
        state.putString("query", query);
        state.putString("username", username);
        super.onSaveInstanceState(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.record_igf_layout, container, false);
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.bg_dark));
        ButterKnife.bind(this, view);

        Gridview.setOnItemClickListener(this);
        Gridview.setOnScrollListener(this);

        if (state != null) {
            backGl = (ArrayList<GenericRecord>) state.getSerializable("backGl");
            gl = (ArrayList<GenericRecord>) state.getSerializable("gl");
            listType = (ListType) state.getSerializable("listType");
            taskjob = (TaskJob) state.getSerializable("taskjob");
            page = state.getInt("page");
            list = state.getInt("list");
            resource = state.getInt("resource");
            hasmorepages = state.getBoolean("hasmorepages");
            swipeRefreshEnabled = state.getBoolean("swipeRefreshEnabled");
            query = state.getString("query");
            username = state.getString("username");
            details = state.getBoolean("details");
            isAnime = state.getBoolean("isAnime");
            numberList = state.getBoolean("numberList");
            useSecondaryAmounts = state.getBoolean("useSecondaryAmounts");
            isList = state.getBoolean("isList");
            sortType = state.getInt("sortType");
            inverse = state.getBoolean("inverse");
            popupEnabled = state.getBoolean("popupEnabled");
        } else {
            resource = PrefManager.getTraditionalListEnabled() ? R.layout.record_igf_listview : R.layout.record_igf_gridview;
            useSecondaryAmounts = PrefManager.getUseSecondaryAmountsEnabled();
        }

        activity = getActivity();
        context = activity;
        setColumns();

        if (activity instanceof Home)
            swipeRefresh.setOnRefreshListener((Home) getActivity());
        if (activity instanceof IGFCallbackListener)
            callback = (IGFCallbackListener) activity;
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(swipeRefreshEnabled);

        recordStatusReceiver = new RecordStatusUpdatedReceiver(this);
        IntentFilter filter = new IntentFilter(RecordStatusUpdatedReceiver.RECV_IDENT);
        LocalBroadcastManager.getInstance(activity).registerReceiver(recordStatusReceiver, filter);

        if (gl.size() > 0) // there are already records, fragment has been rotated
            refresh();

        if (callback != null)
            callback.onIGFReady(this);
        return view;
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
    private void setColumns() {
        int screenWidth = Theme.convert(context.getResources().getConfiguration().screenWidthDp);
        if (PrefManager.getTraditionalListEnabled()) {
            Gridview.setNumColumns(1); //remain in the listview mode
        } else if (PrefManager.getIGFColumns() == 0) {
            int columns = (int) Math.ceil(screenWidth / Theme.floatConvert(225));
            int width = screenWidth / columns;
            height = (int) (width / 0.7);
            Gridview.setNumColumns(columns);
            PrefManager.setIGFColumns(columns);
            PrefManager.commitChanges();
        } else {
            height = (int) (screenWidth / PrefManager.getIGFColumns() / 0.7);
            Gridview.setNumColumns(PrefManager.getIGFColumns());
        }
    }

    /**
     * Set listType, boolean isAnime
     */
    public void setListType(ListType listType) {
        Crashlytics.log(Log.INFO, "Atarashii", "IGF.sort(): listType=" + listType);
        this.listType = listType;
        isAnime = listType.equals(ListType.ANIME);
    }

    /**
     * Init the list other than the user.
     *
     * @param listType ListType, boolean isAnime
     * @return The fragment
     */
    public IGF setFriendList(ListType listType) {
        setListType(listType);
        this.popupEnabled = false;
        return this;
    }

    /**
     * Filter the list by status type.
     */
    public void filter(int statusType) {
        switch (statusType) {
            case 1:
                gl = backGl;
                refresh();
                break;
            case 2:
                filterStatus(isAnime() ? "watching" : "reading");
                break;
            case 3:
                filterStatus("completed");
                break;
            case 4:
                filterStatus("on-hold");
                break;
            case 5:
                filterStatus("dropped");
                break;
            case 6:
                filterStatus(isAnime() ? "plan to watch" : "plan to read");
                break;
            default:
                gl = backGl;
                refresh();
                break;
        }
    }

    /**
     * Filter the status by the provided String.
     *
     * @param status The status of the record
     */
    private void filterStatus(String status) {
        ArrayList<GenericRecord> gr = new ArrayList<>();
        if (backGl != null && backGl.size() > 0) {
            if (isAnime())
                for (GenericRecord record : backGl) {
                    if (((Anime) record).getWatchedStatus().equals(status))
                        gr.add(record);
                }
            else
                for (GenericRecord record : backGl) {
                    if (((Manga) record).getReadStatus().equals(status))
                        gr.add(record);
                }
        }
        gl = gr;
        sortList(sortType);
    }

    /**
     * Sort records by the sortType ID.
     *
     * @param sortType The sort ID
     */
    public void sort(int sortType) {
        Crashlytics.log(Log.INFO, "Atarashii", "IGF.sort(" + listType + "): sortType=" + sortType);
        this.sortType = sortType;
        if (taskjob.equals(TaskJob.GETFRIENDLIST) && !isLoading()) {
            sortList(sortType);
        } else {
            getRecords(true, taskjob, list);
        }
    }

    /**
     * Instead of reloading we just sort them.
     * <p/>
     * note: do not change only this part but also the DatabaseManager part!
     *
     * @param sortType The sort type
     */
    private void sortList(final int sortType) {
        Collections.sort(gl != null && gl.size() > 0 ? gl : new ArrayList<GenericRecord>(), new Comparator<GenericRecord>() {
            @Override
            public int compare(GenericRecord GR1, GenericRecord GR2) {
                switch (sortType) {
                    case 1:
                        return GR1.getTitle().toLowerCase().compareTo(GR2.getTitle().toLowerCase());
                    case 2:
                        return compareCheck(((Integer) GR2.getScore()).compareTo(GR1.getScore()), GR1, GR2);
                    case 3:
                        return compareCheck(GR1.getType().toLowerCase().compareTo(GR2.getType().toLowerCase()), GR1, GR2);
                    case 4:
                        return compareCheck(GR1.getStatus().toLowerCase().compareTo(GR2.getStatus().toLowerCase()), GR1, GR2);
                    case 5:
                        return compareCheck(((Integer) ((Anime) GR1).getWatchedEpisodes()).compareTo(((Anime) GR2).getWatchedEpisodes()), GR1, GR2);
                    case -5:
                        return compareCheck(((Integer) ((Manga) GR1).getChaptersRead()).compareTo(((Manga) GR2).getChaptersRead()), GR1, GR2);
                    default:
                        return GR1.getTitle().toLowerCase().compareTo(GR2.getTitle().toLowerCase());
                }
            }
        });
        if (inverse)
            Collections.reverse(gl);
        refresh();
    }

    /**
     * Used to sort records also on title if they are in the same x.
     *
     * @param x   The x passed by compareTo
     * @param GR1 The first record to compare
     * @param GR2 The second record to compare
     * @return int X the x sorting value
     */
    private int compareCheck(int x, GenericRecord GR1, GenericRecord GR2) {
        if (x != 0)
            return x;
        else
            return GR1.getTitle().toLowerCase().compareTo(GR2.getTitle().toLowerCase());
    }

    /**
     * Show details on covers.
     */
    public void details() {
        this.details = !details;
        if (details)
            resource = R.layout.record_igf_details;
        else
            resource = PrefManager.getTraditionalListEnabled() ? R.layout.record_igf_listview : R.layout.record_igf_gridview;
        refresh();
    }

    /**
     * Get the details status.
     */
    public boolean getDetails() {
        return details;
    }

    /**
     * Get the amount of columns.
     *
     * @param portrait The orientation of the screen.
     * @return int The amount of columns
     */
    public static int getColumns(boolean portrait) {
        int screen;
        if (Theme.isPortrait() && portrait || !Theme.isPortrait() && !portrait)
            screen = Theme.convert(Theme.context.getResources().getConfiguration().screenWidthDp);
        else
            screen = Theme.convert(Theme.context.getResources().getConfiguration().screenHeightDp);
        return (int) Math.ceil(screen / Theme.floatConvert(225));
    }

    /**
     * Get the max amount of columns before the design breaks.
     *
     * @param portrait The orientation of the screen.
     * @return int The amount of max columns
     */
    public static int getMaxColumns(boolean portrait) {
        int screen;
        if (Theme.isPortrait() && portrait || !Theme.isPortrait() && !portrait)
            screen = Theme.convert(Theme.context.getResources().getConfiguration().screenWidthDp);
        else
            screen = Theme.convert(Theme.context.getResources().getConfiguration().screenHeightDp);
        return (int) Math.ceil(screen / Theme.convert(225)) + 2;
    }

    /**
     * Add +1 episode/volume/chapters to the anime/manga.
     * <p/>
     * Use null if the other record isn't available
     *
     * @param anime The Anime record that should increase by one
     * @param manga The manga record that should increase by one
     */
    private void setProgressPlusOne(Anime anime, Manga manga) {
        if (isAnime()) {
            anime.setWatchedEpisodes(anime.getWatchedEpisodes() + 1);
            new WriteDetailTask(listType, activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, anime);
        } else {
            manga.setProgress(useSecondaryAmounts, manga.getProgress(useSecondaryAmounts) + 1);
            new WriteDetailTask(listType, activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, manga);
        }
        refresh();
    }

    /**
     * Mark the anime/manga as completed.
     * <p/>
     * Use null if the other record isn't available
     *
     * @param anime The Anime record that should be marked as complete
     * @param manga The manga record that should be marked as complete
     */
    private void setMarkAsComplete(Anime anime, Manga manga) {
        if (isAnime()) {
            anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            gl.remove(anime);
            new WriteDetailTask(listType, activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, anime);
        } else {
            manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
            if (manga.getChapters() > 0)
                manga.setChaptersRead(manga.getChapters());
            if (manga.getVolumes() > 0)
                manga.setVolumesRead(manga.getVolumes());
            gl.remove(manga);
            new WriteDetailTask(listType, activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, manga);
        }
        refresh();
    }

    /**
     * Handle the loading indicator.
     *
     * @param show If true then the IGF will show the indiacator
     */
    private void toggleLoadingIndicator(boolean show) {
        if (viewflipper != null)
            viewflipper.setDisplayedChild(show ? 1 : 0);
    }

    /**
     * Handle the SwipeRefresh animantion.
     *
     * @param show If true then the IGF will show the animation
     */
    public void toggleSwipeRefreshAnimation(boolean show) {
        if (swipeRefresh != null)
            swipeRefresh.setRefreshing(show);
    }

    /**
     * Handle the SwipeRefreshView.
     *
     * @param enabled If true then the SwipeRefreshView will be enabled
     */
    public void setSwipeRefreshEnabled(boolean enabled) {
        swipeRefreshEnabled = enabled;
        if (swipeRefresh != null)
            swipeRefresh.setEnabled(enabled);
    }

    /**
     * Check of task contains any other taskjob.
     *
     * @param taskJob1 The first Taskjob to compare
     * @param taskJob2 The second Taskjob to compare
     * @return boolean True if they contain the taskjob
     */
    private boolean containsTask(TaskJob taskJob1, TaskJob taskJob2) {
        return taskJob1.toString().contains(taskJob2.toString());
    }

    /**
     * Browse trough the anime/manga lists.
     */
    public void getBrowse(HashMap<String, String> query, boolean clear) {
        taskjob = TaskJob.BROWSE;
        isList = false;
        if (clear) {
            resetPage();
            gl.clear();
            if (ga == null)
                setAdapter();
            ga.clear();
        }
        boolean isEmpty = gl.isEmpty();
        toggleLoadingIndicator((page == 1 && !isList()) || (taskjob.equals(TaskJob.FORCESYNC) && isEmpty));
        toggleSwipeRefreshAnimation(page > 1 && !isList() || taskjob.equals(TaskJob.FORCESYNC));
        loading = true;
        try {
            new NetworkTask(activity,listType, query, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "IGF.getBrowse(): " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    /**
     * Get the anime/manga lists.
     *
     * @param clear If true then the whole list will be cleared and loaded
     * @param task  Which list should be shown (top, popular, upcoming...)
     * @param list  Which list type should be shown (completed, dropped, in progress...)
     */
    public void getRecords(boolean clear, TaskJob task, int list) {
        if (task != null)
            taskjob = task;
        if (task != TaskJob.GETLIST && task != TaskJob.FORCESYNC && task != TaskJob.GETFRIENDLIST) {
            details = false;
            numberList = containsTask(taskjob, TaskJob.GETMOSTPOPULAR) || containsTask(taskjob, TaskJob.GETTOPRATED);
            resource = PrefManager.getTraditionalListEnabled() ? R.layout.record_igf_listview : R.layout.record_igf_gridview;
            isList = false;
        } else {
            isList = true;
        }
        if (list != this.list)
            this.list = list;
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
                if (ga == null)
                    setAdapter();
                ga.clear();
            }
            Bundle data = new Bundle();
            data.putInt("page", page);
            networkTask = new NetworkTask(taskjob, listType, activity, data, this);
            ArrayList<String> args = new ArrayList<>();
            if (taskjob.equals(TaskJob.GETFRIENDLIST)) {
                args.add(username);
                setSwipeRefreshEnabled(false);
            } else if (isList()) {
                setSwipeRefreshEnabled(true);
                args.add(ContentManager.listSortFromInt(list, listType));
                args.add(String.valueOf(sortType));
                args.add(String.valueOf(inverse));
            } else {
                args.add(query);
            }
            networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "IGF.getRecords(): " + e.getMessage());
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
    private void resetPage() {
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
    private void setAdapter() {
        ga = new ListViewAdapter<>(context, resource);
        ga.setNotifyOnChange(true);
    }

    /**
     * Refresh all the covers.
     */
    private void refresh() {
        try {
            if (ga == null)
                setAdapter();
            ga.clear();
            ga.supportAddAll(gl);
            if (Gridview.getAdapter() == null)
                Gridview.setAdapter(ga);
        } catch (Exception e) {
            if (APIHelper.isNetworkAvailable(context)) {
                Crashlytics.log(Log.ERROR, "Atarashii", "IGF.refresh(): " + e.getMessage());
                Crashlytics.logException(e);
                if (taskjob.equals(TaskJob.SEARCH)) {
                    Theme.Snackbar(activity, R.string.toast_error_Search);
                } else {
                    if (isAnime())
                        Theme.Snackbar(activity, R.string.toast_error_Anime_Sync);
                    else
                        Theme.Snackbar(activity, R.string.toast_error_Manga_Sync);
                }
            } else {
                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
            }
        }
        loading = false;
    }

    /**
     * Inverse the list and refresh it.
     */
    public void inverse() {
        this.inverse = !inverse;
        if (taskjob.equals(TaskJob.GETFRIENDLIST)) {
            if (gl != null && gl.size() > 0)
                Collections.reverse(gl);
            refresh();
        } else {
            getRecords(true, taskjob, list);
        }
    }

    /**
     * Set the list with the new page/list.
     */
    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type) {
        ArrayList resultList;
        try {
            if (type == ListType.ANIME)
                resultList = (ArrayList<Anime>) result;
            else
                resultList = (ArrayList<Manga>) result;
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "IGF.onNetworkTaskFinished(): " + result.getClass().toString());
            Crashlytics.logException(e);
            resultList = null;
        }
        if (resultList != null) {
            if (resultList.size() == 0 && taskjob.equals(TaskJob.SEARCH)) {
                if (this.page == 1)
                    doRecordsLoadedCallback(job);
            } else {
                if (job.equals(TaskJob.FORCESYNC))
                    doRecordsLoadedCallback(job);
                if (clearAfterLoading || job.equals(TaskJob.FORCESYNC) || job.equals(TaskJob.GETFRIENDLIST)) { // a forced sync always reloads all data, so clear the list
                    gl.clear();
                    clearAfterLoading = false;
                }
                hasmorepages = resultList.size() > 0;
                gl.addAll(resultList);
                if (taskjob.equals(TaskJob.GETFRIENDLIST)) {
                    backGl.addAll(resultList);
                    sortList(sortType);
                } else {
                    refresh();
                }
            }
        } else {
            doRecordsLoadedCallback(job); // no resultList ? something went wrong
        }
        networkTask = null;
        toggleSwipeRefreshAnimation(false);
        toggleLoadingIndicator(false);
    }

    @Override
    public void onNetworkTaskError(TaskJob job) {
        doRecordsLoadedCallback(job);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleSwipeRefreshAnimation(false);
                toggleLoadingIndicator(false);
            }
        });
    }

    /**
     * Trigger to the parent activity that the records are loaded.
     *
     * @param job Which list should be shown (top, popular, upcoming...)
     */
    private void doRecordsLoadedCallback(TaskJob job) {
        if (callback != null)
            callback.onRecordsLoadingFinished(job);
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
        if (!isList()) {
            if (firstVisibleItem == 0 && visibleItemCount == 0 && totalItemCount == 0)
                return;
            if (totalItemCount - firstVisibleItem <= (visibleItemCount * 2) && !loading && hasmorepages) {
                loading = true;
                page++;
                getRecords(false, null, list);
            }
        }
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


    /**
     * Handle the gridview click by navigating to the detailview.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("DetailView")
                .putContentType(String.valueOf(listType))
                .putContentId(String.valueOf(listType).charAt(0) + String.valueOf(gl.get(position).getId())));
        callback.onItemClick(gl.get(position).getId(), listType, username);
    }

    static class ViewHolder {
        TextView label;
        TextView progressCount;
        TextView flavourText;
        ImageView cover;
        ImageView actionButton;
        TextView scoreCount;
        TextView typeCount;
        TextView statusCount;
    }

    /**
     * The custom adapter for the covers anime/manga.
     */
    public class ListViewAdapter<T> extends ArrayAdapter<T> {
        final LayoutInflater inflater;
        final boolean listView;
        final String StatusWatching;
        final String StatusReading;
        final String StatusCompleted;
        final String StatusOnHold;
        final String StatusDropped;
        final String StatusPlanningToWatch;
        final String StatusPlanningToRead;
        final String Number;
        final boolean isMAL;

        public ListViewAdapter(Context context, int resource) {
            super(context, resource);

            // Get the string to make the scrolling smoother
            StatusWatching = getString(R.string.cover_Watching);
            StatusReading = getString(R.string.cover_Reading);
            StatusCompleted = getString(R.string.cover_Completed);
            StatusOnHold = getString(R.string.cover_OnHold);
            StatusDropped = getString(R.string.cover_Dropped);
            StatusPlanningToWatch = getString(R.string.cover_PlanningToWatch);
            StatusPlanningToRead = getString(R.string.cover_PlanningToRead);
            Number = getString(R.string.label_Number);

            isMAL = AccountService.isMAL();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listView = resource != R.layout.record_igf_listview;
        }

        @SuppressWarnings("deprecation")
        public View getView(int position, View view, ViewGroup parent) {
            final GenericRecord record = gl.get(position);
            Anime animeRecord;
            Manga mangaRecord;
            ViewHolder viewHolder = null;
            String status;
            int progress;

            if (isAnime()) {
                animeRecord = (Anime) record;
                status = animeRecord.getWatchedStatus();
                progress = animeRecord.getWatchedEpisodes();
            } else {
                mangaRecord = (Manga) record;
                status = mangaRecord.getReadStatus();
                progress = useSecondaryAmounts ? mangaRecord.getVolumesRead() : mangaRecord.getChaptersRead();
            }

            if (view != null)
                viewHolder = (ViewHolder) view.getTag();

            if (view == null || (details && viewHolder.scoreCount == null) || (!details && viewHolder.scoreCount != null)) {
                view = inflater.inflate(resource, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.label = (TextView) view.findViewById(R.id.animeName);
                viewHolder.progressCount = (TextView) view.findViewById(R.id.watchedCount);
                viewHolder.cover = (ImageView) view.findViewById(R.id.coverImage);
                viewHolder.actionButton = (ImageView) view.findViewById(R.id.popUpButton);
                viewHolder.flavourText = (TextView) view.findViewById(R.id.stringWatched);
                viewHolder.scoreCount = (TextView) view.findViewById(R.id.scoreCount);
                viewHolder.typeCount = (TextView) view.findViewById(R.id.typeCount);
                viewHolder.statusCount = (TextView) view.findViewById(R.id.statusCount);

                view.setTag(viewHolder);
                if (listView)
                    view.getLayoutParams().height = height;
            }
            try {
                viewHolder.label.setText(record.getTitle());
                if (details) {
                    viewHolder.scoreCount.setText(String.valueOf(record.getScore()));
                    viewHolder.typeCount.setText(record.getType());
                    viewHolder.statusCount.setText(record.getStatus());
                }

                if (isList() && status != null) {
                    viewHolder.progressCount.setText(String.valueOf(progress));

                    switch (status) {
                        case "watching":
                            viewHolder.flavourText.setText(StatusWatching);
                            viewHolder.progressCount.setVisibility(View.VISIBLE);
                            if (popupEnabled) {
                                viewHolder.actionButton.setVisibility(View.VISIBLE);
                                viewHolder.actionButton.setOnClickListener(new ABOnClickListener(record));
                            } else {
                                viewHolder.actionButton.setVisibility(View.GONE);
                            }
                            break;
                        case "reading":
                            viewHolder.flavourText.setText(StatusReading);
                            viewHolder.progressCount.setVisibility(View.VISIBLE);
                            if (popupEnabled) {
                                viewHolder.actionButton.setVisibility(View.VISIBLE);
                                viewHolder.actionButton.setOnClickListener(new ABOnClickListener(record));
                            } else {
                                viewHolder.actionButton.setVisibility(View.GONE);
                            }
                            break;
                        case "completed":
                            viewHolder.flavourText.setText(StatusCompleted);
                            viewHolder.actionButton.setVisibility(View.GONE);
                            viewHolder.progressCount.setVisibility(View.GONE);
                            break;
                        case "on-hold":
                            viewHolder.flavourText.setText(StatusOnHold);
                            viewHolder.progressCount.setVisibility(View.VISIBLE);
                            viewHolder.actionButton.setVisibility(View.GONE);
                            break;
                        case "dropped":
                            viewHolder.flavourText.setText(StatusDropped);
                            viewHolder.actionButton.setVisibility(View.GONE);
                            viewHolder.progressCount.setVisibility(View.GONE);
                            break;
                        case "plan to watch":
                            viewHolder.flavourText.setText(StatusPlanningToWatch);
                            viewHolder.actionButton.setVisibility(View.GONE);
                            viewHolder.progressCount.setVisibility(View.GONE);
                            break;
                        case "plan to read":
                            viewHolder.flavourText.setText(StatusPlanningToRead);
                            viewHolder.actionButton.setVisibility(View.GONE);
                            viewHolder.progressCount.setVisibility(View.GONE);
                            break;
                        default:
                            viewHolder.flavourText.setText("");
                            viewHolder.actionButton.setVisibility(View.GONE);
                            viewHolder.progressCount.setVisibility(View.GONE);
                            break;
                    }
                } else {
                    viewHolder.actionButton.setVisibility(View.GONE);
                    if (isMAL && numberList) {
                        viewHolder.progressCount.setVisibility(View.VISIBLE);
                        viewHolder.progressCount.setText(String.valueOf(position + 1));
                        viewHolder.flavourText.setText(Number);
                    } else {
                        viewHolder.progressCount.setVisibility(View.GONE);
                        viewHolder.flavourText.setText(getString(R.string.unknown));
                    }
                }
                // Picasso will fail at high res images because of MAL support.
                // We will request a low res to at least display something.
                final ImageView cover = viewHolder.cover;
                Picasso.with(context)
                        .load(record.getImageUrl())
                        .error(R.drawable.cover_error)
                        .placeholder(R.drawable.cover_loading)
                        .into(viewHolder.cover, new Callback() {

                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context)
                                        .load(record.getImageUrl().replace("l.jpg", ".jpg"))
                                        .error(R.drawable.cover_error)
                                        .placeholder(R.drawable.cover_loading)
                                        .into(cover);
                            }
                        });
            } catch (Exception e) {
                Theme.logTaskCrash("IGF", "ListViewAdapter()", e);
            }
            return view;
        }

        public void supportAddAll(Collection<? extends T> collection) {
            for (T record : collection) {
                this.add(record);
            }
        }

        /**
         * Custom grid clicker for passing the right record
         */
        public class ABOnClickListener implements View.OnClickListener {
            final GenericRecord record;

            public ABOnClickListener(GenericRecord record) {
                this.record = record;
            }

            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenuInflater().inflate(R.menu.record_popup, popup.getMenu());
                if (!isAnime())
                    popup.getMenu().findItem(R.id.plusOne).setTitle(R.string.action_PlusOneRead);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.plusOne:
                                if (isAnime())
                                    setProgressPlusOne((Anime) record, null);
                                else
                                    setProgressPlusOne(null, (Manga) record);
                                break;
                            case R.id.markCompleted:
                                if (isAnime())
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
        }
    }

    public interface IGFCallbackListener {
        void onIGFReady(IGF igf);

        void onRecordsLoadingFinished(TaskJob job);

        void onItemClick(int id, ListType listType, String username);
    }
}