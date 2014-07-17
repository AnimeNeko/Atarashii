package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.tasks.TaskJob;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.api.MALApi;

public class HomeSectionsPagerAdapter extends FragmentPagerAdapter {

    public HomeSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        IGF fragment = new IGF();
        fragment.taskjob = TaskJob.GETLIST;
        fragment.isAnime = i == 0;
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MALApi.getListTypeString(getTag(position)).toUpperCase();
    }

    public MALApi.ListType getTag(int position) {
        switch (position) {
            case 0:
                return MALApi.ListType.ANIME;
            case 1:
                return MALApi.ListType.MANGA;
            default:
                return null;
        }
    }
}