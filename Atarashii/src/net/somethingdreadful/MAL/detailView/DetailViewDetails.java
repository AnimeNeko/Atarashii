package net.somethingdreadful.MAL.detailView;

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
import android.widget.TableRow;
import android.widget.TextView;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.DetailViewRelationsAdapter;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.MALApi;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailViewDetails extends Fragment implements Serializable, ExpandableListView.OnChildClickListener {
    View view;
    Card cardSynopsis;
    Card cardMediainfo;
    Card cardMediaStats;
    Card cardRelations;
    Card cardTitles;
    Card cardNetwork;
    DetailView activity;
    ExpandableListView relations;
    ExpandableListView titles;
    DetailViewRelationsAdapter relation;
    DetailViewRelationsAdapter title;

    @InjectView(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;

    @InjectView(R.id.SynopsisContent) TextView  synopsis;
    @InjectView(R.id.type) TextView type;
    @InjectView(R.id.episodes) TextView episodes;
    @InjectView(R.id.episodesLabel) TextView episodesLabel;
    @InjectView(R.id.volumes) TextView volumes;
    @InjectView(R.id.volumesLabel) TextView volumesLabel;
    @InjectView(R.id.status) TextView status;
    @InjectView(R.id.start) TextView start;
    @InjectView(R.id.startRow) TableRow startRow;
    @InjectView(R.id.end) TextView end;
    @InjectView(R.id.endRow) TableRow endRow;
    @InjectView(R.id.classification) TextView classification;
    @InjectView(R.id.classificationLabel) TextView classificationLabel;
    @InjectView(R.id.genres) TextView genres;
    @InjectView(R.id.producers) TextView producers;
    @InjectView(R.id.producersRow) TableRow producersRow;

    @InjectView(R.id.score) TextView score;
    @InjectView(R.id.ranked) TextView ranked;
    @InjectView(R.id.popularity) TextView popularity;
    @InjectView(R.id.members) TextView members;
    @InjectView(R.id.favorites) TextView favorites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_details, container, false);

        setViews();
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

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

    /**
     * Set all views once
     */
    public void setViews() {
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
        ButterKnife.inject(this, view);

        if (!AccountService.isMAL()) {
            producers.setVisibility(View.GONE);
            producersRow.setVisibility(View.GONE);
        }

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

    /**
     * Place all the text in the right textview
     */
    public void setText() {
        GenericRecord record = (activity.type.equals(MALApi.ListType.ANIME) ? activity.animeRecord : activity.mangaRecord);
        if (record.getSynopsis() == null)
            return;

        toggleView(true);
        activity.setMenu();

        synopsis.setText(record.getSynopsis());
        synopsis.setMovementMethod(LinkMovementMethod.getInstance());
        genres.setText("\u200F" + TextUtils.join(", ", activity.getGenresString(record.getGenresInt())));
        if (activity.type.equals(MALApi.ListType.ANIME)) {
            type.setText(activity.getTypeString(activity.animeRecord.getTypeInt()));
            episodes.setText(activity.nullCheck(activity.animeRecord.getEpisodes()));
            volumes.setVisibility(View.GONE);
            volumesLabel.setVisibility(View.GONE);
            status.setText(activity.getStatusString(activity.animeRecord.getStatusInt()));
            classification.setText(activity.getClassificationString(activity.animeRecord.getClassificationInt()));
            start.setText(activity.getDate(activity.animeRecord.getStartDate()));
            end.setText(activity.getDate(activity.animeRecord.getEndDate()));
            producers.setText("\u200F" + activity.animeRecord.getProducersString());
        } else {
            type.setText(activity.getTypeString(activity.mangaRecord.getTypeInt()));
            episodes.setText(activity.nullCheck(activity.mangaRecord.getChapters()));
            episodesLabel.setText(R.string.label_Chapters);
            volumes.setText(activity.nullCheck(activity.mangaRecord.getVolumes()));
            status.setText(activity.getStatusString(activity.mangaRecord.getStatusInt()));
            classification.setVisibility(View.GONE);
            classificationLabel.setVisibility(View.GONE);
            startRow.setVisibility(View.GONE);
            endRow.setVisibility(View.GONE);
            producersRow.setVisibility(View.GONE);
        }

        score.setText(record.getAverageScore());
        ranked.setText(String.valueOf(record.getRank()));
        popularity.setText(String.valueOf(record.getPopularity()));
        members.setText(String.valueOf(record.getAverageScoreCount()));
        favorites.setText(String.valueOf(record.getFavoritedCount()));

        relation.clear();
        title.clear();

        if (activity.type.equals(MALApi.ListType.ANIME)) {
            relation.addRelations(activity.animeRecord.getMangaAdaptations(), getString(R.string.card_content_adaptions));
            relation.addRelations(activity.animeRecord.getParentStory(), getString(R.string.card_content_parentstory));
            relation.addRelations(activity.animeRecord.getPrequels(), getString(R.string.card_content_prequel));
            relation.addRelations(activity.animeRecord.getSequels(), getString(R.string.card_content_sequel));
            relation.addRelations(activity.animeRecord.getSideStories(), getString(R.string.card_content_sidestories));
            relation.addRelations(activity.animeRecord.getSpinOffs(), getString(R.string.card_content_spinoffs));
            relation.addRelations(activity.animeRecord.getSummaries(), getString(R.string.card_content_summaries));
            relation.addRelations(activity.animeRecord.getCharacterAnime(), getString(R.string.card_content_character));
            relation.addRelations(activity.animeRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));
            relation.addRelations(activity.animeRecord.getOther(), getString(R.string.card_content_other));

            /*
            TODO: Enable this

            title.addTitles(activity.animeRecord.getOtherTitlesJapanese(), getString(R.string.card_content_japanese));
            title.addTitles(activity.animeRecord.getOtherTitlesEnglish(), getString(R.string.card_content_english));
            title.addTitles(activity.animeRecord.getOtherTitlesSynonyms(), getString(R.string.card_content_synonyms));
            */
        } else {
            relation.addRelations(activity.mangaRecord.getAnimeAdaptations(), getString(R.string.card_content_adaptions));
            relation.addRelations(activity.mangaRecord.getRelatedManga(), getString(R.string.card_content_related));
            relation.addRelations(activity.mangaRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));
            /*
            TODO: Enable this
            title.addTitles(activity.mangaRecord.getOtherTitlesJapanese(), getString(R.string.card_content_japanese));
            title.addTitles(activity.mangaRecord.getOtherTitlesEnglish(), getString(R.string.card_content_english));
            title.addTitles(activity.mangaRecord.getOtherTitlesSynonyms(), getString(R.string.card_content_synonyms));
            */
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

    /**
     * Handle the click events (expand and collapse)
     */
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