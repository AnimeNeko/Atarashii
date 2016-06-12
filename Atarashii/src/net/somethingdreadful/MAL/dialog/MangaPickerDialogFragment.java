package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;

public class MangaPickerDialogFragment extends DialogFragment {
    private NumberPicker chapterPicker;
    private NumberPicker volumePicker;

    private View makeNumberPicker() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_manga_picker, null);

        int volumesTotal = ((DetailView) getActivity()).mangaRecord.getVolumes();
        int volumesRead = ((DetailView) getActivity()).mangaRecord.getVolumesRead();
        int chaptersTotal = ((DetailView) getActivity()).mangaRecord.getChapters();
        int chaptersRead = ((DetailView) getActivity()).mangaRecord.getChaptersRead();

        chapterPicker = (NumberPicker) view.findViewById(R.id.chapterPicker);
        volumePicker = (NumberPicker) view.findViewById(R.id.volumePicker);
        chapterPicker.setMinValue(0);
        volumePicker.setMinValue(0);

        if (chaptersTotal != 0) {
            chapterPicker.setMaxValue(chaptersTotal);
        } else {
            chapterPicker.setMaxValue(9999);
        }

        if (volumesTotal != 0) {
            volumePicker.setMaxValue(volumesTotal);
        } else {
            volumePicker.setMaxValue(9999);
        }

        chapterPicker.setValue(chaptersRead);
        volumePicker.setValue(volumesRead);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getTheme());
        builder.setView(makeNumberPicker());
        builder.setTitle(R.string.dialog_title_read_update);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                chapterPicker.clearFocus();
                volumePicker.clearFocus();
                ((DetailView) getActivity()).onMangaDialogDismissed(chapterPicker.getValue(), volumePicker.getValue());
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