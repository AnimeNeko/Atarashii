package net.somethingdreadful.MAL.dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.SearchActivity;
import net.somethingdreadful.MAL.api.MALApi;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.DialogFragment;

public class SearchIdDialogFragment extends DialogFragment {
    int query;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        query = Integer.parseInt(((SearchActivity) activity).query);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_id_search);
        builder.setMessage(R.string.dialog_message_id_search);

        builder.setPositiveButton(R.string.dialog_label_anime, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startDetails = new Intent(getActivity(), DetailView.class);
                startDetails.putExtra("recordID", query);
                startDetails.putExtra("recordType", MALApi.ListType.ANIME);
                startActivity(startDetails);
                dismiss();
                getActivity().finish();
            }
        });
        builder.setNeutralButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                getActivity().finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_label_manga, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startDetails = new Intent(getActivity(), DetailView.class);
                startDetails.putExtra("recordID", query);
                startDetails.putExtra("recordType", MALApi.ListType.MANGA);
                startActivity(startDetails);
                dismiss();
                getActivity().finish();
            }
        });

        return builder.create();
    }
}