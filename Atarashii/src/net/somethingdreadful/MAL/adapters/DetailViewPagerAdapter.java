package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.detailView.DetailViewDetails;
import net.somethingdreadful.MAL.detailView.DetailViewGeneral;
import net.somethingdreadful.MAL.detailView.DetailViewPersonal;
import net.somethingdreadful.MAL.detailView.DetailViewReviews;

public class DetailViewPagerAdapter extends FragmentPagerAdapter {
    private int count;
    private boolean hidePersonal = false;
    private final DetailView activity;
    private int maxCount = 4;
    public ViewGroup container;
    private long fragmentId = 0;

    public DetailViewPagerAdapter(FragmentManager fm, DetailView activity) {
        super(fm);
        this.activity = activity;
        this.maxCount = MALApi.isNetworkAvailable(activity) ? maxCount : maxCount - 1;
        this.count = getMaxcount();
    }

    private int getMaxcount() {
        return maxCount;
    }

    public void hidePersonal(boolean hidePersonal) {
        if (hidePersonal != this.hidePersonal) {
            this.hidePersonal = hidePersonal;
            TabLayout tabs = (TabLayout) activity.findViewById(R.id.tabs);
            if (tabs != null) {
                if (tabs.getTabCount() == 4)
                    tabs.removeTabAt(2);
                if (!hidePersonal)
                    tabs.addTab(tabs.newTab().setText(getPageTitle(2)), 2);
            }
            notifyChangeInPosition(2);
            count = hidePersonal ? count - 1 : count;
            notifyDataSetChanged();
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DetailViewGeneral();
            case 1:
                return new DetailViewDetails();
            case 2:
                if (!hidePersonal)
                    return new DetailViewPersonal();
            case 3:
                return new DetailViewReviews();
            default:
                return new DetailViewGeneral();
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return activity.getString(R.string.tab_name_general);
            case 1:
                return activity.getString(R.string.tab_name_details);
            case 2:
                if (!hidePersonal)
                    return activity.getString(R.string.tab_name_personal);
            case 3:
                return activity.getString(R.string.tab_name_reviews);
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        return fragmentId + position;
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     *
     * @param number number of items which have been changed
     */
    private void notifyChangeInPosition(int number) {
        fragmentId += getCount() + number;
    }
}