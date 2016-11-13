package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import net.somethingdreadful.MAL.R;

import java.util.ArrayList;

public class CustomListDialogFragment extends DialogFragment implements DialogInterface.OnMultiChoiceClickListener {
    private onUpdateClickListener callback;
    private ArrayList<String> array = new ArrayList<>();
    private String[] resArray;

    @Override
    public Dialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        resArray = getArguments().getStringArray("all");
        boolean[] checkedItems = new boolean[resArray.length];
        if (getArguments().containsKey("current")) {
            array = getArguments().getStringArrayList("current");
            for (int n = 0; n < resArray.length; n++) {
                checkedItems[n] = array.contains(resArray[n]);
            }
        }
        builder.setTitle(getResources().getString(R.string.card_content_genres));
        builder.setMultiChoiceItems(resArray, checkedItems, this);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = "";
                for (String aResArray : resArray) {
                    result = result + (array.contains(aResArray) ? 1 : 0);
                }
                for (int i = result.length(); i < 15; i++) {
                    result = result + "0";
                }
                callback.onUpdated(result, getArguments().getInt("id"));
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
        void onUpdated(String result, int id);
    }

    /**
     * Set the Callback for update purpose.
     *
     * @param callback The activity/fragment where the callback is located
     * @return ListDialogFragment This will return the dialog itself to make init simple
     */
    public CustomListDialogFragment setOnSendClickListener(onUpdateClickListener callback) {
        this.callback = callback;
        return this;
    }
}