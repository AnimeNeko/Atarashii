package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.api.response.Forum;

import java.util.ArrayList;

public class ForumChildDialogFragment extends DialogFragment {
    ArrayList<Forum> child;
    String message;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        message = getArguments().getString("message");
        child = (ArrayList<Forum>) getArguments().getSerializable("child");

        if (child.size() == 2) {
            message = message.replace("$child1;", child.get(0).getName());
            message = message.replace("$child2;", child.get(1).getName());
        } else {
            message = message.replace("$child1;", child.get(0).getName() + ", " + child.get(1).getName());
            message = message.replace("$child2;", child.get(2).getName());
        }

        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(message);

        builder.setPositiveButton(child.get(0).getName(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ForumActivity) getActivity()).getTopics(child.get(0).getId());
                dismiss();
            }
        });
        builder.setNeutralButton(child.get(1).getName(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ForumActivity) getActivity()).getTopics(child.get(1).getId());
                dismiss();
            }
        });
        if (child.size() == 3) {
            builder.setNegativeButton(child.get(2).getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((ForumActivity) getActivity()).getTopics(child.get(2).getId());
                    dismiss();
                }
            });
        }

        return builder.create();
    }
}