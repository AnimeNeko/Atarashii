package net.somethingdreadful.MAL.detailView;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
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
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import java.io.Serializable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailViewGeneral extends Fragment implements Serializable, Card.onCardClickListener {
    private View view;
    private DetailView activity;
    public SwipeRefreshLayout swipeRefresh;

    private Card cardMain;
    private Card cardSynopsis;
    private Card cardMediainfo;
    private Card cardPersonal;

    @Bind(R.id.SynopsisContent)
    TextView synopsis;
    @Bind(R.id.mediaType)
    TextView mediaType;
    @Bind(R.id.mediaStatus)
    TextView mediaStatus;
    @Bind(R.id.statusText)
    TextView status;
    @Bind(R.id.progress1Text1)
    TextView progress1Total;
    @Bind(R.id.progress1Text2)
    TextView progress1Current;
    @Bind(R.id.progress2Text1)
    TextView progress2Total;
    @Bind(R.id.progress2Text2)
    TextView progress2Current;
    @Bind(R.id.myScore)
    TextView myScore;
    @Bind(R.id.Image)
    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_general, container, false);

        activity = ((DetailView) getActivity());
        setViews();
        setListener();

        activity.setGeneral(this);

        if (activity.isDone())
            setText();
        return view;
    }

    /**
     * Set all views once
     */
    private void setViews() {
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

        ButterKnife.bind(this, view);

        cardPersonal.setAllPadding(0, 0, 0, 0);
        cardPersonal.setOnClickListener(R.id.status, this);
        cardPersonal.setOnClickListener(R.id.progress1, this);
        cardPersonal.setOnClickListener(R.id.progress2, this);
        cardPersonal.setOnClickListener(R.id.scorePanel, this);
    }

    /**
     * set all the ClickListeners
     */
    private void setListener() {
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    /**
     * Manage the progress card
     */
    private void setCard() {
        if (activity.type != null && activity.type.equals(ListType.ANIME)) {
            TextView progress1 = (TextView) view.findViewById(R.id.progress1Label);
            progress1.setText(getString(R.string.card_content_episodes));
            RelativeLayout progress2 = (RelativeLayout) view.findViewById(R.id.progress2);
            progress2.setVisibility(View.GONE);
        }
    }

    /**
     * Place all the text in the right textview
     */
    public void setText() {
        if (activity.type == null || (activity.animeRecord == null && activity.mangaRecord == null)) // not enough data to do anything
            return;
        GenericRecord record;
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

        if (record.getSynopsis() == null) {
            if (!APIHelper.isNetworkAvailable(activity))
                synopsis.setText(getString(R.string.toast_error_noConnectivity));
        } else {
            synopsis.setMovementMethod(LinkMovementMethod.getInstance());
            synopsis.setText(record.getSynopsis());
        }

        if (activity.type.equals(ListType.ANIME)) {
            progress1Current.setText(String.valueOf(activity.animeRecord.getWatchedEpisodes()));
            progress1Total.setText(activity.animeRecord.getEpisodes() == 0 ? "/?" : "/" + String.valueOf(activity.animeRecord.getEpisodes()));
            myScore.setText(activity.nullCheck(Theme.getDisplayScore(activity.animeRecord.getScore())));
        } else {
            progress1Current.setText(String.valueOf(activity.mangaRecord.getVolumesRead()));
            progress1Total.setText(activity.mangaRecord.getVolumes() == 0 ? "/?" : "/" + String.valueOf(activity.mangaRecord.getVolumes()));
            progress2Current.setText(String.valueOf(activity.mangaRecord.getChaptersRead()));
            progress2Total.setText(activity.mangaRecord.getChapters() == 0 ? "/?" : "/" + String.valueOf(activity.mangaRecord.getChapters()));
            myScore.setText(activity.nullCheck(Theme.getDisplayScore(activity.mangaRecord.getScore())));
        }

        if (!activity.isAdded() && record.getAverageScore() == null)
            cardMediainfo.setWidth(1, 850);

        Picasso.with(activity)
                .load(record.getImageUrl())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if (AccountService.isMAL() && (bitmap.getHeight() > 320 || bitmap.getWidth() > 230))
                            cardMain.wrapImage((int) (bitmap.getWidth() / 1.4), (int) (bitmap.getHeight() / 1.4));
                        else
                            cardMain.wrapImage(bitmap.getWidth(), bitmap.getHeight());
                        image.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        try {
                            Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.cover_error);
                            cardMain.wrapImage(225, 320);
                            image.setImageDrawable(drawable);
                        } catch (Exception e) {
                            Crashlytics.log(Log.ERROR, "MALX", "DetailViewGeneral.setText(): " + e.getMessage());
                        }
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.cover_loading);
                        cardMain.wrapImage(225, 320);
                        image.setImageDrawable(drawable);
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
                bundle.putInt("max", PrefManager.getMaxScore());
                activity.showDialog("rating", new NumberPickerDialogFragment().setOnSendClickListener(activity), bundle);
                break;
        }
    }
}