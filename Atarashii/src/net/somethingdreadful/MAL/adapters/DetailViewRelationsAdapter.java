package net.somethingdreadful.MAL.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.response.RecordStub;

import java.util.ArrayList;
import java.util.Map;

public class DetailViewRelationsAdapter extends BaseExpandableListAdapter {
    Context context;
    ViewHolder viewHolder;
    Map<String, ArrayList<RecordStub>> list;
    ArrayList<String> headers;

    public DetailViewRelationsAdapter(Context context, Map<String, ArrayList<RecordStub>> list, ArrayList<String> headers) {
        this.context = context;
        this.list = list;
        this.headers = headers;
    }

    public Object getChild(int groupPos, int childPos) {
        return list.get(headers.get(groupPos)).get(childPos);
    }

    public long getChildId(int groupPos, int childPos) {
        return childPos;
    }

    public View getChildView(final int groupPos, final int childPos, boolean isLastChild, View convertView, ViewGroup parent) {
        viewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.record_details_listview, parent, false);

            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(getRecordStub(groupPos, childPos).getTitle());
        return convertView;
    }

    public int getChildrenCount(int groupPos) {
        return list.get(headers.get(groupPos)).size();
    }

    public Object getGroup(int groupPos) {
        return headers.get(groupPos);
    }

    public int getGroupCount() {
        return headers.size();
    }

    public long getGroupId(int groupPos) {
        return groupPos;
    }

    public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.record_details_listview_header, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText(headers.get(groupPos));
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPos, int childPos) {
        return true;
    }

    public RecordStub getRecordStub(int groupPos, int childPos) {
        return list.get(headers.get(groupPos)).get(childPos);
    }

    static class ViewHolder {
        TextView name;
    }
}