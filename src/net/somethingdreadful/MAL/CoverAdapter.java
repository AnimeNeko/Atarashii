package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import net.somethingdreadful.MAL.R;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class CoverAdapter<T> extends ArrayAdapter<T> {
	
	private ArrayList<T> objects;
	private ImageDownloader imageManager;
	private Context c;
	private MALManager mManager;
	private String type;
	
	private int dp64;
	private int dp6;
	private int dp8;
	private int dp12;
	private int dp32;

	public CoverAdapter(Context context, int resource, ArrayList<T> objects, MALManager m, String type) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.objects = objects;
		this.c = context;
		imageManager = new ImageDownloader(c);
		mManager = m;
		this.type = type;
		
		
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
		final GenericMALRecord a;
		
		a = ((GenericMALRecord) objects.get(position));
		
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
		
		ImageView popUpButton = (ImageView) v.findViewById(R.id.popUpButton);
		
		if ((a.getMyStatus().equals(AnimeRecord.STATUS_WATCHING)) || (a.getMyStatus().equals(MangaRecord.STATUS_WATCHING)))
		{
			popUpButton.setVisibility(popUpButton.VISIBLE);
//			System.out.println("true");
			
			popUpButton.setOnClickListener(
					new OnClickListener()
					{
						
						public void onClick(View v) 
						{
//							Toast.makeText(c, a.getName(), Toast.LENGTH_SHORT).show();
							showPopupMenu(v);
						}
						
						private void showPopupMenu(View v) {
							PopupMenu pm = new PopupMenu(c, v);
							
							if (a.getMyStatus().equals(AnimeRecord.STATUS_WATCHING))
							{
								pm.getMenuInflater().inflate(R.menu.cover_action_menu, pm.getMenu());
							}
							if (a.getMyStatus().equals(MangaRecord.STATUS_WATCHING))
							{
								pm.getMenuInflater().inflate(R.menu.cover_action_menu_manga, pm.getMenu());
							}
								
							pm.setOnMenuItemClickListener(
									new OnMenuItemClickListener() 
									{
										public boolean onMenuItemClick(MenuItem item)
										{
											switch (item.getItemId())
											{
												case R.id.action_PlusOneWatched:
//													Toast.makeText(c, "+1: " + a.getName(), Toast.LENGTH_SHORT).show();
//													((GenericMALRecord) objects.get(mposition))
//													.setPersonalProgress(a.getPersonalProgress() + 1);
//													
//													notifyDataSetChanged();
//													
													setProgressPlusOne(a);
													break;
											}
											
											return true;
										}
									});
							
							pm.show();
							
						}
						
					});
		}
		else
		{
			popUpButton.setVisibility(popUpButton.INVISIBLE);
		}
		
		
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
	
	public void setProgressPlusOne(GenericMALRecord gr)
	{
		gr.setPersonalProgress(gr.getPersonalProgress() + 1);
		
		notifyDataSetChanged();
		
		new writeDetailsTask().execute(gr);
	}
	
	public class writeDetailsTask extends AsyncTask<GenericMALRecord, Void, Boolean>
	{

		MALManager internalManager;
		GenericMALRecord internalGr;
		String internalType;
		
		@Override
		protected void onPreExecute()
		{
			internalManager = mManager;
			internalType = type;
		}
		
		
		@Override
		protected Boolean doInBackground(GenericMALRecord... gr) {
			
			boolean result;
			
			if ("anime".equals(internalType))
			{
				internalManager.saveItem((AnimeRecord) gr[0], false);
				result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_ANIME);
			}
			else
			{
				internalManager.saveItem((MangaRecord) gr[0], false);
				result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_MANGA);
			}
			
			
			if (result == true)
			{
				gr[0].setDirty(gr[0].CLEAN);
				
				if ("anime".equals(internalType))
				{
					internalManager.saveItem((AnimeRecord) gr[0], false);
				}
				else
				{
					internalManager.saveItem((MangaRecord) gr[0], false);
				}
			}
			
			return result;
			

		}
		
	}
	
	public int dpToPx(float dp){
	    Resources resources = c.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return (int) px;
	}

}
