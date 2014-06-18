package net.somethingdreadful.MAL;

import net.simonvt.numberpicker.NumberPicker;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MangaProgressDialogFragment extends DialogFragment {

    View view;

    NumberPicker chapterPicker;
    NumberPicker volumePicker;

    int chaptersTotal;
    int chaptersRead;
    int chapterPickerValue;

    int volumesTotal;
    int volumesRead;
    int volumePickerValue;

    public MangaProgressDialogFragment() {

    }

    public interface MangaDialogDismissedListener {
        void onMangaDialogDismissed(int newChapterValue, int newVolumeValue);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v("MALX", "onCreateDialog Fired");

        view = View.inflate(new ContextThemeWrapper(getActivity(), R.style.AlertDialog), R.layout.dialog_manga_progress, null);

        Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));

        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DetailView) getActivity()).onMangaDialogDismissed(chapterPicker.getValue(), volumePicker.getValue());
                dismiss();
            }
        }
        ).setNegativeButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        }
        ).setView(view).setTitle(R.string.dialog_title_read_update);

        return builder.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {

        if (state == null) {

            chaptersTotal = ((DetailView) getActivity()).mangaRecord.getChapters();
            chaptersRead = ((DetailView) getActivity()).mangaRecord.getChaptersRead();
            chapterPickerValue = chaptersRead;

            volumesTotal = ((DetailView) getActivity()).mangaRecord.getVolumes();
            volumesRead = ((DetailView) getActivity()).mangaRecord.getVolumesRead();
            volumePickerValue = volumesRead;

        } else {

            chaptersTotal = state.getInt("chaptersTotal");
            chaptersRead = state.getInt("chaptersRead");
            chapterPickerValue = state.getInt("chapterPickerValue");

            volumesTotal = state.getInt("volumesTotal");
            volumesRead = state.getInt("volumesRead");
            volumePickerValue = state.getInt("volumePickerValue");
        }


        chapterPicker = (NumberPicker) view.findViewById(R.id.chapterPicker);
        volumePicker = (NumberPicker) view.findViewById(R.id.volumePicker);

        //        getDialog().setTitle("I've read:");

        chapterPicker.setMinValue(0);
        volumePicker.setMinValue(0);

        if (chaptersTotal != 0) {
            chapterPicker.setMaxValue(chaptersTotal);
        } else {
            chapterPicker.setMaxValue(9001);
        }

        if (volumesTotal != 0) {
            volumePicker.setMaxValue(volumesTotal);
        } else {
            volumePicker.setMaxValue(9001);
        }

        chapterPicker.setWrapSelectorWheel(false);
        volumePicker.setWrapSelectorWheel(false);

        chapterPicker.setValue(chapterPickerValue);
        volumePicker.setValue(volumePickerValue);

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

        state.putInt("chaptersTotal", chaptersTotal);
        state.putInt("chaptersRead", chaptersRead);
        state.putInt("chapterPickerValue", chapterPicker.getValue());

        state.putInt("volumesTotal", volumesTotal);
        state.putInt("volumesRead", volumesRead);
        state.putInt("volumePickerValue", volumePicker.getValue());

        super.onSaveInstanceState(state);
    }


}