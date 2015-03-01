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

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.DetailViewRelationsAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.GenericRecord;

import java.io.Serializable;

public class DetailViewDetails extends Fragment implements Serializable, ExpandableListView.OnChildClickListener {

    public SwipeRefreshLayout swipeRefresh;
    DetailView activity;
    View view;

    Card cardSynopsis;
    Card cardMediainfo;
    Card cardMediaStats;
    Card cardRelations;
    Card cardTitles;
    Card cardNetwork;

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
    ExpandableListView titles;
    DetailViewRelationsAdapter relation;
    DetailViewRelationsAdapter title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_details, container, false);

        setViews();
        setListener();

        activity.setDetails(this);

        if (activity.isDone())
            setText();
        else if (!MALApi.isNetworkAvailable(activity))
            toggleView(false);
        return view;
    }

    /*
     * The scrollview bugs when you use viewflipper in it!
     */
    public void toggleView(Boolean show) {
        if (show) {
            cardSynopsis.setVisibility(View.VISIBLE);
            cardMediainfo.setVisibility(View.VISIBLE);
            cardMediaStats.setVisibility(View.VISIBLE);
            cardRelations.setVisibility(View.VISIBLE);
            cardTitles.setVisibility(View.VISIBLE);
            cardNetwork.setVisibility(View.GONE);
        } else {
            cardSynopsis.setVisibility(View.GONE);
            cardMediainfo.setVisibility(View.GONE);
            cardMediaStats.setVisibility(View.GONE);
            cardRelations.setVisibility(View.GONE);
            cardTitles.setVisibility(View.GONE);
            cardNetwork.setVisibility(View.VISIBLE);
        }
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
        cardTitles = (Card) view.findViewById(R.id.titles);
        cardNetwork = (Card) view.findViewById(R.id.network_Card);

        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_details_mediainfo);
        cardMediaStats.setContent(R.layout.card_detailview_details_mediastats);
        cardRelations.setContent(R.layout.card_detailview_details_relations);
        cardTitles.setContent(R.layout.card_detailview_details_relations);

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

        relation = new DetailViewRelationsAdapter(activity.getApplicationContext());
        relations = (ExpandableListView) cardRelations.findViewById(R.id.ListView);
        relations.setOnChildClickListener(this);
        relations.setAdapter(relation);

        title = new DetailViewRelationsAdapter(activity.getApplicationContext());
        titles = (ExpandableListView) cardTitles.findViewById(R.id.ListView);
        titles.setAdapter(title);

        view.findViewById(R.id.MALstats).setVisibility(AccountService.isMAL() ? View.VISIBLE : View.GONE);

        clickListeners();
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
        GenericRecord record = (activity.type.equals(MALApi.ListType.ANIME) ? activity.animeRecord : activity.mangaRecord);
        if (record.getSynopsis() == null)
            return;

        toggleView(true);
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

        relation.clear();
        title.clear();

        if (activity.type.equals(MALApi.ListType.ANIME)) {
            relation.addRelations(activity.animeRecord.getMangaAdaptions(), getString(R.string.card_content_adaptions));
            relation.addRelations(activity.animeRecord.getParentStory(), getString(R.string.card_content_parentstory));
            relation.addRelations(activity.animeRecord.getPrequels(), getString(R.string.card_content_prequel));
            relation.addRelations(activity.animeRecord.getSequels(), getString(R.string.card_content_sequel));
            relation.addRelations(activity.animeRecord.getSideStories(), getString(R.string.card_content_sidestories));
            relation.addRelations(activity.animeRecord.getSpinOffs(), getString(R.string.card_content_spinoffs));
            relation.addRelations(activity.animeRecord.getSummaries(), getString(R.string.card_content_summaries));
            relation.addRelations(activity.animeRecord.getCharacterAnime(), getString(R.string.card_content_character));
            relation.addRelations(activity.animeRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));

            title.addTitles(activity.animeRecord.getOtherTitlesJapanese(), getString(R.string.card_content_japanese));
            title.addTitles(activity.animeRecord.getOtherTitlesEnglish(), getString(R.string.card_content_english));
            title.addTitles(activity.animeRecord.getOtherTitlesSynonyms(), getString(R.string.card_content_synonyms));
        } else {
            relation.addRelations(activity.mangaRecord.getAnimeAdaptations(), getString(R.string.card_content_adaptions));
            relation.addRelations(activity.mangaRecord.getRelatedManga(), getString(R.string.card_content_related));
            relation.addRelations(activity.mangaRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));

            title.addTitles(activity.mangaRecord.getOtherTitlesJapanese(), getString(R.string.card_content_japanese));
            title.addTitles(activity.mangaRecord.getOtherTitlesEnglish(), getString(R.string.card_content_english));
            title.addTitles(activity.mangaRecord.getOtherTitlesSynonyms(), getString(R.string.card_content_synonyms));
        }

        relation.notifyDataSetChanged();
        title.notifyDataSetChanged();
        cardRelations.refreshList(relation);
        cardTitles.refreshList(title);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPos, int childPos, long id) {
        Intent detailView = new Intent(activity, DetailView.class);
        detailView.putExtra("recordID", relation.getRecordStub(groupPos, childPos).getId());
        detailView.putExtra("recordType", relation.getRecordStub(groupPos, childPos).getType());
        startActivity(detailView);
        return true;
    }

    public void clickListeners() {
        titles.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                title.expand(i);
                cardTitles.refreshList(title);
            }
        });
        titles.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                title.collapse(i);
                cardTitles.refreshList(title);
            }
        });

        relations.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                relation.expand(i);
                cardRelations.refreshList(relation);
            }
        });
        relations.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                relation.collapse(i);
                cardRelations.refreshList(relation);
            }
        });
    }
}