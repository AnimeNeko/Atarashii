package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.IGF;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.profile.ProfileDetailsAL;
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL;
import net.somethingdreadful.MAL.profile.ProfileFriends;
import net.somethingdreadful.MAL.profile.ProfileHistory;

public class ProfilePagerAdapter extends FragmentPagerAdapter {
    Fragments fragments;

    public ProfilePagerAdapter(FragmentManager fm, ProfileActivity activity) {
        super(fm);
        fragments = new Fragments(activity);

        if (AccountService.isMAL()) {
            fragments.add(new ProfileDetailsMAL(), R.string.tab_name_details);
            fragments.add(new ProfileFriends(), R.string.tab_name_friends);
            if (APIHelper.isNetworkAvailable(activity)) {
                fragments.add(new ProfileHistory(), R.string.tab_name_history);
                fragments.add(new IGF().setFriendList(MALApi.ListType.ANIME), String.valueOf(MALApi.ListType.ANIME));
                fragments.add(new IGF().setFriendList(MALApi.ListType.MANGA), String.valueOf(MALApi.ListType.MANGA));
            }
        } else {
            fragments.add(new ProfileDetailsAL(), R.string.tab_name_details);
            fragments.add(new ProfileFriends().setId(0), R.string.tab_name_following);
            if (APIHelper.isNetworkAvailable(activity)) {
                fragments.add(new ProfileFriends().setId(1), R.string.tab_name_followers);
                fragments.add(new ProfileHistory(), R.string.layout_card_title_activity);
                fragments.add(new IGF().setFriendList(MALApi.ListType.ANIME), String.valueOf(MALApi.ListType.ANIME));
                fragments.add(new IGF().setFriendList(MALApi.ListType.MANGA), String.valueOf(MALApi.ListType.MANGA));
            }
        }
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