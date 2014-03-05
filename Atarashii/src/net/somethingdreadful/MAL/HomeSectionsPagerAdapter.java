package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.api.MALApi;
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

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return MALApi.getListTypeString(getTag(position)).toUpperCase();
	}

	public MALApi.ListType getTag(int position) {
		switch (position) {
		case 0:
			return MALApi.ListType.ANIME;
		case 1:
			return MALApi.ListType.MANGA;
		default:
			return null;
		}
	}
}