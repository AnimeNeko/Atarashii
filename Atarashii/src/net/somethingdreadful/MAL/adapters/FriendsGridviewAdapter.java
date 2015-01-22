package net.somethingdreadful.MAL.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.RoundedTransformation;
import net.somethingdreadful.MAL.api.response.User;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collection;

public class FriendsGridviewAdapter<T> extends ArrayAdapter<T> {
    private Context context;
    private ArrayList<User> list;

    public FriendsGridviewAdapter(Context context, ArrayList<User> list) {
        super(context, R.layout.record_friends_gridview);
        this.context = context;
        this.list = list;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final User record = (list.get(position));
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.record_friends_gridview, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) view.findViewById(R.id.userName);
            viewHolder.last_online = (TextView) view.findViewById(R.id.lastonline);
            viewHolder.avatar = (ImageView) view.findViewById(R.id.profileImg);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        try {
            String username = record.getName();
            viewHolder.username.setText(WordUtils.capitalize(username));
            if (User.isDeveloperRecord(username))
                viewHolder.username.setTextColor(Color.parseColor("#008583")); //Developer

            String last_online = record.getProfile().getDetails().getLastOnline();
            if (last_online != null) {
                last_online = MALDateTools.formatDateString(last_online, context, true);
                viewHolder.last_online.setText(last_online.equals("") ? record.getProfile().getDetails().getLastOnline() : last_online);
            }
            Picasso.with(context).load(record.getProfile().getAvatarUrl())
                    .error(R.drawable.cover_error)
                    .placeholder(R.drawable.cover_loading)
                    .transform(new RoundedTransformation(record.getName()))
                    .into(viewHolder.avatar);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "FriendsActivity.ListViewAdapter(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return view;
    }

    public void supportAddAll(Collection<? extends T> collection) {
        this.clear();
        list = (ArrayList<User>) collection;
        for (T record : collection) {
            this.add(record);
        }
    }

    static class ViewHolder {
        TextView username;
        TextView last_online;
        ImageView avatar;
    }
}