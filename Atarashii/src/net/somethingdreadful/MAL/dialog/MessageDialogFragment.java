package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.forum.HtmlList;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;

public class MessageDialogFragment extends DialogFragment implements View.OnClickListener, ForumNetworkTaskFinishedListener {

    EditText title;
    EditText message;
    TextView header;
    ForumJob task;
    int id;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(setView(inflater.inflate(R.layout.dialog_message, null)));

        task = (ForumJob) getArguments().getSerializable("task");
        if (task == ForumJob.ADDTOPIC)
            title.setVisibility(View.VISIBLE);
        else
            title.setVisibility(View.GONE);

        if (task == ForumJob.UPDATECOMMENT)
            header.setText(getString(R.string.dialog_title_edit_comment));
        else if (task == ForumJob.ADDTOPIC)
            header.setText(getString(R.string.dialog_title_add_topic));
        else
            header.setText(getString(R.string.dialog_title_add_comment));

        String message = getArguments().getString("message");
        if (message != null)
            this.message.setText(HtmlList.convertComment(message));

        id = getArguments().getInt("id");

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    private View setView(View view) {
        view.findViewById(R.id.dialog_message_close).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_bold).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_italic).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_underlined).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_striped).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_spoiler).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_center).setOnClickListener(this);
        view.findViewById(R.id.dialog_message_send).setOnClickListener(this);

        header = (TextView) view.findViewById(R.id.dialog_message_header);
        title = (EditText) view.findViewById(R.id.dialog_message_title);
        message = (EditText) view.findViewById(R.id.dialog_message_message);

        return view;
    }

    private void insert(String BBCode) {
        int curPos = message.getSelectionStart();
        String str = message.getText().toString();
        String str1 = str.substring(0, curPos);
        String str2 = str.substring(curPos);
        message.setText(str1 + BBCode +str2);
        message.setSelection(curPos + ((BBCode.length() - 1) / 2));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_message_close:
                if (message.isEnabled())
                    dismiss();
                break;
            case R.id.dialog_message_bold:
                insert("[b][/b]");
                break;
            case R.id.dialog_message_italic:
                insert("[i][/i]");
                break;
            case R.id.dialog_message_underlined:
                insert("[u][/u]");
                break;
            case R.id.dialog_message_striped:
                insert("[s][/s]");
                break;
            case R.id.dialog_message_spoiler:
                insert("[spoiler][/spoiler]");
                break;
            case R.id.dialog_message_center:
                insert("[center][/center]");
                break;
            case R.id.dialog_message_send:
                title.clearFocus();
                message.clearFocus();
                if (task == ForumJob.ADDTOPIC && !message.getText().toString().equals("") && !title.getText().toString().equals(""))
                    new ForumNetworkTask(getActivity(), this, task, id).execute(title.getText().toString(), message.getText().toString());
                else if (!message.getText().toString().equals(""))
                    new ForumNetworkTask(getActivity(), this, task, id).execute(message.getText().toString());
                message.setEnabled(false);
                title.setEnabled(false);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob task) {
        message.setEnabled(true);
        title.setEnabled(true);
        dismiss();
        ((ForumActivity) getActivity()).refresh();
    }
}