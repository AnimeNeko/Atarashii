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
import java.util.LinkedHashMap;
import java.util.Map;

public class DetailViewRelationsAdapter extends BaseExpandableListAdapter {
    private Context context;
    ViewHolder viewHolder;
    public Map<String, ArrayList<RecordStub>> list = new LinkedHashMap<String, ArrayList<RecordStub>>();
    public ArrayList<String> headers = new ArrayList<String>();
    public int totalRecords;

    public DetailViewRelationsAdapter(Context context) {
        this.context = context;
    }

    /**
     * Clear all the lists.
     */
    public void clear(){
        list.clear();
        headers.clear();
        totalRecords = 0;
    }

    /**
     * Recalculate the total records amount.
     *
     * @param pos The position of the header that should collapse
     */
    public void collapse(int pos){
        totalRecords = totalRecords - list.get(headers.get(pos)).size();
    }

    /**
     * Recalculate the total records amount.
     *
     * @param pos The position of the header that should expand
     */
    public void expand(int pos){
        totalRecords = totalRecords + list.get(headers.get(pos)).size();
    }

    /**
     * Get the recordStub of a child.
     *
     * @param groupPos The header position
     * @param childPos The child position
     * @return RecordStub the child
     */
    public RecordStub getRecordStub(int groupPos, int childPos) {
        return list.get(headers.get(groupPos)).get(childPos);
    }

    /**
     * Add an item to the list.
     *
     * @param recordStub The record item
     * @param header The text that the headers should use
     */
    public void addRelations(RecordStub recordStub, String header) {
        if (recordStub != null) {
            ArrayList<RecordStub> record = new ArrayList<RecordStub>();
            record.add(recordStub);
            addRelations(record, header);
        }
    }

    /**
     * Add an arraylist of items to the list.
     *
     * @param recordStub The arraylist record items
     * @param header The text that the headers should use
     */
    public void addRelations(ArrayList<RecordStub> recordStub, String header) {
        if (recordStub != null && recordStub.size() != 0) {
            headers.add(header);
            list.put(header, recordStub);
            totalRecords = totalRecords + 1;
        }
    }

    /**
     * Get the object of a child.
     *
     * @param groupPos The header position
     * @param childPos The child position
     * @return Object the child
     */
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

    static class ViewHolder {
        TextView name;
    }
}