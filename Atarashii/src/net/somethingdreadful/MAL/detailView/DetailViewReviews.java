package net.somethingdreadful.MAL.detailView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.forum.HtmlUtil;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailViewReviews extends Fragment implements NetworkTask.NetworkTaskListener {
    View view;
    HtmlUtil htmlUtil;
    DetailView activity;
    public ArrayList<Reviews> record;

    @Bind(R.id.webview)
    WebView webview;
    @Bind(R.id.viewFlipper) ViewFlipper viewFlipper;

    public int id;
    public int page = 0;

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreate(bundle);
        view = inflater.inflate(R.layout.fragment_forum_posts, container, false);
        ButterKnife.bind(this, view);

        htmlUtil = new HtmlUtil(activity);

        if (bundle != null) {
            page = bundle.getInt("page");
            apply((ArrayList<Reviews>) bundle.getSerializable("record"));
        } else if (page == 0 && !activity.isEmpty()) {
            getRecords(1);
        }

        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new ReviewsInterface(this), "Posts");
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("page", page);
        state.putSerializable("record", record);
        super.onSaveInstanceState(state);
    }

    /**
     * Get the requested records.
     *
     * @param page The page number
     */
    public void getRecords(int page) {
        if (page != this.page)
            toggle(true);
        this.page = page;
        if (MALApi.isNetworkAvailable(activity)) {
            Bundle bundle = new Bundle();
            bundle.putInt("page", page);
            int id = activity.isAnime() ? activity.animeRecord.getId() : activity.mangaRecord.getId();
            new NetworkTask(TaskJob.REVIEWS, activity.type, activity, bundle, activity.reviews, activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id));
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
        }
    }

    /**
     * Show or hide the progress indicator.
     *
     * @param loading True if the indicator should be shown
     */
    private void toggle(boolean loading) {
        viewFlipper.setDisplayedChild(loading ? 1 : 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((DetailView) activity);
        this.activity.setReviews(this);
    }

    /**
     * Refresh the UI for changes.
     *
     * @param result The new record
     */
    public void apply(ArrayList<Reviews> result) {
        try {
            activity.setTitle(getString(R.string.title_activity_forum));
            if (result != null) {
                webview.loadDataWithBaseURL(null, htmlUtil.convertList(result, page), "text/html", "utf-8", null);
                toggle(false);
                record = result;
            } else {
                Theme.Snackbar(activity, R.string.toast_error_reviews);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailViewReviews.apply(): " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled) {
        apply((ArrayList<Reviews>) result);
    }

    @Override
    public void onNetworkTaskError(TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled) {
    }
}