package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class DetailsBasicFragment extends Fragment {
    public DetailsBasicFragment() {
    }
    
    private View layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	layout = inflater.inflate(R.layout.fragment_basicdetails, null);
    	
    	ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
    	if (viewTreeObserver.isAlive()) {
    	  viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
    	    public void onGlobalLayout() {
    	      layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    	      
    	      int synopsisOffset = layout.getHeight();
    	      synopsisOffset -= layout.findViewById(R.id.SynopsisLabel).getHeight();
    	      System.out.println(synopsisOffset);
    	    	
    	    	
    	      LayoutParams params = (LayoutParams) layout.findViewById(R.id.SynopsisLabel).getLayoutParams();
    	      params.setMargins(0, synopsisOffset, 0, 0);
    	      layout.findViewById(R.id.SynopsisLabel).setLayoutParams(params);
    	    	
    	    }
    	  });
    	}
    	
    	return layout;
    }
    
    @Override
    public void onCreate(Bundle state)
    {
       super.onCreate(state);
    }
}