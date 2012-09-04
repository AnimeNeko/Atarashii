package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.List;

import net.somethingdreadful.MAL.R;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CoverAdapter<T> extends ArrayAdapter<T> {
	
	private ArrayList<T> objects;
	private ImageDownloader imageManager;
	private Context c;
	
	private int dp64;
	private int dp6;
	private int dp8;
	private int dp12;
	private int dp32;

	public CoverAdapter(Context context, int resource, ArrayList<T> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.objects = objects;
		this.c = context;
		imageManager = new ImageDownloader(c);
		
		dp64 = dpToPx(64);
		dp32 = dpToPx(32);
		dp12 = dpToPx(12);
		dp6 = dpToPx(6);
		dp8 = dpToPx(8);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//return super.getView(position, convertView, parent);
		View v = convertView;
		GenericMALRecord a = ((GenericMALRecord) objects.get(position));
		
		String myStatus = a.getMyStatus();
		
		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.grid_cover_with_text_item, parent, false);
		}
		
		
		
		TextView label = (TextView) v.findViewById(R.id.animeName);
		label.setText(a.getName());

		TextView watchedCount = (TextView) v.findViewById(R.id.watchedCount);
		watchedCount.setText(Integer.toString(a.getPersonalProgress()));
		
		ImageView cover = (ImageView) v.findViewById(R.id.coverImage);
		imageManager.download(a.getImageUrl(), cover);
		
		TextView flavourText = (TextView) v.findViewById(R.id.stringWatched);
		if ("watching".equals(myStatus))
		{
			flavourText.setText(R.string.cover_Watching);

			watchedCount.setVisibility(watchedCount.VISIBLE);
			
		}
		if ("completed".equals(myStatus))
		{
			flavourText.setText(R.string.cover_Completed);

			watchedCount.setVisibility(watchedCount.VISIBLE);
			
		}
		if ("on-hold".equals(myStatus))
		{
			flavourText.setText(R.string.cover_OnHold);

			watchedCount.setVisibility(watchedCount.VISIBLE);
			
		}
		if ("dropped".equals(myStatus))
		{
			flavourText.setText(R.string.cover_Dropped);

			watchedCount.setVisibility(watchedCount.INVISIBLE);

		}
		if ("plan to watch".equals(myStatus))
		{
			flavourText.setText(R.string.cover_PlanningToWatch);

			watchedCount.setVisibility(watchedCount.INVISIBLE);

		}
		
//		icon.setImageResource(R.drawable.icon);

		return v;
	}
	
	public int dpToPx(float dp){
	    Resources resources = c.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return (int) px;
	}

}
