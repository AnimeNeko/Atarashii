package net.somethingdreadful.MAL.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.response.UserProfile.User;
import net.somethingdreadful.MAL.forum.HtmlUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileHistory extends Fragment {
    private HtmlUtil htmlUtil;
    private ProfileActivity activity;

    @InjectView(R.id.webview) WebView webview;
    @InjectView(R.id.viewFlipper) ViewFlipper viewFlipper;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_forum_posts, container, false);
        ButterKnife.inject(this, view);

        htmlUtil = new HtmlUtil(activity);
        webview.getSettings().setJavaScriptEnabled(true);

        activity.setHistory(this);
        if (activity.record != null)
            refresh();

        NfcHelper.disableBeam(activity);
        return view;
    }

    /**
     * Refresh the UI for changes.
     *
     * @param result The new record
     */
    public void apply(User result) {
        try {
            if (result != null) {
                webview.loadDataWithBaseURL(null, htmlUtil.convertList(result, 1), "text/html", "utf-8", null);
                toggle(false);
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

    /**
     * Show or hide the progress indicator.
     *
     * @param loading True if the indicator should be shown
     */
    public void toggle(boolean loading) {
        viewFlipper.setDisplayedChild(loading ? 1 : 0);
    }

    public void refresh() {
        apply(activity.record);
    }
}
