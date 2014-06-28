package net.somethingdreadful.MAL;

import java.util.ArrayList;

public class NavigationItems {

	public ArrayList<NavItem> ITEMS = new ArrayList<NavItem>();

	public NavigationItems() {
		addItem(new NavItem(R.drawable.ic_profile, R.string.nav_item_my_profile));
		addItem(new NavItem(R.drawable.ic_list, R.string.nav_item_my_list));
		addItem(new NavItem(R.drawable.ic_friends, R.string.nav_item_my_friends));
		addItem(new NavItem(R.drawable.ic_star, R.string.nav_item_top_rated));
		addItem(new NavItem(R.drawable.ic_popular,R.string.nav_item_most_popular));
		addItem(new NavItem(R.drawable.ic_recent, R.string.nav_item_just_added));
		addItem(new NavItem(R.drawable.ic_upcoming, R.string.nav_item_upcoming));
	}

	public static class NavItem {
		public int icon;
		public int title;

		public NavItem(int icon, int title) {
			this.icon = icon;
			this.title = title;
		}
	}

	public void addItem(NavItem item) {
		ITEMS.add(item);
	}

}