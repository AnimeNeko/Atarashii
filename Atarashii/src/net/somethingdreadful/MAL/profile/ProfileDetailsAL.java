package net.somethingdreadful.MAL.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.APIHelper;

import org.apache.commons.lang3.text.WordUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileDetailsAL extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private ProfileActivity activity;
    private Card imagecard;
    private Card activitycard;
    @Bind(R.id.swiperefresh)
    public SwipeRefreshLayout swipeRefresh;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.network_Card)
    Card networkCard;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_profile_al, container, false);

        imagecard = (Card) view.findViewById(R.id.name_card);
        imagecard.setContent(R.layout.card_image);
        activitycard = (Card) view.findViewById(R.id.activity);
        activitycard.setPadding(0);

        ButterKnife.bind(this, view);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
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

    private void card() {
        Card namecard = (Card) view.findViewById(R.id.name_card);
        namecard.Header.setText(WordUtils.capitalize(activity.record.getUsername()));
    }

    public void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void refresh() {
        if (activity.record == null) {
            if (APIHelper.isNetworkAvailable(activity))
                Theme.Snackbar(activity, R.string.toast_error_UserRecord);
            else
                toggle(2);

        } else {
            card();

            Picasso.with(activity)
                    .load(activity.record.getImageUrl())
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
