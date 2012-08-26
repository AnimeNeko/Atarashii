package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

public class EpisodesPickerDialogFragment extends DialogFragment {

	int totalEpisodes;
	int watchedEpisodes;
	int newWwatchedEpisodes;
	
	public EpisodesPickerDialogFragment()
	{
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
	{
		View view = inflater.inflate(R.layout.dialog_episode_picker, container);
		
		if (state == null)
		{
			totalEpisodes = Integer.parseInt(((DetailView) getActivity()).mAr.getTotal());
			watchedEpisodes = Integer.parseInt(((DetailView) getActivity()).mAr.getWatched());
			
		}
		else
		{
			totalEpisodes = state.getInt("totalEpisodes");
			watchedEpisodes = state.getInt("watchedEpisodes");
		}
		
		
		NumberPicker picker = (NumberPicker) view.findViewById(R.id.episodesWatchedPicker);
		
		getDialog().setTitle("I've watched:");
		
		picker.setMinValue(0);
		
		if (totalEpisodes != 0)
		{
			picker.setMaxValue(totalEpisodes);
		}
		else
		{
			picker.setMaxValue(999);
		}
		
		picker.setWrapSelectorWheel(false);
		
		picker.setValue(watchedEpisodes);
		
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		
		state.putInt("totalEpisodes", totalEpisodes);
		state.putInt("watchedEpisodes", watchedEpisodes);
		
		super.onSaveInstanceState(state);
	}
	
	
	
}
