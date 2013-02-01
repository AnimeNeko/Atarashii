package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class LogoutConfirmationDialogFragment extends SherlockDialogFragment {
    public LogoutConfirmationDialogFragment() {}

    public interface LogoutConfirmationDialogListener {
        void onLogoutConfirmed();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));

        builder.setPositiveButton(R.string.dialog_logout_label_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((Home) getActivity()).onLogoutConfirmed();
                dismiss();
            }
        })
        .setNegativeButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        })
        .setTitle(R.string.dialog_logout_title).setMessage(R.string.dialog_logout_message);

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {}

    @Override
    public void onCancel(DialogInterface dialog) {
        //      startActivity(new Intent(getActivity(), Home.class)); //Relaunching Home without needing to, causes bad things
        this.dismiss();
    }

}
