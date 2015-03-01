package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.ProfileDetails;
import net.somethingdreadful.MAL.ProfileFriends;
import net.somethingdreadful.MAL.R;

public class ProfilePagerAdapter extends FragmentPagerAdapter {
    ProfileActivity activity;

    public ProfilePagerAdapter(FragmentManager fm, ProfileActivity activity) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ProfileDetails();
            case 1:
                return new ProfileFriends();
            default:
                return new ProfileDetails();
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
                return activity.getString(R.string.tab_name_details);
            case 1:
                return activity.getString(R.string.tab_name_friends);
            default:
                return null;
        }
    }
}