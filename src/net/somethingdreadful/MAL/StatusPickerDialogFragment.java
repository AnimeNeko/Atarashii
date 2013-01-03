package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class StatusPickerDialogFragment extends SherlockDialogFragment {

    View view;



    public StatusPickerDialogFragment()
    {

    }

    public interface StatusDialogDismissedListener
    {
        void onStatusDialogDismissed();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_status_picker, null);

        return new AlertDialog.Builder(getActivity())
        .setPositiveButton("Update", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int whichButton)
            {
                ((DetailView) getActivity()).onStatusDialogDismissed();
                dismiss();
            }
        }
                ).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dismiss();
                    }
                }
                        ).setView(view).setTitle("Status").create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {

        if (state == null)
        {

        }
        else
        {

        }


        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {



    }

    @Override
    public void onCancel(DialogInterface dialog)
    {

        this.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {



        super.onSaveInstanceState(state);
    }



}
