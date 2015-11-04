package net.somethingdreadful.MAL.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.forum.HtmlUtil;

import butterknife.ButterKnife;
import butterknife.Bind;

public class ProfileHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private HtmlUtil htmlUtil;
    public ProfileActivity activity;

    @Bind(R.id.webview) WebView webview;
    @Bind(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_profile_history, container, false);
        ButterKnife.bind(this, view);

        htmlUtil = new HtmlUtil(activity);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new ProfileHistoryInterface(this), "Posts");

        activity.setHistory(this);
        if (activity.record != null)
            refresh();
        else
            activity.getRecords();

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        NfcHelper.disableBeam(activity);
        return view;
    }

    /**
     * Refresh the UI for changes.
     *
     * @param result The new record
     */
    public void apply(Profile result) {
        try {
            if (result != null) {
                webview.loadDataWithBaseURL(null, htmlUtil.convertList(result, 1), "text/html", "utf-8", null);
            } else {
                Theme.Snackbar(activity, R.string.toast_error_Records);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "ProfileHistory.apply(): " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
    }

    public void refresh() {
        apply(activity.record);
    }

    @Override
    public void onRefresh() {
        activity.getRecords();
    }
}
