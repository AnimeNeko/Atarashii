package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.profile.ProfileDetailsAL;
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL;
import net.somethingdreadful.MAL.profile.ProfileFriends;
import net.somethingdreadful.MAL.profile.ProfileHistory;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class ProfilePagerAdapter extends FragmentPagerAdapter {
    private final ProfileActivity activity;
    private ArrayList<FragmentHolder> fragments = new ArrayList<>();

    public ProfilePagerAdapter(FragmentManager fm, ProfileActivity activity) {
        super(fm);
        this.activity = activity;

        if (AccountService.isMAL()) {
            fragments.add(new FragmentHolder(new ProfileDetailsMAL(), R.string.tab_name_details));
            fragments.add(new FragmentHolder(new ProfileFriends(), R.string.tab_name_friends));
            fragments.add(new FragmentHolder(new ProfileHistory(), R.string.tab_name_history));
        } else {
            fragments.add(new FragmentHolder(new ProfileDetailsAL(), R.string.tab_name_details));
            fragments.add(new FragmentHolder(new ProfileFriends().setId(0), R.string.tab_name_following));
            if (APIHelper.isNetworkAvailable(activity))
                fragments.add(new FragmentHolder(new ProfileFriends().setId(1), R.string.tab_name_followers));
            fragments.add(new FragmentHolder(new ProfileHistory(), R.string.layout_card_title_activity));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public String getPageTitle(int position) {
        return fragments.get(position).getName();
    }

    public class FragmentHolder {
        @Getter
        @Setter
        private String name;
        @Getter
        @Setter
        private Fragment fragment;

        public FragmentHolder(Fragment fragment, int name) {
            this.fragment = fragment;
            this.name = activity.getString(name);
        }
    }
}