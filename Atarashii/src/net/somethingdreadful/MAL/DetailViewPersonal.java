package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.DatePickerDialogFragment;
import net.somethingdreadful.MAL.dialog.EpisodesPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MessageDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit.http.HEAD;

public class DetailViewPersonal extends Fragment implements Serializable, View.OnClickListener {
    public SwipeRefreshLayout swipeRefresh;

    DetailView activity;
    View view;
    Card cardBasic;
    Card cardOther;
    Card cardRewatch;

    TextView status;
    TextView progress1Total;
    TextView progress1Current;
    TextView progress2Total;
    TextView progress2Current;
    TextView myScore;
    TextView myStartDate;
    TextView myEndDate;
    TextView myPriority;
    TextView myTags;
    TextView comments;

    TextView fansubs;
    TextView storage;
    TextView storageCount;
    TextView dowloaded;

    TextView priority;
    TextView rewatchCount1;
    TextView rewatchCount2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_detailview_personal, container, false);

        setViews();
        setListener();

        activity.setPersonal(this);

        if (activity.isDone())
            setText();
        return view;
    }

    public void setCard() {
        if (activity.type != null && activity.type.equals(MALApi.ListType.ANIME)) {
            TextView progress1 = (TextView) view.findViewById(R.id.progress1Label);
            progress1.setText(getString(R.string.card_content_episodes));
            RelativeLayout progress2 = (RelativeLayout) view.findViewById(R.id.progress2);
            progress2.setVisibility(View.GONE);
        }
    }

    public void setListener() {
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    private void setViews() {
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        cardBasic = (Card) view.findViewById(R.id.basic);
        cardOther = (Card) view.findViewById(R.id.other);
        cardRewatch = (Card) view.findViewById(R.id.rewatch);

        cardBasic.setContent(R.layout.card_detailview_personal_basic);
        cardOther.setContent(R.layout.card_detailview_personal_other);
        cardRewatch.setContent(R.layout.card_detailview_personal_rewatch);

        cardBasic.setAllPadding(0, 0, 0, 0);
        cardOther.setAllPadding(0, 0, 0, 0);
        cardRewatch.setAllPadding(0, 0, 0, 0);

        status = (TextView) view.findViewById(R.id.statusText);
        progress1Total = (TextView) view.findViewById(R.id.progress1Text1);
        progress1Current = (TextView) view.findViewById(R.id.progress1Text2);
        progress2Total = (TextView) view.findViewById(R.id.progress2Text1);
        progress2Current = (TextView) view.findViewById(R.id.progress2Text2);
        myScore = (TextView) view.findViewById(R.id.myScore);
        myStartDate = (TextView) view.findViewById(R.id.myStartDate);
        myEndDate = (TextView) view.findViewById(R.id.myEndDate);
        myPriority = (TextView) view.findViewById(R.id.myPriority);
        myTags = (TextView) view.findViewById(R.id.myTags);
        comments = (TextView) view.findViewById(R.id.comments);

        fansubs = (TextView) view.findViewById(R.id.fansubs);
        storage = (TextView) view.findViewById(R.id.storage);
        storageCount = (TextView) view.findViewById(R.id.storage_amount);
        dowloaded = (TextView) view.findViewById(R.id.downloaded);

        priority = (TextView) view.findViewById(R.id.priority);
        rewatchCount1 = (TextView) view.findViewById(R.id.count2Text1);
        rewatchCount2 = (TextView) view.findViewById(R.id.count2Text2);

        view.findViewById(R.id.status).setOnClickListener(this);
        view.findViewById(R.id.progress1).setOnClickListener(this);
        view.findViewById(R.id.progress2).setOnClickListener(this);
        view.findViewById(R.id.scorePanel).setOnClickListener(this);
        view.findViewById(R.id.startDatePanel).setOnClickListener(this);
        view.findViewById(R.id.endDatePanel).setOnClickListener(this);
        view.findViewById(R.id.priorityPanel).setOnClickListener(this);
        view.findViewById(R.id.tagsPanel).setOnClickListener(this);
        view.findViewById(R.id.commentspanel).setOnClickListener(this);
        view.findViewById(R.id.fansubPanel).setOnClickListener(this);
        view.findViewById(R.id.storagePanel).setOnClickListener(this);
        view.findViewById(R.id.capacityPanel).setOnClickListener(this);
        view.findViewById(R.id.downloadPanel).setOnClickListener(this);
        view.findViewById(R.id.rewatchPriorityPanel).setOnClickListener(this);
        view.findViewById(R.id.countPanel).setOnClickListener(this);
    }

    public void setText() {
        if (activity.isAdded())
            status.setText(activity.getUserStatusString(activity.type.equals(MALApi.ListType.ANIME)
                    ? activity.animeRecord.getWatchedStatusInt()
                    : activity.mangaRecord.getReadStatusInt()));

        if (activity.type.equals(MALApi.ListType.ANIME)) {
            progress1Current.setText(Integer.toString(activity.animeRecord.getWatchedEpisodes()));
            progress1Total.setText(nullCheckOf(activity.animeRecord.getEpisodes()));

            myStartDate.setText(nullCheck(activity.animeRecord.getWatchingStart()));
            myEndDate.setText(nullCheck(activity.animeRecord.getWatchingEnd()));
        } else {
            progress1Current.setText(Integer.toString(activity.mangaRecord.getVolumesRead()));
            progress1Total.setText(nullCheckOf(activity.mangaRecord.getVolumes()));

            progress2Current.setText(Integer.toString(activity.mangaRecord.getChaptersRead()));
            progress2Total.setText(nullCheckOf(activity.mangaRecord.getChapters()));

            myStartDate.setText(nullCheck(activity.mangaRecord.getReadingStart()));
            myEndDate.setText(nullCheck(activity.mangaRecord.getReadingEnd()));
        }

        myScore.setText(nullCheck(activity.animeRecord.getScore()));
        myStartDate.setText(getDate(activity.animeRecord.getStartDate()));
        myEndDate.setText(getDate(activity.animeRecord.getEndDate()));
        myPriority.setText(getString(R.array.priorityArray, activity.animeRecord.getPriority()));
        myTags.setText(activity.animeRecord.getTags() != null ? TextUtils.join(", ", activity.animeRecord.getTags()) : getString(R.string.card_content_none));
        comments.setText(nullCheck(activity.animeRecord.getPersonalComments()));

        fansubs.setText(nullCheck(activity.animeRecord.getFansubGroup()));
        storage.setText(getString(R.array.storageArray, activity.animeRecord.getStorage()));
        storageCount.setText(Integer.toString(activity.animeRecord.getStorageValue()));
        dowloaded.setText(nullCheck(Integer.toString(activity.animeRecord.getEpsDownloaded())));

        priority.setText(getString(R.array.priorityRewatchArray, activity.animeRecord.getRewatchValue()));
        rewatchCount1.setText(nullCheckOf(activity.animeRecord.getEpisodes()));
        rewatchCount2.setText(nullCheck(activity.animeRecord.getRewatchCount()));

        cardOther.findViewById(R.id.capacityPanel).setVisibility((activity.animeRecord.getStorage() == 0 || activity.animeRecord.getStorage() == 3) ? View.GONE : View.VISIBLE);

        setCard();
    }

    private String nullCheck(String string) {
        return ((string == null || string.equals("") || string.equals("0-00-00")) ? getString(R.string.unknown) : string);
    }

    private String nullCheck(int number) {
        return (number == 0 ? "?" : Integer.toString(number));
    }

    private String nullCheckOf(int number) {
        return "/" + (number == 0 ? "?" : Integer.toString(number));
    }

    private String getDate(String string) {
        return nullCheck((string == null || string.equals("")) ? getString(R.string.card_content_none) : MALDateTools.formatDateString(string, activity, true));
    }

    private String getString(int arrayId, int position) {
        return getResources().getStringArray(arrayId)[position];
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((DetailView) activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.status:
                activity.showDialog("statusPicker", new StatusPickerDialogFragment());
                break;
            case R.id.progress1:
                activity.showDialog("episodes", new EpisodesPickerDialogFragment());
                break;
            case R.id.progress2:
                activity.showDialog("manga", new MangaPickerDialogFragment());
                break;
            case R.id.startDatePanel:
                Bundle args1 = new Bundle();
                args1.putBoolean("startDate", true);
                activity.showDialog("startDate", new DatePickerDialogFragment(), args1);
                break;
            case R.id.endDatePanel:
                Bundle args2 = new Bundle();
                args2.putBoolean("startDate", false);
                activity.showDialog("endDate", new DatePickerDialogFragment(), args2);
            case R.id.scorePanel:
                break;
            case R.id.priorityPanel:
                break;
            case R.id.tagsPanel:
                Bundle args3 = new Bundle();
                args3.putBoolean("BBCode", false);
                args3.putString("message", TextUtils.join(", ", activity.animeRecord.getTags() != null ? activity.animeRecord.getTags() : new ArrayList()));
                args3.putString("title", "test");
                activity.showDialog("tags", new MessageDialogFragment(), args3);
                break;
            case R.id.commentspanel:
                break;
            case R.id.fansubPanel:
                break;
            case R.id.storagePanel:
                break;
            case R.id.capacityPanel:
                break;
            case R.id.downloadPanel:
                break;
            case R.id.rewatchPriorityPanel:
                break;
            case R.id.countPanel:
                break;
        }
    }
}