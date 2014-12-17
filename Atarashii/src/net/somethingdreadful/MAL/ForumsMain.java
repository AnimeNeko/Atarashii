package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import net.somethingdreadful.MAL.adapters.ForumMainAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

public class ForumsMain extends Fragment implements ForumNetworkTaskFinishedListener {
    ForumActivity activity;
    View view;
    ForumMain record;
    ForumMainAdapter myanimelistAdapter;
    ForumMainAdapter animemangaAdapter;
    ForumMainAdapter generalAdapter;
    ProgressBar progressBar;
    ScrollView content;
    Card networkCard;
    ListView myAnimeList;
    ListView animeManga;
    ListView general;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_forum_main, container, false);

        myAnimeList = (ListView) view.findViewById(R.id.myanimelist);
        animeManga = (ListView) view.findViewById(R.id.animemanga);
        general = (ListView) view.findViewById(R.id.general);

        myanimelistAdapter = new ForumMainAdapter(activity, myAnimeList, getFragmentManager(), TaskJob.BOARD);
        animemangaAdapter = new ForumMainAdapter(activity, animeManga, getFragmentManager(), TaskJob.BOARD);
        generalAdapter = new ForumMainAdapter(activity, general, getFragmentManager(), TaskJob.BOARD);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        networkCard = (Card) view.findViewById(R.id.network_Card);
        content = (ScrollView) view.findViewById(R.id.content);

        toggle(1);
        setListener();
        getRecords();
        return view;
    }

    private void getRecords() {
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, TaskJob.BOARD, 0).execute();
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
        myAnimeList.setAdapter(myanimelistAdapter);
        animeManga.setAdapter(animemangaAdapter);
        general.setAdapter(generalAdapter);

        myanimelistAdapter.supportAddAll(result.getMyAnimeList());
        animemangaAdapter.supportAddAll(result.getAnimeManga());
        generalAdapter.supportAddAll(result.getGeneral());
        record = result;
        toggle(0);
    }

    private void setListener() {
        myAnimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.getTopics(((Forum) myanimelistAdapter.getItem(position)).getId());
            }
        });
        animeManga.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.getTopics(((Forum) animemangaAdapter.getItem(position)).getId());
            }
        });
        general.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.getTopics(((Forum) generalAdapter.getItem(position)).getId());
            }
        });
    }

    private void toggle(int number) {
        content.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }
}