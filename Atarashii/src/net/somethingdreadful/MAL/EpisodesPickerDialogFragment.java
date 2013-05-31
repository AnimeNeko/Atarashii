package net.somethingdreadful.MAL;

import net.simonvt.numberpicker.NumberPicker;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class EpisodesPickerDialogFragment extends SherlockDialogFragment {

    View view;

    NumberPicker picker;

    int totalEpisodes;
    int watchedEpisodes;
    int pickerValue;

    public EpisodesPickerDialogFragment() {

    }

    public interface DialogDismissedListener {
        void onDialogDismissed(int newValue);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v("MALX", "onCreateDialog Fired");

        view = View.inflate(new ContextThemeWrapper(getActivity(), R.style.AlertDialog), R.layout.dialog_episode_picker, null);

        Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DetailView) getActivity()).onDialogDismissed(picker.getValue());
                dismiss();
            }
        }
                ).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dismiss();
                    }
                }
                        ).setView(view).setTitle("I've watched:");

        return builder.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        //        View view = inflater.inflate(R.layout.dialog_episode_picker, container);

        if (state == null) {
            totalEpisodes = Integer.parseInt(((DetailView) getActivity()).animeRecord.getTotal(false));
            watchedEpisodes = ((DetailView) getActivity()).animeRecord.getPersonalProgress(false);
            pickerValue = watchedEpisodes;

        } else {
            totalEpisodes = state.getInt("totalEpisodes");
            watchedEpisodes = state.getInt("watchedEpisodes");
            pickerValue = state.getInt("pickerValue");
        }


        picker = (NumberPicker) view.findViewById(R.id.episodesWatchedPicker);

        //     getDialog().setTitle("I've watched:");

        picker.setMinValue(0);

        if (totalEpisodes != 0) {
            picker.setMaxValue(totalEpisodes);
        } else {
            picker.setMaxValue(999);
        }

        picker.setWrapSelectorWheel(false);

        picker.setValue(pickerValue);
        Log.v("MALX", "onCreateView Finished");
        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {


    }

    @Override
    public void onCancel(DialogInterface dialog) {

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
