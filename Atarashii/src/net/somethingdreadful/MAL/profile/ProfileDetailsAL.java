package net.somethingdreadful.MAL.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.forum.HtmlUtil;

import org.apache.commons.lang3.text.WordUtils;

public class ProfileDetailsAL extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    Context context;
    View view;
    Card imagecard;
    Card activitycard;
    public SwipeRefreshLayout swipeRefresh;
    ProgressBar progressBar;
    Card networkCard;
    WebView webview;

    private ProfileActivity activity;
    private HtmlUtil htmlUtil;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_profile_al, container, false);

        imagecard = ((Card) view.findViewById(R.id.name_card));
        imagecard.setContent(R.layout.card_image);
        activitycard = ((Card) view.findViewById(R.id.activity));
        activitycard.setContent(R.layout.card_profile_webview);
        activitycard.setPadding(0);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        networkCard = (Card) view.findViewById(R.id.network_Card);
        webview = (WebView) view.findViewById(R.id.webview);

        htmlUtil = new HtmlUtil(activity);
        webview.getSettings().setJavaScriptEnabled(true);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        activity.setDetails(this);

        if (activity.record == null)
            toggle(1);

        NfcHelper.disableBeam(activity);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
    }

    public void card() {
        Card namecard = (Card) view.findViewById(R.id.name_card);
        namecard.Header.setText(WordUtils.capitalize(activity.record.getName()));
    }

    public void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void refresh() {
        if (activity.record == null) {
            if (MALApi.isNetworkAvailable(context)) {
                Toast.makeText(context, R.string.toast_error_UserRecord, Toast.LENGTH_SHORT).show();
            } else {
                toggle(2);
            }
        } else {
            card();

            webview.loadDataWithBaseURL(null, htmlUtil.convertList(activity.record, activity, 1), "text/html", "utf-8", null);

            Picasso.with(context)
                    .load(activity.record.getProfile()
                            .getAvatarUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            imagecard.wrapImage(bitmap.getWidth(), bitmap.getHeight());
                            ((ImageView) view.findViewById(R.id.Image)).setImageBitmap(bitmap);
                            toggle(0);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            toggle(0);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            toggle(0);
                        }
                    });
            toggle(0);
        }
    }

    @Override
    public void onRefresh() {
        activity.getRecords();
    }
}
