package net.somethingdreadful.MAL.adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.dialog.InformationDialogFragment;
import net.somethingdreadful.MAL.tasks.ForumJob;

import java.util.Collection;

public class ForumMainAdapter<T> extends ArrayAdapter<T> {
    private Context context;
    private FragmentManager fm;
    private ListView listview;
    private ForumJob task;

    public ForumMainAdapter(Activity context, ListView listview, FragmentManager fm, ForumJob task) {
        super(context, R.layout.record_forum_listview);
        this.context = context;
        this.listview = listview;
        this.fm = fm;
        this.task = task;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final Forum record = ((Forum) this.getItem(position));
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.record_forum_listview, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            viewHolder.image = (ImageView) view.findViewById(R.id.Image);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        try {
            viewHolder.title.setText(record.getName());
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InformationDialogFragment info = new InformationDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("title", record.getName());
                    if (task == ForumJob.BOARD)
                        args.putString("message", record.getDescription());
                    else
                        args.putString("message", context.getString(R.string.dialog_message_created_by)
                                + " " + record.getUsername()
                                + "\n"
                                + context.getString(R.string.dialog_message_last_post)
                                + " " + record.getReply().getUsername()
                                + " " + context.getString(R.string.dialog_message_on)
                                + " " + MALDateTools.formatDateString(record.getReply().getTime(), context, true));
                    info.setArguments(args);
                    info.show(fm, "fragment_forum");

                }
            });
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "ForumActivity.ListViewAdapter(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return view;
    }

    public void supportAddAll(Collection<? extends T> collection) {
        if (task == ForumJob.BOARD)
            listview.getLayoutParams().height = (int) (((48 + 1) * collection.size() - 1) * (context.getResources().getDisplayMetrics().densityDpi / 160f));
        for (T record : collection) {
            this.add(record);
        }
    }

    static class ViewHolder {
        TextView title;
        ImageView image;
    }
}