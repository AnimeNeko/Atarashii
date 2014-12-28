package net.somethingdreadful.MAL;

import android.content.Context;
import android.widget.ListView;

import java.util.ArrayList;

public class NavigationItems {

    public ArrayList<NavItem> ITEMS = new ArrayList<NavItem>();

    public NavigationItems(ListView list, Context context) {
        addItem(new NavItem(R.drawable.ic_format_list, R.string.nav_item_my_list));
        addItem(new NavItem(R.drawable.ic_person, R.string.nav_item_my_profile));
        addItem(new NavItem(R.drawable.ic_group, R.string.nav_item_my_friends));
        addItem(new NavItem(R.drawable.ic_forum_grey, R.string.nav_item_my_forum));
        addItem(new NavItem(R.drawable.ic_star, R.string.nav_item_top_rated));
        addItem(new NavItem(R.drawable.ic_insert_chart, R.string.nav_item_most_popular));
        addItem(new NavItem(R.drawable.ic_access_time, R.string.nav_item_just_added));
        addItem(new NavItem(R.drawable.ic_event, R.string.nav_item_upcoming));

        // Calculate the best ListView height and apply it
        list.getLayoutParams().height = (int) (ITEMS.size() * 48 * (context.getResources().getDisplayMetrics().densityDpi / 160f));
    }

    public void addItem(NavItem item) {
        ITEMS.add(item);
    }

    public static class NavItem {
        public int icon;
        public int title;

        public NavItem(int icon, int title) {
            this.icon = icon;
            this.title = title;
        }
    }

}