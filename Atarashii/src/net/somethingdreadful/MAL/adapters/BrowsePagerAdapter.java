package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.BrowseActivity;
import net.somethingdreadful.MAL.BrowseFragment;
import net.somethingdreadful.MAL.IGF;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.MALApi;

public class BrowsePagerAdapter extends FragmentPagerAdapter {
    private final Fragments fragments;

    public BrowsePagerAdapter(FragmentManager fm, BrowseActivity activity) {
        super(fm);
        fragments = new Fragments(activity);

        fragments.add(new BrowseFragment(), R.string.title_activity_browse);
        fragments.add(new IGF().setFriendList(MALApi.ListType.ANIME), String.valueOf(MALApi.ListType.ANIME));
        fragments.add(new IGF().setFriendList(MALApi.ListType.MANGA), String.valueOf(MALApi.ListType.MANGA));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.getFragment(position);
    }

    @Override
    public int getCount() {
        return fragments.getSize();
    }

    @Override
    public String getPageTitle(int position) {
        return fragments.getName(position);
    }
}