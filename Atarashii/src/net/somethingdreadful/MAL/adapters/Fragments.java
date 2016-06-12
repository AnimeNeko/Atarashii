package net.somethingdreadful.MAL.adapters;


import android.app.Activity;
import android.app.Fragment;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

class Fragments {
    private final ArrayList<FragmentHolder> fragments = new ArrayList<>();
    private final Activity activity;

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

    /**
     * Get the amount of Fragments stored.
     *
     * @return Int the amount
     */
    public int getSize() {
        return fragments.size();
    }

    /**
     * Get the fragment by the given position.
     *
     * @param position The fragment position
     * @return Fragment The fragment which was stored
     */
    public Fragment getFragment(int position) {
        return fragments.get(position).getFragment();
    }

    /**
     * Get the fragment name by the given position.
     *
     * @param position The position of the fragment
     * @return String The name as shown in the tabs
     */
    public String getName(int position) {
        return fragments.get(position).getName();
    }

    /**
     * Remove all fragments.
     */
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
