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

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BackupGridviewAdapter<T> extends ArrayAdapter<T> {
    private Context context;
    private ArrayList<File> list;
    private String username;
    private onClickListener onClickListener;

    public BackupGridviewAdapter(Context context, ArrayList<File> list, onClickListener onClickListener) {
        super(context, R.layout.record_friends_gridview);
        this.context = context;
        this.list = list;
        this.onClickListener = onClickListener;
        username = AccountService.getUsername();
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final String fileName = (list.get(position).getName());
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.record_friends_gridview, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) view.findViewById(R.id.userName);
            viewHolder.last_online = (TextView) view.findViewById(R.id.lastonline);
            viewHolder.friends_last = (TextView) view.findViewById(R.id.friends_last);
            viewHolder.avatar = (ImageView) view.findViewById(R.id.profileImg);
            viewHolder.avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_settings_backup_restore_grey));
            viewHolder.removeButton = (ImageView) view.findViewById(R.id.removeButton);
            viewHolder.removeButton.setVisibility(View.VISIBLE);
            viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onRemoveClicked(position);
                }
            });

            if (Theme.darkTheme) {
                viewHolder.username.setTextColor(context.getResources().getColor(R.color.text_dark));
                Theme.setBackground(context, view);
            }

            if (!AccountService.isMAL())
                viewHolder.last_online.setText(context.getString(R.string.unknown));
            viewHolder.friends_last.setText(context.getString(R.string.creation_date));

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        try {
            viewHolder.username.setText(fileName.substring(fileName.indexOf('_') + 1).replace(".json", ""));
            viewHolder.last_online.setText(DateTools.parseDate(Long.parseLong(fileName.substring(6, fileName.indexOf('_'))), true));
            if (!username.equals(viewHolder.username.getText()))
                viewHolder.username.setTextColor(context.getResources().getColor(R.color.card_red));
            else
                viewHolder.username.setTextColor(Theme.darkTheme ? context.getResources().getColor(R.color.text_dark) : Color.BLACK);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "BackupActivity.ListViewAdapter(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return view;
    }

    public void supportAddAll(Collection<? extends T> collection) {
        this.clear();
        list = (ArrayList<File>) collection;
        Collections.reverse(list);
        for (T record : collection) {
            this.add(record);
        }
    }

    /**
     * The interface for callback
     */
    public interface onClickListener {
        void onRemoveClicked(int position);
    }

    static class ViewHolder {
        TextView username;
        TextView last_online;
        ImageView avatar;
        TextView friends_last;
        ImageView removeButton;
    }
}