package net.somethingdreadful.MAL;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.dialog.EpisodesPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import org.apache.commons.lang3.text.WordUtils;

import java.io.Serializable;

public class DetailViewGeneral extends Fragment implements Serializable, OnRatingBarChangeListener, Card.onCardClickListener {

    public SwipeRefreshLayout swipeRefresh;
    Menu menu;
    DetailView activity;

    View view;

    Card cardMain;
    Card cardSynopsis;
    Card cardMediainfo;
    Card cardPersonal;
    Card cardRating;

    TextView synopsis;
    TextView mediaType;
    TextView mediaStatus;
    TextView status;
    TextView progress1Total;
    TextView progress1Current;
    TextView progress2Total;
    TextView progress2Current;
    TextView myScore;
    TextView MALScore;
    RatingBar myScoreBar;
    RatingBar MALScoreBar;
    ImageView image;

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
        cardRating = (Card) view.findViewById(R.id.rating);

        // add all the card contents
        cardMain.setContent(R.layout.card_image);
        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_mediainfo);
        cardPersonal.setContent(R.layout.card_detailview_general_personal);
        cardRating.setContent(R.layout.card_detailview_rating);
        cardPersonal.setPadding(0, 0, 0, -1);
        cardPersonal.setOnClickListener(R.id.status, this);
        cardPersonal.setOnClickListener(R.id.progress1, this);
        cardPersonal.setOnClickListener(R.id.progress2, this);

        // set all the views
        image = (ImageView) view.findViewById(R.id.Image);
        synopsis = (TextView) view.findViewById(R.id.SynopsisContent);
        mediaType = (TextView) view.findViewById(R.id.mediaType);
        mediaStatus = (TextView) view.findViewById(R.id.mediaStatus);
        status = (TextView) view.findViewById(R.id.statusText);
        progress1Total = (TextView) view.findViewById(R.id.progress1Text1);
        progress1Current = (TextView) view.findViewById(R.id.progress1Text2);
        progress2Total = (TextView) view.findViewById(R.id.progress2Text1);
        progress2Current = (TextView) view.findViewById(R.id.progress2Text2);
        myScore = (TextView) view.findViewById(R.id.MyScoreLabel);
        MALScore = (TextView) view.findViewById(R.id.MALScoreLabel);
        myScoreBar = (RatingBar) view.findViewById(R.id.MyScoreBar);
        MALScoreBar = (RatingBar) view.findViewById(R.id.MALScoreBar);
    }

    /*
     * set all the ClickListeners
     */
    public void setListener() {
        myScoreBar.setOnRatingBarChangeListener(this);

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
            if (MALApi.isNetworkAvailable(activity) && menu.findItem(R.id.action_Remove).isVisible()) {
                menu.findItem(R.id.action_Remove).setVisible(true);
            } else {
                menu.findItem(R.id.action_Remove).setVisible(false);
            }
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
                status.setText(WordUtils.capitalize(activity.getUserStatusString(activity.animeRecord.getWatchedStatusInt())));
                cardPersonal.setVisibility(View.VISIBLE);
            } else {
                cardPersonal.setVisibility(View.GONE);
                cardRating.setRightof(cardMediainfo, 2, 720);
            }
            mediaType.setText(activity.getTypeString(activity.animeRecord.getTypeInt()));
            mediaStatus.setText(activity.getStatusString(activity.animeRecord.getStatusInt()));
        } else {
            record = activity.mangaRecord;

            if (activity.mangaRecord.getReadStatus() != null) {
                status.setText(WordUtils.capitalize(activity.getUserStatusString(activity.mangaRecord.getReadStatusInt())));
                cardPersonal.setVisibility(View.VISIBLE);
            } else {
                cardPersonal.setVisibility(View.GONE);
            }
            mediaType.setText(activity.getTypeString(activity.mangaRecord.getTypeInt()));
            mediaStatus.setText(activity.getStatusString(activity.mangaRecord.getStatusInt()));
        }

        if (record.getSynopsis() == null) {
            if (!MALApi.isNetworkAvailable(activity)) {
                synopsis.setText(getString(R.string.toast_error_noConnectivity));
            }
        } else {
            synopsis.setMovementMethod(LinkMovementMethod.getInstance());
            synopsis.setText(record.getSpannedSynopsis());
        }

        if (activity.type.equals(ListType.ANIME)) {
            progress1Current.setText(Integer.toString(activity.animeRecord.getWatchedEpisodes()));
            if (activity.animeRecord.getEpisodes() == 0)
                progress1Total.setText("/?");
            else
                progress1Total.setText("/" + Integer.toString(activity.animeRecord.getEpisodes()));
        } else {
            progress1Current.setText(Integer.toString(activity.mangaRecord.getVolumesRead()));
            if (activity.mangaRecord.getVolumes() == 0)
                progress1Total.setText("/?");
            else
                progress1Total.setText("/" + Integer.toString(activity.mangaRecord.getVolumes()));

            progress2Current.setText(Integer.toString(activity.mangaRecord.getChaptersRead()));

            if (activity.mangaRecord.getChapters() == 0)
                progress2Total.setText("/?");
            else
                progress2Total.setText("/" + Integer.toString(activity.mangaRecord.getChapters()));
        }

        if (!activity.isAdded() && record.getMembersScore() == 0) {
            cardRating.setVisibility(View.GONE);
            cardMediainfo.setWidth(1, 850);
        } else {
            if (record.getMembersScore() == 0) {
                MALScoreBar.setVisibility(View.GONE);
                MALScore.setVisibility(View.GONE);
            } else {
                MALScoreBar.setVisibility(View.VISIBLE);
                MALScore.setVisibility(View.VISIBLE);
                MALScoreBar.setRating(record.getMembersScore() / 2);
            }

            if (activity.isAdded()) {
                myScore.setVisibility(View.VISIBLE);
                myScoreBar.setVisibility(View.VISIBLE);
                myScoreBar.setRating((float) record.getScore() / 2);
            } else {
                myScore.setVisibility(View.GONE);
                myScoreBar.setVisibility(View.GONE);
            }
        }

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
                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.cover_error);
                        cardMain.wrapImage(225, 320);
                        image.setImageBitmap(bitmap.getBitmap());
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
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (activity.type.equals(ListType.ANIME)) {
            if (activity.animeRecord != null) {
                activity.animeRecord.setScore((int) (rating * 2));
                activity.animeRecord.setDirty(true);
            }
        } else {
            if (activity.mangaRecord != null) {
                activity.mangaRecord.setScore((int) (rating * 2));
                activity.mangaRecord.setDirty(true);
            }
        }
    }

    @Override
    public void onCardClickListener(int res) {
        if (res == R.id.status) {
            activity.showDialog("statusPicker", new StatusPickerDialogFragment());
        } else if (res == R.id.progress1 || res == R.id.progress2) {
            if (activity.type.equals(ListType.ANIME)) {
                activity.showDialog("episodes", new EpisodesPickerDialogFragment());
            } else {
                activity.showDialog("manga", new MangaPickerDialogFragment());
            }
        }
    }
}