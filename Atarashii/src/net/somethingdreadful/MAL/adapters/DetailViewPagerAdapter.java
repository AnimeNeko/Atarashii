package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.DetailViewDetails;
import net.somethingdreadful.MAL.DetailViewGeneral;
import net.somethingdreadful.MAL.R;

public class DetailViewPagerAdapter extends FragmentPagerAdapter {
    DetailView activity;

    public DetailViewPagerAdapter(FragmentManager fm, DetailView activity) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DetailViewGeneral();
            case 1:
                return new DetailViewDetails();
            default:
                return new DetailViewGeneral();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return activity.getString(R.string.tab_name_general);
            case 1:
                return activity.getString(R.string.tab_name_details);
            default:
                return null;
        }
    }
}