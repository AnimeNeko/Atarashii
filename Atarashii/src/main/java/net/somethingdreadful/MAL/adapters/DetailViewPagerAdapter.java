package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.detailView.DetailViewDetails;
import net.somethingdreadful.MAL.detailView.DetailViewPersonal;
import net.somethingdreadful.MAL.detailView.DetailViewRecs;
import net.somethingdreadful.MAL.detailView.DetailViewReviews;

public class DetailViewPagerAdapter extends FragmentPagerAdapter {
    private final Fragments fragments;
    private boolean hidePersonal = false;
    private long fragmentId = 0;
    private final DetailView activity;

    public DetailViewPagerAdapter(FragmentManager fm, DetailView activity) {
        super(fm);
        this.activity = activity;
        fragments = new Fragments(activity);
        reCreate();
    }

    private void reCreate() {
        fragments.clear();
        fragments.add(new DetailViewDetails(), R.string.tab_name_details);
        if (!hidePersonal)
            fragments.add(new DetailViewPersonal(), R.string.tab_name_personal);
        if (APIHelper.isNetworkAvailable(activity)) {
            fragments.add(new DetailViewReviews(), R.string.tab_name_reviews);
            if (AccountService.isMAL())
                fragments.add(new DetailViewRecs(), R.string.tab_name_recommendations);
        }
    }

    public void hidePersonal(boolean hidePersonal) {
        if (hidePersonal != this.hidePersonal) {
            this.hidePersonal = hidePersonal;
            reCreate();
            notifyChangeInPosition(2);
            notifyDataSetChanged();
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.getFragment(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        return fragmentId + position;
    }

    @Override
    public int getCount() {
        return fragments.getSize();
    }

    @Override
    public String getPageTitle(int position) {
        return fragments.getName(position);
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