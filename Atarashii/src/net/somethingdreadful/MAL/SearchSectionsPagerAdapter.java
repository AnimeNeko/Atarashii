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
                args.putSerializable("type", MALApi.ListType.ANIME);
                break;
            case 1:
                args.putSerializable("type", MALApi.ListType.MANGA);
                break;
            default:
                args.putSerializable("type", MALApi.ListType.ANIME);
                break;
        }

        fragment.setArguments(args);
        return fragment;
    }


}