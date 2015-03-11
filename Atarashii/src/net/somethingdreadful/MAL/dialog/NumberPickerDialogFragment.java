package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.R;

public class NumberPickerDialogFragment extends DialogFragment {

    NumberPicker numberPicker;
    private onUpdateClickListener callback;

    private View makeNumberPicker() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_episode_picker, null);
        int max = getInt("max");
        int current = getInt("current");

        numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(max != 0 ? max : 999);
        numberPicker.setMinValue(0);
        numberPicker.setValue(current);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getTheme());
        builder.setView(makeNumberPicker());
        builder.setTitle(getArguments().getString("title"));
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                numberPicker.clearFocus();
                callback.onUpdated(numberPicker.getValue(), getArguments().getInt("id"));
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

    /**
     * Get the integer from an argument.
     *
     * @param key The argument name
     * @return int The number of the argument
     */
    public int getInt(String key) {
        try {
            return getArguments().getInt(key);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "EpisodesPickerDialogFragment.makeNumberPicker(" + key + "): " + e.getMessage());
            return 0;
        }
    }

    /**
     * Set the Callback for update purpose.
     *
     * @param callback The activity/fragment where the callback is located
     * @return NumberPickerDialogFragment This will return the dialog itself to make init simple
     */
    public NumberPickerDialogFragment setOnSendClickListener(onUpdateClickListener callback) {
        this.callback = callback;
        return this;
    }

    /**
     * The interface for callback
     */
    public interface onUpdateClickListener {
        public void onUpdated(int number, int id);
    }
}