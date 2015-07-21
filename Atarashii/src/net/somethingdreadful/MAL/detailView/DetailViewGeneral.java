package net.somethingdreadful.MAL.detailView;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailViewGeneral extends Fragment implements Serializable, Card.onCardClickListener {
    View view;
    Menu menu;
    DetailView activity;
    public SwipeRefreshLayout swipeRefresh;

    Card cardMain;
    Card cardSynopsis;
    Card cardMediainfo;
    Card cardPersonal;

    @InjectView(R.id.SynopsisContent) TextView synopsis;
    @InjectView(R.id.mediaType) TextView mediaType;
    @InjectView(R.id.mediaStatus) TextView mediaStatus;
    @InjectView(R.id.statusText) TextView status;
    @InjectView(R.id.progress1Text1) TextView progress1Total;
    @InjectView(R.id.progress1Text2) TextView progress1Current;
    @InjectView(R.id.progress2Text1) TextView progress2Total;
    @InjectView(R.id.progress2Text2) TextView progress2Current;
    @InjectView(R.id.myScore) TextView myScore;
    @InjectView(R.id.Image) ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_general, container, false);

        activity = ((DetailView) getActivity());
        setViews();
        setListener();

        activity.setGeneral(this);
        return view;
    }

    /*
     * Set all views once
     */
    public void setViews() {
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        // set all the card views
        cardMain = (Card) view.findViewById(R.id.detailCoverImage);
        cardSynopsis = (Card) view.findViewById(R.id.synopsis);
        cardMediainfo = (Card) view.findViewById(R.id.mediainfo);
        cardPersonal = (Card) view.findViewById(R.id.personal);

        // add all the card contents
        cardMain.setContent(R.layout.card_image);
        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_mediainfo);
        cardPersonal.setContent(R.layout.card_detailview_general_personal);

        ButterKnife.inject(this, view);

        cardPersonal.setAllPadding(0, 0, 0, 0);
        cardPersonal.setOnClickListener(R.id.status, this);
        cardPersonal.setOnClickListener(R.id.progress1, this);
        cardPersonal.setOnClickListener(R.id.progress2, this);
        cardPersonal.setOnClickListener(R.id.scorePanel, this);
    }

    /*
     * set all the ClickListeners
     */
    public void setListener() {
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    /*
     * Manage the progress card
     */
    public void setCard() {
        if (activity.type != null && activity.type.equals(ListType.ANIME)) {
            TextView progress1 = (TextView) view.findViewById(R.id.progress1Label);
            progress1.setText(getString(R.string.card_content_episodes));
            RelativeLayout progress2 = (RelativeLayout) view.findViewById(R.id.progress2);
            progress2.setVisibility(View.GONE);
        }
    }

    /*
     * set the right menu items.
     */
    public void setMenu() {
        if (menu != null) {
            if (activity.isAdded()) {
                menu.findItem(R.id.action_Remove).setVisible(true);
                menu.findItem(R.id.action_addToList).setVisible(false);
            } else {
                menu.findItem(R.id.action_Remove).setVisible(false);
                menu.findItem(R.id.action_addToList).setVisible(true);
            }
            menu.findItem(R.id.action_Remove).setVisible(MALApi.isNetworkAvailable(activity) && menu.findItem(R.id.action_Remove).isVisible());
        }
    }

    /*
     * Place all the text in the right textview
     */
    public void setText() {
        if (activity.type == null || (activity.animeRecord == null && activity.mangaRecord == null)) // not enough data to do anything
            return;
        GenericRecord record;
        setMenu();
        if (activity.type.equals(ListType.ANIME)) {
            record = activity.animeRecord;

            if (activity.isAdded()) {
                status.setText(activity.getUserStatusString(activity.animeRecord.getWatchedStatusInt()));
                cardPersonal.setVisibility(View.VISIBLE);
            } else {
                cardPersonal.setVisibility(View.GONE);
            }
            mediaType.setText(activity.getTypeString(activity.animeRecord.getTypeInt()));
            mediaStatus.setText(activity.getStatusString(activity.animeRecord.getStatusInt()));
        } else {
            record = activity.mangaRecord;

            if (activity.mangaRecord.getReadStatus() != null) {
                status.setText(activity.getUserStatusString(activity.mangaRecord.getReadStatusInt()));
                cardPersonal.setVisibility(View.VISIBLE);
            } else {
                cardPersonal.setVisibility(View.GONE);
            }
            mediaType.setText(activity.getTypeString(activity.mangaRecord.getTypeInt()));
            mediaStatus.setText(activity.getStatusString(activity.mangaRecord.getStatusInt()));
        }
        activity.hidePersonal(!activity.isAdded() || record.getSynopsis() == null);

        if (record.getSynopsis() == null) {
            if (!MALApi.isNetworkAvailable(activity))
                synopsis.setText(getString(R.string.toast_error_noConnectivity));
        } else {
            synopsis.setMovementMethod(LinkMovementMethod.getInstance());
            synopsis.setText(record.getSpannedSynopsis());
        }

        if (activity.type.equals(ListType.ANIME)) {
            progress1Current.setText(Integer.toString(activity.animeRecord.getWatchedEpisodes()));
            progress1Total.setText(activity.animeRecord.getEpisodes() == 0 ? "/?" : "/" + Integer.toString(activity.animeRecord.getEpisodes()));
            myScore.setText(activity.nullCheck(Theme.getDisplayScore(activity.animeRecord.getScore())));
        } else {
            progress1Current.setText(Integer.toString(activity.mangaRecord.getVolumesRead()));
            progress1Total.setText(activity.mangaRecord.getVolumes() == 0 ? "/?" : "/" + Integer.toString(activity.mangaRecord.getVolumes()));
            progress2Current.setText(Integer.toString(activity.mangaRecord.getChaptersRead()));
            progress2Total.setText(activity.mangaRecord.getChapters() == 0 ? "/?" : "/" + Integer.toString(activity.mangaRecord.getChapters()));
            myScore.setText(activity.nullCheck(Theme.getDisplayScore(activity.mangaRecord.getScore())));
        }

        if (!activity.isAdded() && record.getMembersScore() == 0)
            cardMediainfo.setWidth(1, 850);

        Picasso.with(activity)
                .load(record.getImageUrl())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        cardMain.wrapImage(bitmap.getWidth(), bitmap.getHeight());
                        image.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        try {
                            BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.cover_error);
                            cardMain.wrapImage(225, 320);
                            image.setImageBitmap(bitmap.getBitmap());
                        } catch (Exception e) {
                            Crashlytics.log(Log.ERROR, "MALX", "DetailViewGeneral.setText(): " + e.getMessage());
                        }
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.cover_loading);
                        cardMain.wrapImage(225, 320);
                        image.setImageBitmap(bitmap.getBitmap());
                    }
                });
        cardMain.Header.setText(record.getTitle());

        setCard();
    }

    @Override
    public void onCardClickListener(int res) {
        switch (res) {
            case R.id.status:
                activity.showDialog("statusPicker", new StatusPickerDialogFragment());
                break;
            case R.id.progress1:
            case R.id.progress2:
                if (activity.type.equals(ListType.ANIME)) {
                    Bundle args = new Bundle();
                    args.putInt("id", R.id.progress1);
                    args.putInt("current", activity.animeRecord.getWatchedEpisodes());
                    args.putInt("max", activity.animeRecord.getEpisodes());
                    args.putString("title", getString(R.string.dialog_title_watched_update));
                    activity.showDialog("episodes", new NumberPickerDialogFragment().setOnSendClickListener(activity), args);
                } else {
                    activity.showDialog("manga", new MangaPickerDialogFragment());
                }
                break;
            case R.id.scorePanel:
                Bundle bundle = new Bundle();
                bundle.putInt("id", R.id.scorePanel);
                bundle.putString("title", getString(R.string.dialog_title_rating));
                bundle.putInt("current", activity.isAnime() ? activity.animeRecord.getScore() : activity.mangaRecord.getScore());
                bundle.putInt("max", PrefManager.getScoreType() == 3 ? 5 : 10);
                activity.showDialog("rating", new NumberPickerDialogFragment().setOnSendClickListener(activity), bundle);
        }
    }
}