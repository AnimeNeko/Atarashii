package net.somethingdreadful.MAL.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.NumberPicker;

public class EpisodesPickerDialogFragment extends DialogFragment {

    NumberPicker numberPicker;

    private View makeNumberPicker() {
        View view = getLayoutInflater().inflate(R.layout.dialog_episode_picker);
        int totalEpisodes = ((DetailView) getActivity()).animeRecord.getEpisodes();
        int watchedEpisodes = ((DetailView) getActivity()).animeRecord.getWatchedEpisodes();

        numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        if (totalEpisodes != 0) {
            numberPicker.setMaxValue(totalEpisodes);
        } else {
            numberPicker.setMaxValue(999);
        }
        numberPicker.setMinValue(0);
        numberPicker.setValue(watchedEpisodes);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity(), getTheme());
        builder.setView(makeNumberPicker());
        builder.setTitle(R.string.dialog_title_watched_update);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DetailView) getActivity()).onDialogDismissed(numberPicker.getValue());
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        });

        return builder.create();
    }
}