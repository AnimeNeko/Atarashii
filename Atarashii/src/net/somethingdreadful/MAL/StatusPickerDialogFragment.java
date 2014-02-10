package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;
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
                currentStatus = ((DetailView) getActivity()).animeRecord.getWatchedStatus();
            } else {
                currentStatus = ((DetailView) getActivity()).mangaRecord.getReadStatus();
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
                                    currentStatus = Anime.STATUS_WATCHING;
                                } else {
                                    currentStatus = Manga.STATUS_READING;
                                }
                                break;

                            case R.id.statusRadio_Completed:
                                currentStatus = GenericRecord.STATUS_COMPLETED;
                                break;

                            case R.id.statusRadio_OnHold:
                                currentStatus = GenericRecord.STATUS_ONHOLD;
                                break;

                            case R.id.statusRadio_Dropped:
                                currentStatus = GenericRecord.STATUS_DROPPED;
                                break;

                            case R.id.statusRadio_Planned:
                                if ("anime".equals(type)) {
                                    currentStatus = Anime.STATUS_PLANTOWATCH;
                                } else {
                                    currentStatus = Manga.STATUS_PLANTOREAD;
                                }
                                break;
                        }
                    }
                });

        if ((Anime.STATUS_WATCHING.equals(currentStatus)) || (Manga.STATUS_READING.equals(currentStatus))) {
            statusGroup.check(R.id.statusRadio_InProgress);
        }
        if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
            statusGroup.check(R.id.statusRadio_Completed);
        }
        if (GenericRecord.STATUS_ONHOLD.equals(currentStatus)) {
            statusGroup.check(R.id.statusRadio_OnHold);
        }
        if (GenericRecord.STATUS_DROPPED.equals(currentStatus)) {
            statusGroup.check(R.id.statusRadio_Dropped);
        }
        if ((Anime.STATUS_PLANTOWATCH.equals(currentStatus)) || (Manga.STATUS_PLANTOREAD.equals(currentStatus))) {
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
