package net.somethingdreadful.MAL.forum;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.adapters.ForumMainAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.dialog.ForumChildDialogFragment;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ForumsMain extends Fragment implements ForumNetworkTask.ForumNetworkTaskListener {
    ForumActivity activity;
    View view;
    ForumMain record;
    ForumMainAdapter myanimelistAdapter;
    ForumMainAdapter animemangaAdapter;
    ForumMainAdapter generalAdapter;

    @InjectView(R.id.general) ListView general;
    @InjectView(R.id.scrollView) ObservableScrollView content;
    @InjectView(R.id.animemanga) ListView animeManga;
    @InjectView(R.id.myanimelist) ListView myAnimeList;
    @InjectView(R.id.network_Card) Card networkCard;
    @InjectView(R.id.progressBar) ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.activity_forum_main, container, false);
        ButterKnife.inject(this, view);

        myanimelistAdapter = new ForumMainAdapter(activity, myAnimeList, getFragmentManager(), ForumJob.BOARD);
        animemangaAdapter = new ForumMainAdapter(activity, animeManga, getFragmentManager(), ForumJob.BOARD);
        generalAdapter = new ForumMainAdapter(activity, general, getFragmentManager(), ForumJob.BOARD);

        toggle(1);
        setListener();

        if (bundle != null && bundle.getSerializable("main") != null)
            apply((ForumMain) bundle.getSerializable("main"));
        else
            getRecords();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable("main", record);
        super.onSaveInstanceState(state);
    }

    /**
     * Request new records.
     */
    private void getRecords() {
        if (MALApi.isNetworkAvailable(activity))
            new ForumNetworkTask(activity, this, ForumJob.BOARD, 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        apply(result);
    }

    /**
     * Refresh the UI for changes.
     *
     * @param result The new record that should be applied.
     */
    public void apply(ForumMain result) {
        try {
            myAnimeList.setAdapter(myanimelistAdapter);
            animeManga.setAdapter(animemangaAdapter);
            general.setAdapter(generalAdapter);

            myanimelistAdapter.supportAddAll(result.getMyAnimeList());
            animemangaAdapter.supportAddAll(result.getAnimeManga());
            generalAdapter.supportAddAll(result.getGeneral());
            record = result;
            toggle(0);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "ForumMain.apply(): " + e.getMessage());
        }
    }

    /**
     * Set all the clicklisteners.
     */
    private void setListener() {
        myAnimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                requestTopic((Forum) myanimelistAdapter.getItem(position));
            }
        });
        animeManga.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                requestTopic((Forum) animemangaAdapter.getItem(position));
            }
        });
        general.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                requestTopic((Forum) generalAdapter.getItem(position));
            }
        });
    }

    /**
     * Create a dialog to ask which subBoard you want to view.
     *
     * @param item The forum item with the subBoards
     */
    private void requestTopic(Forum item) {
        if (item.getId() == 0) {
            ForumChildDialogFragment info = new ForumChildDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", item.getName());
            args.putString("message", getString(R.string.dialog_message_forum_child));
            args.putSerializable("child", item.getChildren());
            info.setArguments(args);
            info.show(getFragmentManager(), "fragment_forum");
        } else {
            activity.getTopics(item.getId());
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
}