package net.somethingdreadful.MAL.forum;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.adapters.ForumMainAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;

public class ForumsTopics extends Fragment implements ForumNetworkTaskFinishedListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    public int id;
    public MALApi.ListType type = MALApi.ListType.MANGA;
    public ForumMain subBoard;
    public ForumMain topic;
    public ForumJob task;

    ForumActivity activity;
    View view;
    public ForumMainAdapter topicsAdapter;
    ProgressBar progressBar;
    RelativeLayout content;
    Card networkCard;
    ListView topics;

    Boolean loading = true;
    public int page = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.activity_forum_topics, container, false);

        topics = (ListView) view.findViewById(R.id.list);
        topicsAdapter = new ForumMainAdapter(activity, topics, getFragmentManager(), ForumJob.TOPICS);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        networkCard = (Card) view.findViewById(R.id.network_Card);
        content = (RelativeLayout) view.findViewById(R.id.content);

        topics.setOnScrollListener(this);
        topics.setOnItemClickListener(this);
        topics.setAdapter(topicsAdapter);
        topicsAdapter.setNotifyOnChange(true);


        toggle(1);

        if (bundle != null && bundle.getSerializable("task") != null) {
            topic = (ForumMain) bundle.getSerializable("topic");
            subBoard = (ForumMain) bundle.getSerializable("subBoard");
            id = bundle.getInt("id");
            task = (ForumJob) bundle.getSerializable("task");
            type = (MALApi.ListType) bundle.getSerializable("type");
            apply(task == ForumJob.SUBBOARD ? subBoard : topic);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("topic", topic);
        state.putSerializable("subBoard", subBoard);
        state.putInt("id", id);
        state.putSerializable("task", task);
        state.putSerializable("type", type);
        super.onSaveInstanceState(state);
    }

    /**
     * Change the records in this fragment.
     *
     * @param id The new id of the record
     * @return ForumJob The task of this fragment
     */
    public ForumJob setId(int id, ForumJob task) {
        if (this.id != id || this.task != task) {
            this.id = id;
            this.task = task;
            getRecords(1, task);
        }
        return task;
    }

    /**
     * Change the records in this fragment (search).
     *
     * @param query The topic title query
     * @return ForumJob The task of this fragment
     */
    public ForumJob setId(String query) {
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, ForumJob.SEARCH, 0).execute(query);
        else
            toggle(2);
        return ForumJob.SEARCH;
    }

    /**
     * Get the requested records.
     *
     * @param page The page number
     * @param task The task that should be performed
     */
    public void getRecords(int page, ForumJob task) {
        toggle(1);
        this.page = page;
        if (page == 1)
            topicsAdapter.clear();
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, task, id).execute(Integer.toString(page), type.toString());
        else
            toggle(2);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((ForumActivity) activity);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob task) {
        if (result != null && result.getList() != null)
            apply(result);
    }

    public void apply(ForumMain result) {
        topicsAdapter.supportAddAll(result.getList());
        toggle(0);
        loading = false;
        activity.setTitle(getString(R.string.title_activity_forum));
        if (task == ForumJob.SUBBOARD) {
            subBoard = result;
        } else {
            topic = result;
        }
    }

    /**
     * Handle the viewFlipper.
     *
     * 0 = The real content
     * 1 = The progress indicator
     * 2 = The network not available card
     *
     * @param number The number of the desired content
     */
    private void toggle(int number) {
        content.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // don't do anything if there is nothing in the list
        if (firstVisibleItem == 0 && visibleItemCount == 0 && totalItemCount == 0)
            return;
        if (totalItemCount - firstVisibleItem <= visibleItemCount && !loading && page < (task == ForumJob.SUBBOARD ? subBoard.getPages() : topic.getPages())) {
            loading = true;
            getRecords(page + 1, task);
            activity.setTitle(getString(R.string.layout_card_loading));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int itemID = ((Forum) topicsAdapter.getItem(position)).getId();
        if (task == ForumJob.SUBBOARD)
            activity.getDiscussion(itemID);
        else
            activity.getPosts(itemID);
    }
}