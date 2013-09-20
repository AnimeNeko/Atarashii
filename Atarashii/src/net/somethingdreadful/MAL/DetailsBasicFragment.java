package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailsBasicFragment extends Fragment {
    public DetailsBasicFragment() {
    }
    
    private View layout;
    public IDetailsBasicAnimeFragment fragmentInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	layout = inflater.inflate(R.layout.fragment_basicdetails, null);
    	return layout;
    }
    
    @Override
    public void onCreate(Bundle state){
       super.onCreate(state);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	fragmentInterface.basicFragmentReady();
    }
    
    @Override
    public void onAttach(Activity a){
    	super.onAttach(a);
    	fragmentInterface = (IDetailsBasicAnimeFragment) a;
    }
    
    public interface IDetailsBasicAnimeFragment{
    	public void basicFragmentReady();
    }
}
