package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.detailView.DetailViewDetails;
import net.somethingdreadful.MAL.detailView.DetailViewGeneral;
import net.somethingdreadful.MAL.detailView.DetailViewPersonal;
import net.somethingdreadful.MAL.detailView.DetailViewReviews;

public class DetailViewPagerAdapter extends FragmentPagerAdapter {
    public int count;
    boolean hidePersonal = false;
    DetailView activity;
    int maxCount = 4;
    FragmentManager fm;
    public ViewGroup container;

    public DetailViewPagerAdapter(FragmentManager fm, DetailView activity) {
        super(fm);
        this.fm = fm;
        this.activity = activity;
        this.maxCount = MALApi.isNetworkAvailable(activity) ? maxCount : maxCount - 1;
        this.count = getMaxcount();
    }

    public int getMaxcount() {
        return maxCount;
    }

    public void hidePersonal(boolean hidePersonal) {
        if (hidePersonal != this.hidePersonal) {
            this.hidePersonal = hidePersonal;
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment, new DetailViewReviews()).commit();

            count = hidePersonal ? count - 1 : count;
            this.notifyDataSetChanged();
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
}