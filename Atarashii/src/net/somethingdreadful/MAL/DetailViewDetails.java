package net.somethingdreadful.MAL;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.GenericRecord;

import org.holoeverywhere.widget.TextView;

import java.io.Serializable;

public class DetailViewDetails extends Fragment implements Serializable, SwipeRefreshLayout.OnRefreshListener {

    public SwipeRefreshLayout swipeRefresh;
    Menu menu;
    DetailView activity;

    View view;

    Card cardSynopsis;
    Card cardMediainfo;
    Card cardMediaStats;

    TextView synopsis;
    TextView type;
    TextView episodes;
    TextView episodesLabel;
    TextView volumes;
    TextView volumesLabel;
    TextView status;
    TextView genres;
    TextView classification;
    TextView classificationLabel;

    TextView score;
    TextView ranked;
    TextView popularity;
    TextView members;
    TextView favorites;

    TextView adaptations;
    TextView prequels;
    TextView sequels;
    TextView side_stories;
    TextView parent_story;
    TextView spin_offs;
    TextView summaries;
    TextView alternative_versions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_details, container, false);

        setViews();
        setListener();

        NfcHelper.disableBeam(activity);
        activity.setDetails(this);

        if (activity.isDone())
            setText();
        else
            activity.setRefreshing(true);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((DetailView) activity);
    }

    /*
         * Set all views once
         */
    public void setViews() {
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        // set all the card views
        cardSynopsis = (Card) view.findViewById(R.id.synopsis);
        cardMediainfo = (Card) view.findViewById(R.id.mediainfo);
        cardMediaStats = (Card) view.findViewById(R.id.mediastats);

        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_details_mediainfo);
        cardMediaStats.setContent(R.layout.card_detailview_details_mediastats);

        // set all the views
        synopsis = (TextView) view.findViewById(R.id.SynopsisContent);

        type = (TextView) view.findViewById(R.id.type);
        episodes = (TextView) view.findViewById(R.id.episodes);
        episodesLabel = (TextView) view.findViewById(R.id.episodesLabel);
        volumes = (TextView) view.findViewById(R.id.volumes);
        volumesLabel = (TextView) view.findViewById(R.id.volumesLabel);
        status = (TextView) view.findViewById(R.id.status);
        genres = (TextView) view.findViewById(R.id.genres);
        classification = (TextView) view.findViewById(R.id.classification);
        classificationLabel = (TextView) view.findViewById(R.id.classificationLabel);

        score = (TextView) view.findViewById(R.id.score);
        ranked = (TextView) view.findViewById(R.id.ranked);
        popularity = (TextView) view.findViewById(R.id.popularity);
        members = (TextView) view.findViewById(R.id.members);
        favorites = (TextView) view.findViewById(R.id.favorites);
    }

    @Override
    public void onRefresh() {
        activity.getRecord(true);
    }

    /*
     * set all the ClickListeners
     */
    public void setListener() {
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    /*
     * Place all the text in the right textview
     */
    public void setText() {
        if (activity.type == null || (activity.animeRecord == null && activity.mangaRecord == null)) // not enough data to do anything
            return;
        GenericRecord record;
        record = (activity.type.equals(MALApi.ListType.ANIME) ? activity.animeRecord : activity.mangaRecord);
        activity.setMenu();

        synopsis.setText(Html.fromHtml(record.getSynopsis()));
        synopsis.setMovementMethod(LinkMovementMethod.getInstance());
        genres.setText("\u200F" + TextUtils.join(", ", activity.getGenresString(record.getGenresInt())));
        if (activity.type.equals(MALApi.ListType.ANIME)) {
            type.setText(activity.getTypeString(activity.animeRecord.getTypeInt()));
            episodes.setText(activity.animeRecord.getEpisodes() == 0 ? "?" : Integer.toString(activity.animeRecord.getEpisodes()));
            volumes.setVisibility(View.GONE);
            volumesLabel.setVisibility(View.GONE);
            status.setText(activity.getStatusString(activity.animeRecord.getStatusInt()));
            classification.setText(activity.getClassificationString(activity.animeRecord.getClassificationInt()));
        } else {
            type.setText(activity.getTypeString(activity.mangaRecord.getTypeInt()));
            episodes.setText(activity.mangaRecord.getChapters() == 0 ? "?" : Integer.toString(activity.mangaRecord.getChapters()));
            episodesLabel.setText(R.string.label_Chapters);
            volumes.setText(activity.mangaRecord.getVolumes() == 0 ? "?" : Integer.toString(activity.mangaRecord.getVolumes()));
            status.setText(activity.getStatusString(activity.mangaRecord.getStatusInt()));
            classification.setVisibility(View.GONE);
            classificationLabel.setVisibility(View.GONE);
        }

        score.setText(Float.toString(record.getMembersScore()));
        ranked.setText(Integer.toString(record.getRank()));
        popularity.setText(Integer.toString(record.getPopularityRank()));
        members.setText(Integer.toString(record.getMembersCount()));
        favorites.setText(Integer.toString(record.getFavoritedCount()));
    }
}