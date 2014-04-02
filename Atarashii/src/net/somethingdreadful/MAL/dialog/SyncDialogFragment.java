package net.somethingdreadful.MAL.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;

import net.somethingdreadful.MAL.R;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;

public class SyncDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_sync));
        builder.setMessage(getString(R.string.dialog_message_sync));
        builder.setPositiveButton(R.string.dialog_label_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
                startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS));
            }
        });
        return builder.create();
    }
}