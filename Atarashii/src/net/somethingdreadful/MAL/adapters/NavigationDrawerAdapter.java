package net.somethingdreadful.MAL.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.somethingdreadful.MAL.Home;
import net.somethingdreadful.MAL.NavigationItems;
import net.somethingdreadful.MAL.R;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends ArrayAdapter<NavigationItems.NavItem> {
    private ArrayList<NavigationItems.NavItem> items;
    private Home home;

    public NavigationDrawerAdapter(Home home, ArrayList<NavigationItems.NavItem> items) {
        super(home, R.layout.record_home_navigation, items);
        this.items = items;
        this.home = home;
    }

    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder viewHolder;

        if (v == null) {
            LayoutInflater vi = home.getLayoutInflater();
            v = vi.inflate(R.layout.record_home_navigation, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.mIcon = (ImageView) v.findViewById(R.id.nav_item_icon);
            viewHolder.mTitle = (TextView) v.findViewById(R.id.nav_item_text);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        NavigationItems.NavItem item = items.get(position);
        if (item != null) {
            if (viewHolder.mIcon != null) {
                viewHolder.mIcon.setImageResource(item.icon);
            }
            if (viewHolder.mTitle != null) {
                viewHolder.mTitle.setText(item.title);
            }
        }

        return v;
    }

    static class ViewHolder {
        ImageView mIcon;
        TextView mTitle;
    }
}