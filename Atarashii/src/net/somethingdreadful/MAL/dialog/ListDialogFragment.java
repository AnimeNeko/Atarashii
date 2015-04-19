package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import net.somethingdreadful.MAL.R;

public class ListDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    int[] array;
    int selectedItem = -1;
    private onUpdateClickListener callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        array = getResources().getIntArray(getArguments().getInt("intArray"));
        builder.setSingleChoiceItems(getArguments().getInt("stringArray"), getArguments().getInt("current"), this);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (selectedItem != -1)
                    callback.onUpdated(selectedItem, getArguments().getInt("id"));
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

    @Override
    public void onClick(DialogInterface dialog, int which) {
        selectedItem = array[which];
    }

    /**
     * The interface for callback
     */
    public interface onUpdateClickListener {
        public void onUpdated(int number, int id);
    }

    /**
     * Set the Callback for update purpose.
     *
     * @param callback The activity/fragment where the callback is located
     * @return ListDialogFragment This will return the dialog itself to make init simple
     */
    public ListDialogFragment setOnSendClickListener(onUpdateClickListener callback) {
        this.callback = callback;
        return this;
    }
}