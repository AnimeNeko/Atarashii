package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;

public class ShareDialogFragment extends DialogFragment {
    String title;
    boolean share;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getTheme());
        final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        title = getArguments().getString("title");
        share = getArguments().getBoolean("share");

        if (share) {
            builder.setTitle(R.string.dialog_title_share);
            builder.setMessage(R.string.dialog_message_share);
            sharingIntent.setType("text/plain");
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else {
            builder.setTitle(R.string.dialog_title_view);
            builder.setMessage(R.string.dialog_message_view);
        }

        builder.setPositiveButton(R.string.dialog_label_animelist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (share) {
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_animelist)
                            .replace("$name;", title)
                            .replace("$username;", AccountService.getUsername()));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)));
                } else {
                    Uri mallisturlanime = Uri.parse("http://myanimelist.net/animelist/" + title);
                    startActivity(new Intent(Intent.ACTION_VIEW, mallisturlanime));
                }
            }
        });
        builder.setNeutralButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.dialog_label_mangalist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (share) {
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_mangalist)
                            .replace("$name;", title)
                            .replace("$username;", AccountService.getUsername()));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)));
                } else {
                    Uri mallisturlmanga = Uri.parse("http://myanimelist.net/mangalist/" + title);
                    startActivity(new Intent(Intent.ACTION_VIEW, mallisturlmanga));
                }
            }
        });

        return builder.create();
    }
}