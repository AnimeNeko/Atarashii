package net.somethingdreadful.MAL;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class AnimuFragment extends Fragment {
    public AnimuFragment() {
    }

    ArrayList<AnimeRecord> al = new ArrayList();
    GridView gv;
    MALManager mManager;
    Context c;
    CoverAdapter<AnimeRecord> ca;
    IAnimeFragment ready;
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View layout = inflater.inflate(R.layout.fragment_animelist, null);
    	c = layout.getContext();
    	
    	mManager = ((Home) getActivity()).mManager;
    	
    	int orientation = layout.getContext().getResources().getConfiguration().orientation;
    	
    	gv = (GridView) layout.findViewById(R.id.gridview);
    	
    	gv.setOnItemClickListener(new OnItemClickListener()
    	{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
			{
				String animeTitle = ((TextView) v.findViewById(R.id.animeName)).getText().toString();
//				Toast.makeText(getActivity(), animeTitle + " clicked", Toast.LENGTH_SHORT).show();
				
				startActivity(new Intent(getView().getContext(), DetailView.class));
				
			}
    	});
    	
    	if (orientation == layout.getContext().getResources().getConfiguration().ORIENTATION_LANDSCAPE )
    	{
    		gv.setNumColumns(3);
    	}
    	
    	gv.setDrawSelectorOnTop(true);
    	
 //   	gv.setAdapter(new CoverAdapter<String>(layout.getContext(), R.layout.grid_cover_with_text_item, ar));
    	
    	getAnimeRecords(1);
    	
    	ready.fragmentReady();
    	
    	return layout;
    	
    }
    
    public void getAnimeRecords(int listint)
    {
    	new getAnimeRecordsTask().execute(listint);
    	
    }
    
    public class getAnimeRecordsTask extends AsyncTask<Integer, Void, ArrayList<AnimeRecord>>
	{

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected ArrayList<AnimeRecord> doInBackground(Integer... list) {
			
			int listint = 0;
			
			for(int i : list)
			{
				listint = i;
				System.out.println("int passed: " + listint);
			}
			
			al = mManager.getAnimeRecordsFromDB(listint);
			
			if (al == null)
			{
				al = new ArrayList();
				
				JSONObject raw = mManager.getAnimeList();
				
			
				JSONArray jArray;
				try 
				{
					jArray = raw.getJSONArray("anime");
					
					for (int i = 0; i < jArray.length(); i++)
					{
						JSONObject a = jArray.getJSONObject(i);
						
						int id = a.getInt("id");
						String name = a.getString("title");
						int watched = a.getInt("watched_episodes");
						String imageUrl = a.getString("image_url");
						String myStatus = a.getString("watched_status");
						
						AnimeRecord ar = new AnimeRecord(id, name, imageUrl, watched, myStatus);
						
						mManager.initialInsertAnime(ar);
						
//						al.add(ar);
						
					}
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
			
				al = mManager.getAnimeRecordsFromDB(listint);
				
			}
			
			return al;
		}

		@Override
		protected void onPostExecute(ArrayList<AnimeRecord> result) {
			
			if (ca == null)
			{
				ca = new CoverAdapter<AnimeRecord>(c, R.layout.grid_cover_with_text_item, result);
			}
			
			if (gv.getAdapter() == null)
			{
				gv.setAdapter(ca);
			}
			else
			{
				ca.clear();
				ca.addAll(result);
//				new AdapterHelper().update((CoverAdapter<AnimeRecord>) ca, result);
				ca.notifyDataSetChanged();
			}
			
		}

	}
    
    @Override
    public void onCreate(Bundle state)
    {
    	super.onCreate(state);
    	
    	

    }
    
    @Override
    public void onAttach(Activity a)
    {
    	super.onAttach(a);
    	ready = (IAnimeFragment) a;
    	
    }
    
    public interface IAnimeFragment
    {
    	public void fragmentReady();
    }
}