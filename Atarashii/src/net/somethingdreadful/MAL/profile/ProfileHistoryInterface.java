package net.somethingdreadful.MAL.profile;

import android.content.Intent;
import android.webkit.JavascriptInterface;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.UserProfile.History;

public class ProfileHistoryInterface {
    ProfileHistory history;

    ProfileHistoryInterface(ProfileHistory history) {
        this.history = history;
    }

    /**
     * This method will be triggered when the user clicks on a cover image.
     *
     * @param position The array position of the history item
     */
    @JavascriptInterface
    public void viewProfile(String position) {
        History historyItem = history.activity.record.getActivity().get(Integer.parseInt(position));
        int id = historyItem.getId();
        Intent detailView = new Intent(history.activity, DetailView.class);
        detailView.putExtra("recordID", id);
        detailView.putExtra("recordType", (historyItem.getSeries().getSeriesType().equals("anime") ? MALApi.ListType.ANIME : MALApi.ListType.MANGA));
        history.startActivity(detailView);
    }
}