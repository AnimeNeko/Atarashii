package net.somethingdreadful.MAL;

import android.os.Bundle;
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
        Fragment fragment;
        Bundle args = new Bundle();

        fragment = new ItemGridFragment();
        switch (i) {
            case 0:
                args.putInt("type", MALApi.ListType.ANIME.value);
                break;
            case 1:
                args.putInt("type", MALApi.ListType.MANGA.value);
                break;
            default:
                args.putInt("type", MALApi.ListType.ANIME.value);
                break;
        }

        fragment.setArguments(args);
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