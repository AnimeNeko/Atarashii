package net.somethingdreadful.MAL;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

public class EpisodesPickerDialogFragment extends DialogFragment {

	NumberPicker picker;
	
	int totalEpisodes;
	int watchedEpisodes;
	int pickerValue;
	
	public EpisodesPickerDialogFragment()
	{
		
	}
	
	public interface DialogDismissedListener
	{
		void onDialogDismissed(int newValue);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
	{
		View view = inflater.inflate(R.layout.dialog_episode_picker, container);
		
		if (state == null)
		{
			totalEpisodes = Integer.parseInt(((DetailView) getActivity()).mAr.getTotal());
			watchedEpisodes = Integer.parseInt(((DetailView) getActivity()).mAr.getWatched());
			pickerValue = watchedEpisodes;
			
		}
		else
		{
			totalEpisodes = state.getInt("totalEpisodes");
			watchedEpisodes = state.getInt("watchedEpisodes");
			pickerValue = state.getInt("pickerValue");
		}
		
		
		picker = (NumberPicker) view.findViewById(R.id.episodesWatchedPicker);
		
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
		
		picker.setValue(pickerValue);
		
		return view;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog)
	{
		
		

	}
	
	public void onCancel(DialogInterface dialog)
	{
		((DetailView) getActivity()).onDialogDismissed(picker.getValue());
		this.dismiss();
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		
		state.putInt("totalEpisodes", totalEpisodes);
		state.putInt("watchedEpisodes", watchedEpisodes);
		state.putInt("pickerValue", picker.getValue());
		
		super.onSaveInstanceState(state);
	}
	
	
	
}
