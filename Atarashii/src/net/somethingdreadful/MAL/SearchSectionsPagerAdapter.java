package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import net.somethingdreadful.MAL.api.MALApi;

public class SearchSectionsPagerAdapter extends HomeSectionsPagerAdapter {

    public SearchSectionsPagerAdapter(FragmentManager fm) {
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


}