package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.somethingdreadful.MAL.adapters.DetailViewRelationsAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.GenericRecord;

import java.io.Serializable;

public class DetailViewDetails extends Fragment implements Serializable, ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnGroupClickListener {

    public SwipeRefreshLayout swipeRefresh;
    DetailView activity;

    View view;

    Card cardSynopsis;

    Card cardMediainfo;
    Card cardMediaStats;
    Card cardRelations;

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

    ExpandableListView relations;

    DetailViewRelationsAdapter listadapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_details, container, false);

        setViews();
        setListener();

        activity.setDetails(this);

        if (activity.isDone())
            setText();
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
        cardRelations = (Card) view.findViewById(R.id.relations);

        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_details_mediainfo);
        cardMediaStats.setContent(R.layout.card_detailview_details_mediastats);
        cardRelations.setContent(R.layout.card_detailview_details_relations);

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

        listadapter = new DetailViewRelationsAdapter(activity.getApplicationContext());

        relations = (ExpandableListView) view.findViewById(R.id.ListView);
        relations.setOnChildClickListener(this);
        relations.setOnGroupExpandListener(this);
        relations.setOnGroupCollapseListener(this);
        relations.setOnGroupClickListener(this);
        relations.setAdapter(listadapter);
    }

    /*
     * set all the ClickListeners
     */
    public void setListener() {
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
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

        synopsis.setText(record.getSpannedSynopsis());
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

        listadapter.clear();

        if (activity.type.equals(MALApi.ListType.ANIME)) {
            listadapter.addRelations(activity.animeRecord.getMangaAdaptions(), getString(R.string.card_content_adaptions));
            listadapter.addRelations(activity.animeRecord.getParentStory(), getString(R.string.card_content_parentstory));
            listadapter.addRelations(activity.animeRecord.getPrequels(), getString(R.string.card_content_prequel));
            listadapter.addRelations(activity.animeRecord.getSequels(), getString(R.string.card_content_sequel));
            listadapter.addRelations(activity.animeRecord.getSideStories(), getString(R.string.card_content_sidestories));
            listadapter.addRelations(activity.animeRecord.getSpinOffs(), getString(R.string.card_content_spinoffs));
            listadapter.addRelations(activity.animeRecord.getSummaries(), getString(R.string.card_content_summaries));
            listadapter.addRelations(activity.animeRecord.getCharacterAnime(), getString(R.string.card_content_character));
            listadapter.addRelations(activity.animeRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));
        } else {
            listadapter.addRelations(activity.mangaRecord.getAnimeAdaptations(), getString(R.string.card_content_adaptions));
            listadapter.addRelations(activity.mangaRecord.getRelatedManga(), getString(R.string.card_content_related));
            listadapter.addRelations(activity.mangaRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));
        }

        listadapter.notifyDataSetChanged();
        cardRelations.refreshList(listadapter);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPos, int childPos, long id) {
        Intent detailView = new Intent(activity, DetailView.class);
        detailView.putExtra("recordID", listadapter.getRecordStub(groupPos, childPos).getId());
        detailView.putExtra("recordType", listadapter.getRecordStub(groupPos, childPos).getType());
        startActivity(detailView);
        return true;
    }

    @Override
    public void onGroupExpand(int i) {
        listadapter.expand(i);
        cardRelations.refreshList(listadapter);
    }

    @Override
    public void onGroupCollapse(int i) {
        listadapter.collapse(i);
        cardRelations.refreshList(listadapter);
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        return false;
    }
}