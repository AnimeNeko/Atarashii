package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import net.somethingdreadful.MAL.R;

public class ChooseDialogFragment extends DialogFragment {
    private onClickListener callback;

    @Override
    public AlertDialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("message"));
        builder.setNegativeButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        });
        builder.setPositiveButton(getArguments().getString("positive"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                callback.onPositiveButtonClicked();
                dismiss();
            }
        });
        return builder.create();
    }

    public void setCallback(onClickListener callback) {
        this.callback = callback;
    }

    /**
     * The interface for callback
     */
    public interface onClickListener {
        void onPositiveButtonClicked();
    }
}