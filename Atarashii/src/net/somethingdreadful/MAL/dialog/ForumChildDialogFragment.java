package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Forum;

import java.util.ArrayList;

public class ForumChildDialogFragment extends DialogFragment {
    ArrayList<Forum> child;
    String message;
    public static boolean DBModificationRequest;

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

        return builder.create();
    }
}