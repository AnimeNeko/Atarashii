package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class HomeSectionsPagerAdapter extends FragmentPagerAdapter {

	public HomeSectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment;
		Bundle args = new Bundle();
		
		switch (i) {
		case 0:
			fragment = new ItemGridFragment();
			args.putString("type", "anime");
			break;
		case 1:
			fragment = new ItemGridFragment();
			args.putString("type", "manga");
			break;
		default:
			fragment = new ItemGridFragment();
			args.putString("type", "anime");
			break;
		}
		
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			
			return "Anime".toUpperCase();
		case 1:
			
			return "Manga".toUpperCase();
		}
		return null;
	}
}