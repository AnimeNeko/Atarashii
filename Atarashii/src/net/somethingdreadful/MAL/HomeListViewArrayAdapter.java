package net.somethingdreadful.MAL;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HomeListViewArrayAdapter extends ArrayAdapter<String> {
	Context context;
	int layoutResourceId;
	String data[] = null;
	View mActive;
	View mPrevious;

	public HomeListViewArrayAdapter(Context context, int layoutResourceId,
			String data[]) {
		super(context, layoutResourceId, data);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new Holder();
			holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		String text = data[position];
		holder.txtTitle.setText(text);
		return row;
	}

	static class Holder {
		TextView txtTitle;
	}
}
