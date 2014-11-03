package net.somethingdreadful.MAL.dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.SearchActivity;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.DialogFragment;

public class SearchIdDialogFragment extends DialogFragment {

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_id_search);
        builder.setMessage(R.string.dialog_message_id_search);

        final Integer query = Integer.parseInt(((SearchActivity) getActivity()).query);

        builder.setPositiveButton(R.string.dialog_label_anime, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Anime record = new Anime();
                record.setId(query);
                Intent startDetails = new Intent(getActivity(), DetailView.class);
                startDetails.putExtra("record", record);
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
                Manga record = new Manga();
                record.setId(query);
                Intent startDetails = new Intent(getActivity(), DetailView.class);
                startDetails.putExtra("record", record);
                startDetails.putExtra("recordType", MALApi.ListType.MANGA);
                startActivity(startDetails);
                dismiss();
                getActivity().finish();
            }
        });

        return builder.create();
    }
}