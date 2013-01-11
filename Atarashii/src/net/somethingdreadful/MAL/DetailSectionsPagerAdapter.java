package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DetailSectionsPagerAdapter extends FragmentPagerAdapter {
	
	Context c;

    public DetailSectionsPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        this.c = c;
    }

    @Override
    public Fragment getItem(int i) {
		Fragment fragment;
		Bundle args = new Bundle();
		
		switch (i) {
		case 0:
			fragment = new DetailsBasicFragment();
			break;
		case 1:
			fragment = new DetailsStatsFragment();
			break;
		default:
			fragment = new DetailsBasicFragment();
			break;
		}
		
		fragment.setArguments(args);
		return fragment;
	}

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return c.getString(R.string.title_BasicStuff).toUpperCase();
            case 1: return c.getString(R.string.title_Stats).toUpperCase();
        }
        return null;
    }
}