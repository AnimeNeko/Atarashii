package net.somethingdreadful.MAL.adapters;


import android.app.Activity;
import android.app.Fragment;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Fragments {
    private ArrayList<FragmentHolder> fragments = new ArrayList<>();
    Activity activity;

    Fragments(Activity activity) {
        this.activity = activity;
    }

    /**
     * Add fragments to the holder.
     *
     * @param fragment The fragment which should be added
     * @param name The fragment name res ID shown in the tabs
     */
    public void add(Fragment fragment, int name) {
        add(fragment, activity.getString(name));
    }

    /**
     * Add fragments to the holder.
     *
     * @param fragment The fragment which should be added
     * @param name The fragment String name shown in the tabs
     */
    public void add(Fragment fragment, String name) {
        fragments.add(new FragmentHolder(fragment, name));
    }

    public int getSize() {
        return fragments.size();
    }

    public Fragment getFragment(int position) {
        return fragments.get(position).getFragment();
    }

    public String getName(int position) {
        return fragments.get(position).getName();
    }

    public void clear() {
        fragments.clear();
    }

    public class FragmentHolder {
        @Getter
        @Setter
        private String name;
        @Getter
        @Setter
        private Fragment fragment;

        public FragmentHolder(Fragment fragment, String name) {
            this.fragment = fragment;
            this.name = name;
        }
    }
}
