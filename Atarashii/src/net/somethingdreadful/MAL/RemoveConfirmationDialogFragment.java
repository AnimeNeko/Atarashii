package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class RemoveConfirmationDialogFragment extends SherlockDialogFragment {
	public RemoveConfirmationDialogFragment() {

	}

	public interface RemoveConfirmationDialogListener {
		void onRemoveConfirmed();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
				getActivity(), R.style.AlertDialog));

		builder.setPositiveButton("Remove",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						((DetailView) getActivity()).onRemoveConfirmed();
						dismiss();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dismiss();
							}
						}).setTitle(R.string.dialog_title_remove)
				.setMessage(R.string.dialog_message_remove);

		return builder.create();

	}

	@Override
	public void onDismiss(DialogInterface dialog) {

	}

	@Override
	public void onCancel(DialogInterface dialog) {

		this.dismiss();
	}

}
