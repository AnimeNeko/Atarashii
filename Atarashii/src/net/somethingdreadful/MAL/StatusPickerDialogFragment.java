package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import com.actionbarsherlock.app.SherlockDialogFragment;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.GenericMALRecord;
import net.somethingdreadful.MAL.record.MangaRecord;

public class StatusPickerDialogFragment extends SherlockDialogFragment {

    View view;

    String type;
    String currentStatus;

    RadioGroup statusGroup;


    public StatusPickerDialogFragment() {

    }

    public interface StatusDialogDismissedListener {
        void onStatusDialogDismissed(String status);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        view = View.inflate(new ContextThemeWrapper(getActivity(), R.style.AlertDialog), R.layout.dialog_status_picker, null);

        Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DetailView) getActivity()).onStatusDialogDismissed(currentStatus);
                dismiss();
            }
        }
        ).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        }
        ).setView(view).setTitle("Status");

        return builder.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {

        if (state == null) {
            type = ((DetailView) getActivity()).recordType;

            if ("anime".equals(type)) {
                currentStatus = ((DetailView) getActivity()).mAr.getMyStatus();
            } else {
                currentStatus = ((DetailView) getActivity()).mMr.getMyStatus();
            }
        } else {
            type = state.getString("type");
            currentStatus = state.getString("status");
        }

        statusGroup = (RadioGroup) view.findViewById(R.id.statusRadioGroup);

        statusGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.statusRadio_InProgress:
                                if ("anime".equals(type)) {
                                    currentStatus = AnimeRecord.STATUS_WATCHING;
                                } else {
                                    currentStatus = MangaRecord.STATUS_WATCHING;
                                }
                                break;

                            case R.id.statusRadio_Completed:
                                currentStatus = GenericMALRecord.STATUS_COMPLETED;
                                break;

                            case R.id.statusRadio_OnHold:
                                currentStatus = GenericMALRecord.STATUS_ONHOLD;
                                break;

                            case R.id.statusRadio_Dropped:
                                currentStatus = GenericMALRecord.STATUS_DROPPED;
                                break;

                            case R.id.statusRadio_Planned:
                                if ("anime".equals(type)) {
                                    currentStatus = AnimeRecord.STATUS_PLANTOWATCH;
                                } else {
                                    currentStatus = MangaRecord.STATUS_PLANTOWATCH;
                                }
                                break;
                        }
                    }
                });

        if ((AnimeRecord.STATUS_WATCHING.equals(currentStatus)) || (MangaRecord.STATUS_WATCHING.equals(currentStatus))) {
            statusGroup.check(R.id.statusRadio_InProgress);
        }
        if (GenericMALRecord.STATUS_COMPLETED.equals(currentStatus)) {
            statusGroup.check(R.id.statusRadio_Completed);
        }
        if (GenericMALRecord.STATUS_ONHOLD.equals(currentStatus)) {
            statusGroup.check(R.id.statusRadio_OnHold);
        }
        if (GenericMALRecord.STATUS_DROPPED.equals(currentStatus)) {
            statusGroup.check(R.id.statusRadio_Dropped);
        }
        if ((AnimeRecord.STATUS_PLANTOWATCH.equals(currentStatus)) || (MangaRecord.STATUS_PLANTOWATCH.equals(currentStatus))) {
            statusGroup.check(R.id.statusRadio_Planned);
        }
        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {


    }

    @Override
    public void onCancel(DialogInterface dialog) {

        this.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {

        state.putString("type", type);
        state.putString("status", currentStatus);

        super.onSaveInstanceState(state);
    }


}
