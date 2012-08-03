package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CoverAdapter<T> extends ArrayAdapter<T> {
	
	private ArrayList<T> objects;

	public CoverAdapter(Context context, int resource, ArrayList<T> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.objects = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//return super.getView(position, convertView, parent);
		View v = convertView;
		
		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.grid_cover_with_text_item, parent, false);
		}
		
		TextView label = (TextView) v.findViewById(R.id.animeName);
		label.setText(((AnimeRecord) objects.get(position)).getName());

		TextView watchedCount = (TextView) v.findViewById(R.id.watchedCount);
		watchedCount.setText(((AnimeRecord) objects.get(position)).getWatched());
		
//		ImageView icon = (ImageView) v.findViewById(R.id.coverImage);
		
//		icon.setImageResource(R.drawable.icon);

		return v;
	}

}
