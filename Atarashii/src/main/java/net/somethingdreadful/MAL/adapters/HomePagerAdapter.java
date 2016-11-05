package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.Home;
import net.somethingdreadful.MAL.IGF;
import net.somethingdreadful.MAL.api.MALApi;

public class HomePagerAdapter extends FragmentPagerAdapter {
    private final Fragments fragments;

    /**
     * Init page adapter
     */
    public HomePagerAdapter(FragmentManager fm, Home activity) {
        super(fm);
        fragments = new Fragments(activity);

        IGF AIGF = new IGF();
        AIGF.setListType(MALApi.ListType.ANIME);
        fragments.add(new IGF(), MALApi.ListType.ANIME.toString());

        IGF MIGF = new IGF();
        AIGF.setListType(MALApi.ListType.ANIME);
        fragments.add(new IGF(), MALApi.ListType.ANIME.toString());
    }

    @Override
    public Fragment getItem(int i) {
        IGF fragment = new IGF();
        fragment.setListType(i == 0 ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
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

    private MALApi.ListType getTag(int position) {
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