package net.somethingdreadful.MAL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.RecordStub;

import org.holoeverywhere.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class DetailViewDetails extends Fragment implements Serializable, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

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

    ListView relations;

    ArrayList<RecordStub> relationsList;
    ListViewAdapter<RecordStub> listadapter;
    Integer headers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_details, container, false);

        setViews();
        setListener();

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

        relations = (ListView) view.findViewById(R.id.ListView);
        relations.setOnItemClickListener(this);

        relationsList = new ArrayList<RecordStub>();
        listadapter = new ListViewAdapter<RecordStub>(activity.getApplicationContext(), R.layout.record_details_listview);
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

        relationsList.clear();
        headers = 0;

        if (activity.type.equals(MALApi.ListType.ANIME)) {
            addRelations(activity.animeRecord.getMangaAdaptions(), R.string.card_content_adaptions);
            addRelations(activity.animeRecord.getParentStory(), R.string.card_content_parentstory);
            addRelations(activity.animeRecord.getPrequels(), R.string.card_content_prequel);
            addRelations(activity.animeRecord.getSequels(), R.string.card_content_sequel);
            addRelations(activity.animeRecord.getSideStories(), R.string.card_content_sidestories);
            addRelations(activity.animeRecord.getSpinOffs(), R.string.card_content_spinoffs);
            addRelations(activity.animeRecord.getSummaries(), R.string.card_content_summaries);
            addRelations(activity.animeRecord.getCharacterAnime(), R.string.card_content_character);
            addRelations(activity.animeRecord.getAlternativeVersions(), R.string.card_content_alternativeversions);
        } else {
            addRelations(activity.mangaRecord.getAnimeAdaptations(), R.string.card_content_adaptions);
            addRelations(activity.mangaRecord.getRelatedManga(), R.string.card_content_related);
            addRelations(activity.mangaRecord.getAlternativeVersions(), R.string.card_content_alternativeversions);
        }
        relations.setAdapter(listadapter);
        listadapter.supportAddAll(relationsList);
        listadapter.notifyDataSetChanged();
        cardRelations.refreshList(relationsList.size(), 56, headers, 48, 1);
    }

    public void addRelations(ArrayList<RecordStub> recordStub, Integer header) {
        if (recordStub != null && recordStub.size() != 0) {
            RecordStub headerStub = new RecordStub();
            headerStub.setId(0, MALApi.ListType.ANIME);
            headerStub.setTitle(getString(header));
            relationsList.add(headerStub);
            relationsList.addAll(recordStub);
            headers = headers + 1;
        }
    }

    public void addRelations(RecordStub recordStub, Integer header) {
        if (recordStub != null) {
            RecordStub headerStub = new RecordStub();
            headerStub.setId(0, MALApi.ListType.ANIME);
            headerStub.setTitle(getString(header));
            relationsList.add(headerStub);
            relationsList.add(recordStub);
            headers = headers + 1;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (relationsList.get(position).getId() != 0) {
            Intent detailView = new Intent(activity, DetailView.class);
            detailView.putExtra("recordID", relationsList.get(position).getId());
            detailView.putExtra("recordType", relationsList.get(position).getType());
            startActivity(detailView);
        }
    }

    public class ListViewAdapter<T> extends ArrayAdapter<T> {
        Context context;

        public ListViewAdapter(Context context, int resource) {
            super(context, resource);
            this.context = context;
        }

        public View getView(int position, View view, ViewGroup parent) {
            final RecordStub record = (relationsList.get(position));

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (record.getId() == 0)
                view = inflater.inflate(R.layout.record_details_listview_header, parent, false);
            else
                view = inflater.inflate(R.layout.record_details_listview, parent, false);

            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(record.getTitle());
            return view;
        }

        public void supportAddAll(Collection<? extends T> collection) {
            this.clear();
            for (T record : collection) {
                this.add(record);
            }
        }
    }
}