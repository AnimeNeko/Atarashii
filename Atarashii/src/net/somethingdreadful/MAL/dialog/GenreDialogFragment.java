package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import net.somethingdreadful.MAL.R;

import java.util.ArrayList;

public class GenreDialogFragment extends DialogFragment implements DialogInterface.OnMultiChoiceClickListener {
    private onUpdateClickListener callback;
    private ArrayList<String> array = new ArrayList<>();
    private String[] resArray;

    @Override
    public Dialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        resArray = getResources().getStringArray(R.array.genresArray);
        builder.setTitle(getResources().getString(R.string.card_content_genres));
        builder.setMultiChoiceItems(resArray, (new boolean[resArray.length]), this);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                callback.onUpdated(array, getArguments().getInt("id"));
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
    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
        if (b)
            array.add(resArray[i]);
        else
            array.remove(resArray[i]);
    }

    /**
     * The interface for callback
     */
    public interface onUpdateClickListener {
        void onUpdated(ArrayList<String> result, int id);
    }

    /**
     * Set the Callback for update purpose.
     *
     * @param callback The activity/fragment where the callback is located
     * @return ListDialogFragment This will return the dialog itself to make init simple
     */
    public GenreDialogFragment setOnSendClickListener(onUpdateClickListener callback) {
        this.callback = callback;
        return this;
    }
}