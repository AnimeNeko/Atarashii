package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.DatePickerDialogFragment;
import net.somethingdreadful.MAL.dialog.ListDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MessageDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import java.io.Serializable;

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
        rewatchCount2 = (TextView) view.findViewById(R.id.count2Text2);

        setCard(R.id.status, false);
        setCard(R.id.progress1, false);
        setCard(R.id.progress2, false);
        setCard(R.id.scorePanel, false);
        setCard(R.id.startDatePanel, true);
        setCard(R.id.endDatePanel, true);
        setCard(R.id.priorityPanel, true);
        setCard(R.id.tagsPanel, true);
        setCard(R.id.commentspanel, false);
        setCard(R.id.fansubPanel, true);
        setCard(R.id.storagePanel, true);
        setCard(R.id.capacityPanel, true);
        setCard(R.id.downloadPanel, true);
        setCard(R.id.rewatchPriorityPanel, true);
        setCard(R.id.countPanel, false);
        if (!AccountService.isMAL())
            cardOther.setVisibility(View.GONE);
    }

    private void setCard(int id, boolean ALOnly) {
        if (ALOnly && !AccountService.isMAL())
            view.findViewById(id).setVisibility(View.GONE);
        else
            view.findViewById(id).setOnClickListener(this);
    }

    public void setText() {
        if (activity.isAdded())
            status.setText(activity.getUserStatusString(activity.isAnime()
                    ? activity.animeRecord.getWatchedStatusInt()
                    : activity.mangaRecord.getReadStatusInt()));

        if (activity.isAnime()) {
            progress1Current.setText(Integer.toString(activity.animeRecord.getWatchedEpisodes()));
            progress1Total.setText(nullCheckOf(activity.animeRecord.getEpisodes()));

            myStartDate.setText(activity.nullCheck(activity.animeRecord.getWatchingStart()));
            myEndDate.setText(activity.nullCheck(activity.animeRecord.getWatchingEnd()));

            myScore.setText(activity.nullCheck(activity.animeRecord.getScore()));
            myStartDate.setText(activity.getDate(activity.animeRecord.getWatchingStart()));
            myEndDate.setText(activity.getDate(activity.animeRecord.getWatchingEnd()));
            myPriority.setText(getString(R.array.priorityArray, activity.animeRecord.getPriority()));
            myTags.setText(activity.animeRecord.getPersonalTagsString().equals("") ? getString(R.string.card_content_none) : activity.animeRecord.getPersonalTagsString());
            comments.setText(activity.nullCheck(activity.animeRecord.getPersonalComments()));

            fansubs.setText(activity.nullCheck(activity.animeRecord.getFansubGroup()));
            storage.setText(getString(R.array.storageArray, activity.animeRecord.getStorage()));
            storageCount.setText(Integer.toString(activity.animeRecord.getStorageValue()));
            dowloaded.setText(activity.nullCheck(Integer.toString(activity.animeRecord.getEpsDownloaded())));

            priority.setText(getString(R.array.priorityRewatchArray, activity.animeRecord.getRewatchValue()));
            rewatchCount2.setText(activity.nullCheck(activity.animeRecord.getRewatchCount()));

            cardOther.findViewById(R.id.capacityPanel).setVisibility((activity.animeRecord.getStorage() == 0 || activity.animeRecord.getStorage() == 3) ? View.GONE : View.VISIBLE);

        } else {
            progress1Current.setText(Integer.toString(activity.mangaRecord.getVolumesRead()));
            progress1Total.setText(nullCheckOf(activity.mangaRecord.getVolumes()));

            progress2Current.setText(Integer.toString(activity.mangaRecord.getChaptersRead()));
            progress2Total.setText(nullCheckOf(activity.mangaRecord.getChapters()));

            myStartDate.setText(activity.nullCheck(activity.mangaRecord.getReadingStart()));
            myEndDate.setText(activity.nullCheck(activity.mangaRecord.getReadingEnd()));

            myScore.setText(activity.nullCheck(activity.mangaRecord.getScore()));
            myStartDate.setText(activity.getDate(activity.mangaRecord.getReadingStart()));
            myEndDate.setText(activity.getDate(activity.mangaRecord.getReadingEnd()));
            myPriority.setText(getString(R.array.priorityArray, activity.mangaRecord.getPriority()));
            myTags.setText(activity.mangaRecord.getPersonalTagsString().equals("") ? getString(R.string.card_content_none) : activity.mangaRecord.getPersonalTagsString());
            comments.setText(activity.nullCheck(activity.mangaRecord.getPersonalComments()));

            cardOther.setVisibility(View.GONE);

            priority.setText(getString(R.array.priorityRewatchArray, activity.mangaRecord.getRereadValue() != 0 ? activity.mangaRecord.getRereadValue() - 1 : 0));
            rewatchCount2.setText(activity.nullCheck(activity.mangaRecord.getRereadCount()));
        }
        setCard();
    }

    private String nullCheckOf(int number) {
        return "/" + activity.nullCheck(number);
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
                if (activity.isAnime()) {
                    Bundle args = bundle(R.id.progress1, R.string.dialog_title_watched_update);
                    args.putInt("current", activity.animeRecord.getWatchedEpisodes());
                    args.putInt("max", activity.animeRecord.getEpisodes());
                    activity.showDialog("episodes", new NumberPickerDialogFragment().setOnSendClickListener(activity), args);
                    break;
                }
            case R.id.progress2:
                activity.showDialog("manga", new MangaPickerDialogFragment());
                break;
            case R.id.startDatePanel:
                Bundle args1 = new Bundle();
                args1.putBoolean("startDate", true);
                args1.putString("current", activity.isAnime() ? activity.animeRecord.getWatchingStart() : activity.mangaRecord.getReadingStart() );
                activity.showDialog("startDate", new DatePickerDialogFragment(), args1);
                break;
            case R.id.endDatePanel:
                Bundle args2 = new Bundle();
                args2.putBoolean("startDate", false);
                args2.putString("current", activity.isAnime() ? activity.animeRecord.getWatchingEnd() : activity.mangaRecord.getReadingEnd());
                activity.showDialog("endDate", new DatePickerDialogFragment(), args2);
                break;
            case R.id.scorePanel:
                Bundle args3 = bundle(R.id.scorePanel, R.string.dialog_title_rating);
                if (PrefManager.getScoreType() == 1 || PrefManager.getScoreType() == 3)
                    args3.putInt("current", (int) (activity.isAnime() ? activity.animeRecord.getScore() : activity.mangaRecord.getScore()));
                else
                    args3.putFloat("current", activity.isAnime() ? activity.animeRecord.getScore() : activity.mangaRecord.getScore());
                args3.putInt("max", PrefManager.getScoreType() == 3 ? 5 : 10);
                activity.showDialog("rating", new NumberPickerDialogFragment().setOnSendClickListener(activity), args3);
                break;
            case R.id.priorityPanel:
                Bundle args4 = bundle(R.id.priorityPanel, R.string.card_content_my_priority);
                args4.putInt("current", activity.isAnime() ? activity.animeRecord.getPriority() : activity.mangaRecord.getPriority());
                args4.putInt("stringArray", R.array.priorityArray);
                args4.putInt("intArray", R.array.id);
                activity.showDialog("priority", new ListDialogFragment().setOnSendClickListener(activity), args4);
                break;
            case R.id.tagsPanel:
                Bundle args5 = bundle(R.id.tagsPanel, R.string.dialog_title_tags);
                args5.putBoolean("BBCode", false);
                args5.putString("message", activity.isAnime() ? activity.animeRecord.getPersonalTagsString() : activity.mangaRecord.getPersonalTagsString());
                args5.putString("hint", getString(R.string.dialog_hint_tags));
                activity.showDialog("tags", new MessageDialogFragment().setOnSendClickListener(activity), args5);
                break;
            case R.id.commentspanel:
                Bundle args6 = bundle(R.id.commentspanel, R.string.dialog_title_comment);
                args6.putBoolean("BBCode", false);
                args6.putString("message", activity.isAnime() ? activity.animeRecord.getPersonalComments() : activity.mangaRecord.getPersonalComments());
                args6.putString("hint", getString(R.string.dialog_hint_comment));
                activity.showDialog("tags", new MessageDialogFragment().setOnSendClickListener(activity), args6);
                break;
            case R.id.fansubPanel:
                Bundle args7 = bundle(R.id.fansubPanel, R.string.dialog_title_fansub);
                args7.putBoolean("BBCode", false);
                args7.putString("message", activity.animeRecord.getFansubGroup());
                args7.putString("hint", getString(R.string.dialog_hint_fansub));
                activity.showDialog("tags", new MessageDialogFragment().setOnSendClickListener(activity), args7);
                break;
            case R.id.storagePanel:
                Bundle args8 = bundle(R.id.storagePanel, R.string.dialog_title_storage);
                args8.putInt("current", activity.animeRecord.getStorage());
                args8.putInt("stringArray", R.array.storageArray);
                args8.putInt("intArray", R.array.id);
                activity.showDialog("storage", new ListDialogFragment().setOnSendClickListener(activity), args8);
                break;
            case R.id.capacityPanel:
                Bundle args9 = bundle(R.id.capacityPanel, R.string.dialog_title_storage_value);
                args9.putInt("current", activity.animeRecord.getStorageValue());
                args9.putInt("max", 10);
                activity.showDialog("storagevalue", new NumberPickerDialogFragment().setOnSendClickListener(activity), args9);
                break;
            case R.id.downloadPanel:
                Bundle args10 = bundle(R.id.downloadPanel, R.string.dialog_title_downloaded);
                args10.putInt("current", activity.animeRecord.getEpsDownloaded());
                args10.putInt("max", activity.animeRecord.getEpisodes());
                activity.showDialog("storagevalue", new NumberPickerDialogFragment().setOnSendClickListener(activity), args10);
                break;
            case R.id.rewatchPriorityPanel:
                Bundle args11 = bundle(R.id.rewatchPriorityPanel, R.string.dialog_title_rewatched_priority);
                args11.putInt("current", activity.isAnime() ? activity.animeRecord.getRewatchValue() : activity.mangaRecord.getRereadValue());
                args11.putInt("stringArray", R.array.priorityRewatchArray);
                args11.putInt("intArray", R.array.id);
                activity.showDialog("rewatchPriority", new ListDialogFragment().setOnSendClickListener(activity), args11);
                break;
            case R.id.countPanel:
                Bundle args12 = bundle(R.id.countPanel, R.string.dialog_title_rewatched_times);
                args12.putInt("current", activity.isAnime() ? activity.animeRecord.getRewatchCount() : activity.mangaRecord.getRereadCount());
                args12.putInt("max", 0); // will be set to 999 in the dialog
                activity.showDialog("storagevalue", new NumberPickerDialogFragment().setOnSendClickListener(activity), args12);
                break;
        }
    }

    public Bundle bundle(int id, int title) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("title", getString(title));
        return bundle;
    }
}