package net.somethingdreadful.MAL;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.dialog.EpisodesPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import org.apache.commons.lang3.text.WordUtils;
import org.holoeverywhere.widget.TextView;

import java.io.Serializable;

public class DetailViewGeneral extends Fragment implements Serializable, OnRatingBarChangeListener, Card.onCardClickListener, SwipeRefreshLayout.OnRefreshListener {

    public SwipeRefreshLayout swipeRefresh;
    Menu menu;
    DetailView activity;

    View view;

    Card cardMain;
    Card cardSynopsis;
    Card cardMediainfo;
    Card cardStatus;
    Card cardProgress;
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
        setText();

        NfcHelper.disableBeam(activity);

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
        cardStatus = (Card) view.findViewById(R.id.status);
        cardProgress = (Card) view.findViewById(R.id.progress);
        cardRating = (Card) view.findViewById(R.id.rating);

        // add all the card contents
        cardMain.setContent(R.layout.card_detailview_image);
        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_mediainfo);
        cardStatus.setContent(R.layout.card_detailview_status);
        cardProgress.setContent(R.layout.card_detailview_progress);
        cardRating.setContent(R.layout.card_detailview_rating);

        // set all the views
        image = (ImageView) view.findViewById(R.id.Image);
        synopsis = (TextView) view.findViewById(R.id.SynopsisContent);
        mediaType = (TextView) view.findViewById(R.id.mediaType);
        mediaStatus = (TextView) view.findViewById(R.id.mediaStatus);
        status = (TextView) view.findViewById(R.id.cardStatusLabel);
        progress1Total = (TextView) view.findViewById(R.id.progresslabel1Total);
        progress1Current = (TextView) view.findViewById(R.id.progresslabel1Current);
        progress1Total = (TextView) view.findViewById(R.id.progresslabel1Total);
        progress2Total = (TextView) view.findViewById(R.id.progresslabel2Total);
        progress2Current = (TextView) view.findViewById(R.id.progresslabel2Current);
        progress1Current = (TextView) view.findViewById(R.id.progresslabel1Current);
        myScore = (TextView) view.findViewById(R.id.MyScoreLabel);
        MALScore = (TextView) view.findViewById(R.id.MALScoreLabel);
        myScoreBar = (RatingBar) view.findViewById(R.id.MyScoreBar);
        MALScoreBar = (RatingBar) view.findViewById(R.id.MALScoreBar);
    }

    @Override
    public void onRefresh() {
        activity.getRecord(true);
    }

    /*
     * set all the ClickListeners
     */
    public void setListener() {
        myScoreBar.setOnRatingBarChangeListener(this);
        cardStatus.setCardClickListener(this);
        cardProgress.setCardClickListener(this);

        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    /*
     * Manage the progress card
     */
    public void setCard() {
        if (activity.type != null && activity.type.equals(ListType.ANIME)) {
            TextView progress1 = (TextView) view.findViewById(R.id.progresslabel1);
            progress1.setText(getString(R.string.card_content_episodes));
            TextView progress2 = (TextView) view.findViewById(R.id.progresslabel2);
            progress2.setVisibility(View.GONE);
            progress2Current.setVisibility(View.GONE);
            progress2Total.setVisibility(View.GONE);
        }
    }

    /*
     * set the right menu items.
     */
    public void setMenu() {
        if (menu != null) {
            if (isAdded()) {
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
     * Get the translation from strings.xml
     */
    private String getStringFromResourceArray(int resArrayId, int notFoundStringId, int index) {
        Resources res = getResources();
        try {
            String[] types = res.getStringArray(resArrayId);
            if (index < 0 || index >= types.length) // make sure to have a valid array index
                return res.getString(notFoundStringId);
            else
                return types[index];
        } catch (Resources.NotFoundException e) {
            return res.getString(notFoundStringId);
        }
    }

    private String getAnimeTypeString(int typesInt) {
        return getStringFromResourceArray(R.array.mediaType_Anime, R.string.unknown, typesInt);
    }

    private String getAnimeStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_Anime, R.string.unknown, statusInt);
    }

    private String getMangaTypeString(int typesInt) {
        return getStringFromResourceArray(R.array.mediaType_Manga, R.string.unknown, typesInt);
    }

    private String getMangaStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_Manga, R.string.unknown, statusInt);
    }

    private String getUserStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_User, R.string.unknown, statusInt);
    }

    /*
     * Place all the text in the right textview
     */
    public void setText() {
        try {
            if (activity.type == null || (activity.animeRecord == null && activity.mangaRecord == null)) // not enough data to do anything
                return;
            GenericRecord record;
            setMenu();
            if (activity.type.equals(ListType.ANIME)) {
                record = activity.animeRecord;
                if (activity.animeRecord.getWatchedStatus() != null) {
                    status.setText(WordUtils.capitalize(getUserStatusString(activity.animeRecord.getWatchedStatusInt())));
                    cardStatus.setVisibility(View.VISIBLE);
                } else {
                    cardStatus.setVisibility(View.GONE);
                }
                mediaType.setText(getAnimeTypeString(activity.animeRecord.getTypeInt()));
                mediaStatus.setText(getAnimeStatusString(activity.animeRecord.getStatusInt()));
            } else {
                record = activity.mangaRecord;

                if (activity.mangaRecord.getReadStatus() != null) {
                    status.setText(WordUtils.capitalize(getUserStatusString(activity.mangaRecord.getReadStatusInt())));
                    cardStatus.setVisibility(View.VISIBLE);
                } else {
                    cardStatus.setVisibility(View.GONE);
                }
                mediaType.setText(getMangaTypeString(activity.mangaRecord.getTypeInt()));
                mediaStatus.setText(getMangaStatusString(activity.mangaRecord.getStatusInt()));
            }

            if (record.getSynopsis() == null) {
                if (MALApi.isNetworkAvailable(activity)) {
                    Bundle data = new Bundle();
                    data.putSerializable("record", activity.type.equals(ListType.ANIME) ? activity.animeRecord : activity.mangaRecord);
                    activity.setRefreshing(true);
                    new NetworkTask(TaskJob.GETDETAILS, activity.type, activity, data, activity, activity).execute();
                } else {
                    synopsis.setText(getString(R.string.crouton_error_noConnectivity));
                }
            } else {
                synopsis.setMovementMethod(LinkMovementMethod.getInstance());
                synopsis.setText(Html.fromHtml(record.getSynopsis()));
            }

            if (isAdded()) {
                cardProgress.setVisibility(View.VISIBLE);
            } else {
                cardProgress.setVisibility(View.GONE);
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

            if (record.getMembersScore() == 0) {
                MALScoreBar.setVisibility(View.GONE);
                MALScore.setVisibility(View.GONE);
            } else {
                MALScoreBar.setVisibility(View.VISIBLE);
                MALScore.setVisibility(View.VISIBLE);
                MALScoreBar.setRating(record.getMembersScore() / 2);
            }

            if (isAdded()) {
                myScore.setVisibility(View.VISIBLE);
                myScoreBar.setVisibility(View.VISIBLE);
                myScoreBar.setRating((float) record.getScore() / 2);
            } else {
                myScore.setVisibility(View.GONE);
                myScoreBar.setVisibility(View.GONE);
            }
            image.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            Picasso.with(activity)
                    .load(record.getImageUrl())
                    .error(R.drawable.cover_error)
                    .placeholder(R.drawable.cover_loading)
                    .centerInside()
                    .fit()
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            cardMain.wrapWidth(false);
                        }

                        @Override
                        public void onError() {
                        }
                    });
            cardMain.Header.setText(record.getTitle());

            setCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (activity.type.equals(ListType.ANIME)) {
            if (activity.animeRecord != null) {
                activity.animeRecord.setScore((int) (rating * 2));
                activity.animeRecord.setDirty(true);
            }
        } else {
            if (activity.animeRecord != null) {
                activity.mangaRecord.setScore((int) (rating * 2));
                activity.mangaRecord.setDirty(true);
            }
        }
    }

    @Override
    public void onCardClickListener(int res) {
        if (res == R.id.status) {
            activity.showDialog("statusPicker", new StatusPickerDialogFragment());
        } else if (res == R.id.progress) {
            if (activity.type.equals(ListType.ANIME)) {
                activity.showDialog("episodes", new EpisodesPickerDialogFragment());
            } else {
                activity.showDialog("manga", new MangaPickerDialogFragment());
            }
        }
    }
}