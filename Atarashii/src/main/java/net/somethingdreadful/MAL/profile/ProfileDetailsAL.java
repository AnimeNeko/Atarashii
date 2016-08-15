package net.somethingdreadful.MAL.profile;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.forum.ForumHTMLUnit;

import org.apache.commons.lang3.text.WordUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileDetailsAL extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private ProfileActivity activity;
    private ForumHTMLUnit forumHTMLUnit;
    private Card imagecard;
    @BindView(R.id.swiperefresh)
    public SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.network_Card)
    Card networkCard;
    @BindView(R.id.SynopsisContent)
    TextView about;
    @BindView(R.id.atimedayssmall)
    TextView timeDays;
    @BindView(R.id.mtimedayssmall)
    TextView chapsRead;
    @BindView(R.id.Image)
    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_profile_al, container, false);

        Card.fastInit(view, R.id.about, R.layout.card_profile_about);
        imagecard = (Card) view.findViewById(R.id.name_card);
        imagecard.setContent(R.layout.card_image);
        forumHTMLUnit = new ForumHTMLUnit(activity, null);

        ButterKnife.bind(this, view);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        activity.setDetails(this);

        if (activity.record == null)
            toggle(1);
        else
            refresh();
        NfcHelper.disableBeam(activity);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
    }

    public void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void refresh() {
        try {
            if (activity.record == null) {
                if (APIHelper.isNetworkAvailable(activity))
                    Theme.Snackbar(activity, R.string.toast_error_UserRecord);
                else
                    toggle(2);

            } else {
                Profile profile = activity.record;
                Card namecard = (Card) view.findViewById(R.id.name_card);
                namecard.Header.setText(WordUtils.capitalize(profile.getUsername()));
                if (profile.getAbout() == null || profile.getAbout().equals("")) {
                    about.setText(getString(R.string.unknown));
                } else {
                    about.setText(Html.fromHtml(forumHTMLUnit.convertComment(profile.getAbout())));
                    about.setMovementMethod(LinkMovementMethod.getInstance());
                }
                timeDays.setText(String.valueOf(profile.getAnimeStats().getTimeDays()));
                chapsRead.setText(String.valueOf(profile.getMangaStats().getCompleted()));

                Picasso.with(activity)
                        .load(activity.record.getImageUrl())
                        .error(R.drawable.cover_error)
                        .placeholder(R.drawable.cover_loading)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                imagecard.wrapImage(bitmap.getWidth(), bitmap.getHeight());
                                image.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                try {
                                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.cover_error);
                                    imagecard.wrapImage(225, 320);
                                    image.setImageDrawable(drawable);
                                } catch (Exception e) {
                                    Theme.log(Log.ERROR, "Atarashii", "ProfileDetailsMAL.refresh(): " + e.getMessage());
                                }
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.cover_loading);
                                imagecard.wrapImage(225, 320);
                                image.setImageDrawable(drawable);
                            }
                        });
                toggle(0);
            }
        } catch (IllegalStateException e) {
            Theme.log(Log.ERROR, "Atarashii", "ProfileDetailsMAL.refresh(): has been closed too fast");
        }
    }

    @Override
    public void onRefresh() {
        activity.getRecords();
    }
}
