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

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.dialog.EpisodesPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;

import java.io.Serializable;

public class DetailViewPersonal extends Fragment implements Serializable, View.OnClickListener {
    public SwipeRefreshLayout swipeRefresh;

    DetailView activity;
    View view;
    Card cardBasic;

    TextView status;
    TextView progress1Total;
    TextView progress1Current;
    TextView progress2Total;
    TextView progress2Current;
    TextView myStartDate;
    TextView myEndDate;

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

        cardBasic.setContent(R.layout.card_detailview_personal_basic);

        cardBasic.setPadding(0, 0, 0, 0);

        status = (TextView) view.findViewById(R.id.statusText);
        progress1Total = (TextView) view.findViewById(R.id.progress1Text1);
        progress1Current = (TextView) view.findViewById(R.id.progress1Text2);
        progress2Total = (TextView) view.findViewById(R.id.progress2Text1);
        progress2Current = (TextView) view.findViewById(R.id.progress2Text2);
        myStartDate = (TextView) view.findViewById(R.id.myStartDate);
        myEndDate = (TextView) view.findViewById(R.id.myEndDate);

        view.findViewById(R.id.status).setOnClickListener(this);
        view.findViewById(R.id.progress1).setOnClickListener(this);
        view.findViewById(R.id.progress2).setOnClickListener(this);
        view.findViewById(R.id.startDatePanel).setOnClickListener(this);
        view.findViewById(R.id.endDatePanel).setOnClickListener(this);
    }

    public void setText() {
        if (activity.isAdded())
            status.setText(activity.getUserStatusString(activity.type.equals(MALApi.ListType.ANIME)
                    ? activity.animeRecord.getWatchedStatusInt()
                    : activity.mangaRecord.getReadStatusInt()));

        if (activity.type.equals(MALApi.ListType.ANIME)) {
            progress1Current.setText(Integer.toString(activity.animeRecord.getWatchedEpisodes()));
            progress1Total.setText(nullCheckOf(activity.animeRecord.getEpisodes()));
        } else {
            progress1Current.setText(Integer.toString(activity.mangaRecord.getVolumesRead()));
            progress1Total.setText(nullCheckOf(activity.mangaRecord.getVolumes()));

            progress2Current.setText(Integer.toString(activity.mangaRecord.getChaptersRead()));
            progress2Total.setText(nullCheckOf(activity.mangaRecord.getChapters()));
        }

        myStartDate.setText(getDate(activity.animeRecord.getWatchingStart()));
        myEndDate.setText(getDate(activity.animeRecord.getWatchingEnd()));

        setCard();
    }

    private String nullCheck(String string) {
        return ((string == null || string.equals("")) ? getString(R.string.card_content_none) : string);
    }

    private String nullCheckOf(int number) {
        return "/" + (number == 0 ? "?" : Integer.toString(number));
    }

    private String getDate(String string) {
        return nullCheck((string == null || string.equals("")) ? getString(R.string.card_content_none) : MALDateTools.formatDateString(string, activity, true));
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
                break;
            case R.id.endDatePanel:
                break;
        }
    }
}