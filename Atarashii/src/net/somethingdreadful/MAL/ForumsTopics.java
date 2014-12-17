package net.somethingdreadful.MAL;

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

import net.somethingdreadful.MAL.adapters.ForumMainAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

public class ForumsTopics extends Fragment implements ForumNetworkTaskFinishedListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    ForumActivity activity;
    View view;
    ForumMainAdapter topicsAdapter;
    ProgressBar progressBar;
    RelativeLayout content;
    Card networkCard;
    ListView topics;
    Boolean loading = true;
    int id;
    int page = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_forum_topics, container, false);

        topics = (ListView) view.findViewById(R.id.list);
        topicsAdapter = new ForumMainAdapter(activity, topics, getFragmentManager(), TaskJob.TOPICS);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        networkCard = (Card) view.findViewById(R.id.network_Card);
        content = (RelativeLayout) view.findViewById(R.id.content);

        topics.setOnScrollListener(this);
        topics.setOnItemClickListener(this);
        topics.setAdapter(topicsAdapter);
        topicsAdapter.setNotifyOnChange(true);

        toggle(1);
        return view;
    }

    public void setId(int id, TaskJob task) {
        if (this.id != id) {
            this.id = id;
            getRecords(1, task);
        }
    }

    private void getRecords(int page, TaskJob task) {
        toggle(1);
        this.page = page;
        if (page == 1)
            topicsAdapter.clear();
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, task, id).execute(Integer.toString(page));
        else
            toggle(2);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((ForumActivity) activity);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result) {
        topicsAdapter.supportAddAll(result.getList());
        toggle(0);
        loading = false;
        activity.setTitle(getString(R.string.title_activity_forum));
    }

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
        if (totalItemCount - firstVisibleItem <= visibleItemCount && !loading) {
            loading = true;
            getRecords(page + 1, TaskJob.TOPICS);
            activity.setTitle(getString(R.string.layout_card_loading));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        activity.getPosts(((Forum) topicsAdapter.getItem(position)).getId());
    }
}