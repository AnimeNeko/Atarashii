package net.somethingdreadful.MAL.detailView;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.DatePickerDialogFragment;
import net.somethingdreadful.MAL.dialog.InputDialogFragment;
import net.somethingdreadful.MAL.dialog.ListDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailViewPersonal extends Fragment implements Serializable, View.OnClickListener {
    public SwipeRefreshLayout swipeRefresh;

    private DetailView activity;
    private View view;
    private Card cardOther;

    @BindView(R.id.statusText)
    TextView status;
    @BindView(R.id.progress1Text1)
    TextView progress1Total;
    @BindView(R.id.progress1Text2)
    TextView progress1Current;
    @BindView(R.id.progress2Text1)
    TextView progress2Total;
    @BindView(R.id.progress2Text2)
    TextView progress2Current;
    @BindView(R.id.myScore)
    TextView myScore;
    @BindView(R.id.myStartDate)
    TextView myStartDate;
    @BindView(R.id.myEndDate)
    TextView myEndDate;
    @BindView(R.id.myPriority)
    TextView myPriority;
    @BindView(R.id.myTags)
    TextView myTags;
    @BindView(R.id.comments)
    TextView comments;

    @BindView(R.id.storage)
    TextView storage;
    @BindView(R.id.storage_amount)
    TextView storageCount;

    @BindView(R.id.priority)
    TextView priority;
    @BindView(R.id.count2Text2)
    TextView rewatchCount2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        super.onCreate(state);
        view = inflater.inflate(R.layout.activity_detailview_personal, container, false);

        setViews();
        setListener();

        activity.setPersonal(this);

        if (activity.isDone())
            setText();
        return view;
    }

    private void setCard() {
        if (activity.type != null && activity.type.equals(MALApi.ListType.ANIME)) {
            TextView progress1 = (TextView) view.findViewById(R.id.progress1Label);
            progress1.setText(getString(R.string.card_content_episodes));
            RelativeLayout progress2 = (RelativeLayout) view.findViewById(R.id.progress2);
            progress2.setVisibility(View.GONE);
        }
    }

    private void setListener() {
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    private void setViews() {
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        Card cardBasic = (Card) view.findViewById(R.id.basic);
        Card cardRewatch = (Card) view.findViewById(R.id.rewatch);
        cardOther = (Card) view.findViewById(R.id.other);

        cardBasic.setContent(R.layout.card_detailview_personal_basic);
        cardRewatch.setContent(R.layout.card_detailview_personal_rewatch);
        cardOther.setContent(R.layout.card_detailview_personal_other);

        ButterKnife.bind(this, view);

        cardBasic.setAllPadding(0, 0, 0, 0);
        cardRewatch.setAllPadding(0, 0, 0, 0);
        cardOther.setAllPadding(0, 0, 0, 0);

        setCard(R.id.status, false);
        setCard(R.id.progress1, false);
        setCard(R.id.progress2, false);
        setCard(R.id.scorePanel, false);
        setCard(R.id.startDatePanel, true);
        setCard(R.id.endDatePanel, true);
        setCard(R.id.priorityPanel, true);
        setCard(R.id.tagsPanel, true);
        setCard(R.id.commentspanel, false);
        setCard(R.id.storagePanel, true);
        setCard(R.id.capacityPanel, true);
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
            progress1Current.setText(String.valueOf(activity.animeRecord.getWatchedEpisodes()));
            progress1Total.setText(nullCheckOf(activity.animeRecord.getEpisodes()));

            myStartDate.setText(activity.nullCheck(activity.animeRecord.getWatchingStart()));
            myEndDate.setText(activity.nullCheck(activity.animeRecord.getWatchingEnd()));

            myScore.setText(activity.nullCheck(Theme.getDisplayScore(activity.animeRecord.getScore())));
            myStartDate.setText(activity.getDate(activity.animeRecord.getWatchingStart()));
            myEndDate.setText(activity.getDate(activity.animeRecord.getWatchingEnd()));
            myPriority.setText(getString(R.array.priorityArray, activity.animeRecord.getPriority()));
            myTags.setText(activity.animeRecord.getPersonalTagsString().equals("") ? getString(R.string.card_content_none) : activity.animeRecord.getPersonalTagsString());
            comments.setText(activity.nullCheck(activity.animeRecord.getNotes()));

            storage.setText(getString(R.array.storageArray, activity.animeRecord.getStorage()));
            storageCount.setText(String.valueOf(activity.animeRecord.getStorageValue()));

            priority.setText(getString(R.array.priorityRewatchArray, activity.animeRecord.getRewatchValue()));
            rewatchCount2.setText(activity.nullCheck(activity.animeRecord.getRewatchCount()));

            cardOther.findViewById(R.id.capacityPanel).setVisibility((activity.animeRecord.getStorage() == 0 || activity.animeRecord.getStorage() == 3) ? View.GONE : View.VISIBLE);

        } else {
            progress1Current.setText(String.valueOf(activity.mangaRecord.getVolumesRead()));
            progress1Total.setText(nullCheckOf(activity.mangaRecord.getVolumes()));

            progress2Current.setText(String.valueOf(activity.mangaRecord.getChaptersRead()));
            progress2Total.setText(nullCheckOf(activity.mangaRecord.getChapters()));

            myStartDate.setText(activity.nullCheck(activity.mangaRecord.getReadingStart()));
            myEndDate.setText(activity.nullCheck(activity.mangaRecord.getReadingEnd()));

            myScore.setText(activity.nullCheck(Theme.getDisplayScore(activity.mangaRecord.getScore())));
            myStartDate.setText(activity.getDate(activity.mangaRecord.getReadingStart()));
            myEndDate.setText(activity.getDate(activity.mangaRecord.getReadingEnd()));
            myPriority.setText(getString(R.array.priorityArray, activity.mangaRecord.getPriority()));
            myTags.setText(activity.mangaRecord.getPersonalTagsString().equals("") ? getString(R.string.card_content_none) : activity.mangaRecord.getPersonalTagsString());
            comments.setText(activity.nullCheck(activity.mangaRecord.getNotes()));

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
                args1.putString("current", activity.isAnime() ? activity.animeRecord.getWatchingStart() : activity.mangaRecord.getReadingStart());
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
                args3.putInt("current", activity.isAnime() ? activity.animeRecord.getScore() : activity.mangaRecord.getScore());
                args3.putInt("max", PrefManager.getMaxScore());
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
                args5.putString("message", activity.isAnime() ? activity.animeRecord.getPersonalTagsString() : activity.mangaRecord.getPersonalTagsString());
                args5.putString("hint", getString(R.string.dialog_hint_tags));
                activity.showDialog("tags", new InputDialogFragment().setCallback(activity), args5);
                break;
            case R.id.commentspanel:
                Bundle args6 = bundle(R.id.commentspanel, R.string.dialog_title_comment);
                args6.putString("message", activity.isAnime() ? activity.animeRecord.getNotes() : activity.mangaRecord.getNotes());
                args6.putString("hint", getString(R.string.dialog_hint_comment));
                activity.showDialog("tags", new InputDialogFragment().setCallback(activity), args6);
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
                args9.putDouble("current", activity.animeRecord.getStorageValue());
                args9.putInt("max", 10);
                activity.showDialog("storagevalue", new NumberPickerDialogFragment().setOnSendClickListener(activity), args9);
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

    private Bundle bundle(int id, int title) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("title", getString(title));
        return bundle;
    }
}