package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import net.somethingdreadful.MAL.R;

public class InputDialogFragment extends DialogFragment {
    private onClickListener callback;
    private EditText input;

    public InputDialogFragment setCallback(onClickListener callback) {
        this.callback = callback;
        return this;
    }

    private View createView() {
        View result = getActivity().getLayoutInflater().inflate(R.layout.dialog_update_nav_image, null);
        input = (EditText) result.findViewById(R.id.editText);
        return result;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getTheme());
        builder.setTitle(getArguments().getString("title"));
        builder.setView(createView());
        input.setHint(getArguments().getString("hint"));
        input.setText(getArguments().getString("message"));

        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                input.clearFocus();
                if (!input.getText().toString().equals("")) {
                    callback.onPosInputButtonClicked(input.getText().toString(), getArguments().getInt("id"));
                }
                dismiss();
            }
        });
        builder.setNeutralButton(R.string.dialog_label_remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                input.clearFocus();
                callback.onNegInputButtonClicked("", getArguments().getInt("id"));
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
     * The interface for callback
     */
    public interface onClickListener {
        void onPosInputButtonClicked(String text, int id);

        void onNegInputButtonClicked(String text, int id);
    }
}
