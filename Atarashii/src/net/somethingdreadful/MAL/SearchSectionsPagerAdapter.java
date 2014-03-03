package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

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
                args.putString("type", MALManager.TYPE_ANIME);
                break;
            case 1:
                args.putString("type", MALManager.TYPE_MANGA);
                break;
            default:
                args.putString("type", MALManager.TYPE_ANIME);
                break;
        }

        fragment.setArguments(args);
        return fragment;
    }


}