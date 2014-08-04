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

public class MangaPickerDialogFragment extends DialogFragment {
    NumberPicker chapterPicker;
    NumberPicker volumePicker;
    int chaptersTotal;
    int chaptersRead;

    int volumesTotal;
    int volumesRead;

    private View makeNumberPicker() {
        View view = getLayoutInflater().inflate(R.layout.dialog_manga_picker);

        volumesTotal = ((DetailView) getActivity()).mangaRecord.getVolumes();
        volumesRead = ((DetailView) getActivity()).mangaRecord.getVolumesRead();
        chaptersTotal = ((DetailView) getActivity()).mangaRecord.getChapters();
        chaptersRead = ((DetailView) getActivity()).mangaRecord.getChaptersRead();

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity(), getTheme());
        builder.setView(makeNumberPicker());
        builder.setTitle(R.string.dialog_title_read_update);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
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