package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.R;

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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class AnimuFragment extends Fragment {

	// The pixel dimensions used by MAL images
	private static final double MAL_IMAGE_WIDTH = 225;
	private static final double MAL_IMAGE_HEIGHT = 320;
	
	public AnimuFragment() {
    }

    ArrayList<AnimeRecord> al = new ArrayList();
    GridView gv;
    MALManager mManager;
    PrefManager mPrefManager;
    Context c;
    CoverAdapter<AnimeRecord> ca;
    IAnimeFragment Iready;
    boolean forceSyncBool = false;
    int currentList;
    
    @Override
    public void onCreate(Bundle state)
    {
    	super.onCreate(state);
    	
    	if (state != null)
    	{
    		currentList = state.getInt("list", 1);
    	}

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View layout = inflater.inflate(R.layout.fragment_animelist, null);
    	c = layout.getContext();
    	
    	mManager = ((Home) getActivity()).mManager;
    	mPrefManager = ((Home) getActivity()).mPrefManager;
    	
    	if (!((Home) getActivity()).instanceExists)
    	{
    		currentList = mPrefManager.getDefaultList();
    	}

    	
    	int orientation = layout.getContext().getResources().getConfiguration().orientation;
    	
    	gv = (GridView) layout.findViewById(R.id.gridview);
    	
    	gv.setOnItemClickListener(new OnItemClickListener()
    	{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
			{

				startActivity(new Intent(getView().getContext(), DetailView.class)
					.putExtra("net.somethingdreadful.MAL.recordID", ca.getItem(position).recordID));
//				Toast.makeText(c, ca.getItem(position).getID(), Toast.LENGTH_SHORT).show();
			}
    	});
    	
    	int listColumns = (int) Math.ceil(layout.getContext().getResources().getConfiguration().screenWidthDp / MAL_IMAGE_WIDTH);
    	
    	gv.setNumColumns(listColumns);

    	gv.setDrawSelectorOnTop(true);
    	
 //   	gv.setAdapter(new CoverAdapter<String>(layout.getContext(), R.layout.grid_cover_with_text_item, ar));
    	
    	getAnimeRecords(currentList, false);
    	
    	Iready.fragmentReady();
    	
    	return layout;
    	
    }
    
    public void getAnimeRecords(int listint, boolean forceSync)
    {
    	forceSyncBool = forceSync;
    	currentList = listint;
    	
    	new getAnimeRecordsTask().execute(currentList);
    	
    }
    
    public class getAnimeRecordsTask extends AsyncTask<Integer, Void, ArrayList<AnimeRecord>>
	{

		boolean mForceSync = forceSyncBool;
		int mList = currentList;
    	
    	@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected ArrayList<AnimeRecord> doInBackground(Integer... list) {
			
			int listint = 0;
			
			for(int i : list)
			{
				listint = i;
				System.out.println("int passed: " + listint);
			}
			
			if (mForceSync)
			{
				al = new ArrayList();
				
				mManager.downloadAndStoreAnimeList();
				
			}
			
			al = mManager.getAnimeRecordsFromDB(listint);
			
			return al;
		}

		@Override
		protected void onPostExecute(ArrayList<AnimeRecord> result) {
			
			if (result == null)
			{
				result = new ArrayList();
			}
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
			
			if (mForceSync)
			{
				Toast.makeText(c, R.string.toast_SyncDone, Toast.LENGTH_SHORT).show();
			}
			
		}

	}
    
    @Override
    public void onSaveInstanceState(Bundle state)
    {
    	state.putInt("list", currentList);
    	
    	super.onSaveInstanceState(state);
    }
    
    @Override
    public void onAttach(Activity a)
    {
    	super.onAttach(a);
    	Iready = (IAnimeFragment) a;
    	
    }
    
    public interface IAnimeFragment
    {
    	public void fragmentReady();
    }
}