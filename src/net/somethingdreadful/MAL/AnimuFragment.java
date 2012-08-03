package net.somethingdreadful.MAL;

import java.util.ArrayList;

import android.content.Intent;
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

    ArrayList<String> ar = new ArrayList();
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View layout = inflater.inflate(R.layout.fragment_animelist, null);
    	
    	int orientation = layout.getContext().getResources().getConfiguration().orientation;
    	
    	ar.add("Sword Art Online");
    	ar.add("Moar Anime");
    	ar.add("Making the names long");
    	ar.add("A really long anime name here");
    	ar.add("Anime");
    	
    	GridView gv = (GridView) layout.findViewById(R.id.gridview);
    	
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
    	
    	gv.setAdapter(new CoverAdapter<String>(layout.getContext(), R.layout.grid_cover_with_text_item, ar));
    	
    	return layout;
    	
    }
    
    @Override
    public void onCreate(Bundle state)
    {
    	super.onCreate(state);

    }
}