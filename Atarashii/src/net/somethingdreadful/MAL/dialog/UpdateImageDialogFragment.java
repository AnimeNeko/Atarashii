package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;

public class UpdateImageDialogFragment extends DialogFragment {
    EditText input;

    private View createView() {
        View result = getActivity().getLayoutInflater().inflate(R.layout.dialog_update_nav_image, null);
        input = (EditText) result.findViewById(R.id.editText);
        return result;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getTheme());
        builder.setTitle(R.string.dialog_title_update_navigation);
        builder.setView(createView());

        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                input.clearFocus();
                if (!input.getText().toString().equals("")) {
                    Picasso.with(getActivity())
                            .load(input.getText().toString())
                            .into((ImageView) getActivity().findViewById(R.id.NDimage));
                    PrefManager pref = new PrefManager(getActivity());
                    pref.setNavigationBackground(input.getText().toString());
                    pref.commitChanges();
                }
                dismiss();
            }
        });
        builder.setNeutralButton(R.string.dialog_label_remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                Picasso.with(getActivity())
                        .load(R.drawable.atarashii_background)
                        .into((ImageView) getActivity().findViewById(R.id.NDimage));
                PrefManager pref = new PrefManager(getActivity());
                pref.setNavigationBackground(null);
                pref.commitChanges();
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
