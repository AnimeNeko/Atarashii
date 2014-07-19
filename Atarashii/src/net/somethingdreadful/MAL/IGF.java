package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.NetworkTaskCallbackListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import java.util.ArrayList;
import java.util.Collection;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class IGF extends Fragment implements OnScrollListener, OnItemLongClickListener, OnItemClickListener, NetworkTaskCallbackListener {

    Context context;
    ListType listType = ListType.ANIME; // just to have it proper initialized
    TaskJob taskjob;
    GridView Gridview;
    PrefManager pref;
    ViewFlipper viewflipper;
    SwipeRefreshLayout swipeRefresh;
    Activity activity;
    ArrayList<GenericRecord> gl = new ArrayList<GenericRecord>();
    ListViewAdapter<GenericRecord> ga;
    IGFCallbackListener callback;

    NetworkTask networkTask;

    int page = 1;
    int list = -1;
    int resource;
    boolean useSecondaryAmounts;
    boolean loading = true;
    boolean detail = false;
    /* setSwipeRefreshEnabled() may be called before swipeRefresh exists (before onCreateView() is
     * called), so save it and apply it in onCreateView() */
    boolean swipeRefreshEnabled = true;

    String query;

    /*
     * set the watched/read count & status on the covers.
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
        Gridview.setOnItemLongClickListener(this);
        Gridview.setOnScrollListener(this);

        context = getActivity();
        activity = getActivity();
        pref = new PrefManager(context);
        useSecondaryAmounts = pref.getUseSecondaryAmountsEnabled();
        if (pref.getTraditionalListEnabled()) {
            Gridview.setColumnWidth((int) Math.pow(9999, 9999)); //remain in the listview mode
            resource = R.layout.record_igf_listview;
        } else {
            resource = R.layout.record_igf_gridview;
        }

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        if (isOnHomeActivity()) {
            swipeRefresh.setOnRefreshListener((Home) getActivity());
            swipeRefresh.setColorScheme(
                    R.color.holo_blue_bright,
                    R.color.holo_green_light,
                    R.color.holo_orange_light,
                    R.color.holo_red_light
            );
        }
        swipeRefresh.setEnabled(swipeRefreshEnabled);

        if (taskjob != null && !taskjob.equals(TaskJob.SEARCH)) {
            if (list == -1)
                getRecords(true, null, pref.getDefaultList());
            else
                refresh();
        }

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
            callback = (IGFCallbackListener)activity;
    }

    private boolean isOnHomeActivity() {
        return getActivity() != null && getActivity().getClass() == Home.class;
    }

    /*
     * add +1 episode/volume/chapters to the anime/manga.
     */
    public void setProgressPlusOne(Anime anime, Manga manga) {
        if (listType.equals(ListType.ANIME)) {
            anime.setWatchedEpisodes(anime.getWatchedEpisodes() + 1);
            if (anime.getWatchedEpisodes() == anime.getEpisodes())
                anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            new WriteDetailTask(listType, TaskJob.UPDATE, context).execute(anime);
        } else {
            manga.setProgress(useSecondaryAmounts, manga.getProgress(useSecondaryAmounts) + 1);
            if (manga.getProgress(useSecondaryAmounts) == manga.getTotal(useSecondaryAmounts))
                manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
            new WriteDetailTask(listType, TaskJob.UPDATE, context).execute(manga);
        }
        refresh();
    }

    /*
     * mark the anime/manga as completed.
     */
    public void setMarkAsComplete(Anime anime, Manga manga) {
        if (listType.equals(ListType.ANIME)) {
            anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            if (anime.getEpisodes() > 0)
                anime.setWatchedEpisodes(anime.getEpisodes());
            anime.setDirty(true);
            gl.remove(anime);
            new WriteDetailTask(listType, TaskJob.UPDATE, context).execute(anime);
        } else {
            manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
            manga.setDirty(true);
            gl.remove(manga);
            new WriteDetailTask(listType, TaskJob.UPDATE, context).execute(manga);
        }
        refresh();
    }

    /*
     * handle the loading indicator
     */
    private void toggleLoadingIndicator(boolean show) {
        if (viewflipper != null) {
            viewflipper.setDisplayedChild(show ? 1 : 0);
        }
    }

    public void toggleSwipeRefreshAnimation(boolean show) {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(show);
        }
    }

    public void setSwipeRefreshEnabled(boolean enabled) {
        swipeRefreshEnabled = enabled;
        if (swipeRefresh != null) {
            swipeRefresh.setEnabled(enabled);
        }
    }

    /*
     * get the anime/manga lists.
	 * (if clear is true the whole list will be cleared and loaded)
	 */
    public void getRecords(boolean clear, TaskJob task, int list) {
        if (task != null) {
            taskjob = task;
        }
        if (list != this.list) {
            this.list = list;
        }
        /* only show loading indicator if
         * - is not own list and not page 1
         * - force sync and list is empty (only show swipe refresh animation if not empty) or should
         *   be cleared
         */
        boolean isEmpty = gl.isEmpty();
        toggleLoadingIndicator((page == 1 && !isList()) || (taskjob.equals(TaskJob.FORCESYNC) && (isEmpty || clear)));
        /* show swipe refresh animation if
         * - loading more pages
         * - forced update
         */
        toggleSwipeRefreshAnimation((page > 1 && !isList() || taskjob.equals(TaskJob.FORCESYNC)) && !taskjob.equals(TaskJob.SEARCH));
		loading = true;
		try{
            if (clear){
                gl.clear();
                if (ga == null) {
                    setAdapter();
                }
                ga.clear();
                resetPage();
            }
            Bundle data = new Bundle();
            data.putInt("page", page);
            if (networkTask != null)
                networkTask.cancelTask();
            networkTask = new NetworkTask(taskjob, listType, context, data, this);
            networkTask.execute(isList() ? MALManager.listSortFromInt(list, listType) : query);
        }catch (Exception e){
            Log.e("MALX", "error getting records: " + e.getMessage());
        }
    }

    public void searchRecords(String search) {
        if (search != null && !search.equals(query) && !search.isEmpty()) { // no need for searching the same again or empty string
            query = search;
            page = 1;
            setSwipeRefreshEnabled(false);
            getRecords(true, TaskJob.SEARCH, 0);
        }

    }

    /*
     * reset the page number of anime/manga lists.
     */
    public void resetPage() {
        if (!isList()) {
            page = 1;
            Gridview.setSelection(0);
        }
    }

    /*
     * set the adapter anime/manga
     */
    public void setAdapter() {
        ga = new ListViewAdapter<GenericRecord>(context, resource);
        ga.setNotifyOnChange(true);
    }

    /*
     * refresh the covers.
     */
    public void refresh() {
        try {
            if (ga == null)
                setAdapter();
            ga.clear();
            ga.supportAddAll(gl);
            if (Gridview.getAdapter() == null)
                Gridview.setAdapter(ga);
        } catch (Exception e) {
            if (MALApi.isNetworkAvailable(context)) {
                e.printStackTrace();
                if (taskjob.equals(TaskJob.SEARCH)) {
                    Crouton.makeText(activity, R.string.crouton_error_Search, Style.ALERT).show();
                } else {
                    if (listType.equals(ListType.ANIME)) {
                        Crouton.makeText(activity, R.string.crouton_error_Anime_Sync, Style.ALERT).show();
                    } else {
                        Crouton.makeText(activity, R.string.crouton_error_Manga_Sync, Style.ALERT).show();
                    }
                }
                Log.e("MALX", "error on refresh: " + e.getMessage());
            } else {
                Crouton.makeText(activity, R.string.crouton_error_noConnectivity, Style.ALERT).show();
            }
        }
        loading = false;
    }

    /*
     * check if the taskjob is my personal anime/manga list
     */
    public boolean isList() {
        return taskjob != null && (taskjob.equals(TaskJob.GETLIST) || taskjob.equals(TaskJob.FORCESYNC));
    }

    /*
     * set the list with the new page/list.
     */
    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type, Bundle data, boolean cancelled) {
        if (!cancelled) // don't change the UI if cancelled
            toggleLoadingIndicator(false);
        if ( !cancelled || (cancelled && job.equals(TaskJob.FORCESYNC))) { // forced sync tasks are completed even after cancellation
            ArrayList resultList;
            try {
                if (type == ListType.ANIME) {
                    resultList = (ArrayList<Anime>) result;
                } else {
                    resultList = (ArrayList<Manga>) result;
                }
            } catch (ClassCastException e) {
                Log.e("MALX", "error reading result because of invalid result class: " + result.getClass().toString());
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
                        if (detail || job.equals(TaskJob.FORCESYNC)) { // a forced sync always reloads all data, so clear the list
                            gl.clear();
                            detail = false;
                        }
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
    }

    private void doRecordsLoadedCallback(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {
        if (callback != null)
            callback.onRecordsLoadingFinished(type, job, error, resultEmpty, cancelled);
    }

    /*
     * handle the gridview click by navigating to the detailview.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent startDetails = new Intent(getView().getContext(), DetailView.class);
        startDetails.putExtra("net.somethingdreadful.MAL.recordID", ga.getItem(position).getId());
        startDetails.putExtra("net.somethingdreadful.MAL.recordType", listType);
        startActivity(startDetails);
        if (isList() || taskjob.equals(TaskJob.SEARCH))
            this.detail = true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /*
     * load more pages if we are almost on the bottom.
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount - firstVisibleItem <= (visibleItemCount * 2) && !loading) {
            loading = true;
            if (taskjob != TaskJob.GETLIST && taskjob != TaskJob.FORCESYNC) {
                page = page + 1;
                getRecords(false, null, 0);
            }
        }
    }

    /*
     * corpy the anime title to the clipboard on long click.
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Crouton.makeText(activity, R.string.crouton_info_Copied, Style.CONFIRM).show();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager c = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            c.setText(gl.get(position).getTitle());
        } else {
            android.content.ClipboardManager c1 = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData c2;
            c2 = android.content.ClipData.newPlainText("Atarashii", gl.get(position).getTitle());
            c1.setPrimaryClip(c2);
        }
        return false;
    }

    static class ViewHolder {
        TextView label;
        TextView progressCount;
        TextView flavourText;
        ImageView cover;
        ImageView bar;
        ImageView actionButton;
    }

    /*
     * the custom adapter for the covers anime/manga.
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
                viewHolder.bar = (ImageView) view.findViewById(R.id.textOverlayPanel);
                viewHolder.actionButton = (ImageView) view.findViewById(R.id.popUpButton);
                viewHolder.flavourText = (TextView) view.findViewById(R.id.stringWatched);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            try {

                if (taskjob.equals(TaskJob.GETMOSTPOPULAR) || taskjob.equals(TaskJob.GETTOPRATED)) {
                    viewHolder.progressCount.setVisibility(View.VISIBLE);
                    viewHolder.progressCount.setText(Integer.toString(position + 1));
                    viewHolder.actionButton.setVisibility(View.GONE);
                    viewHolder.flavourText.setText(R.string.label_Number);
                } else if (listType.equals(ListType.ANIME)) {
                    viewHolder.progressCount.setText(Integer.toString(((Anime) record).getWatchedEpisodes()));
                    setStatus(((Anime) record).getWatchedStatus(), viewHolder.flavourText, viewHolder.progressCount, viewHolder.actionButton);
                } else {
                    if (useSecondaryAmounts)
                        viewHolder.progressCount.setText(Integer.toString(((Manga) record).getVolumesRead()));
                    else
                        viewHolder.progressCount.setText(Integer.toString(((Manga) record).getChaptersRead()));
                    setStatus(((Manga) record).getReadStatus(), viewHolder.flavourText, viewHolder.progressCount, viewHolder.actionButton);
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
                viewHolder.bar.setAlpha(175);
            } catch (Exception e) {
                Log.e("MALX", "error on the ListViewAdapter: " + e.getMessage());
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