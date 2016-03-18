package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.forum.HtmlUtil;
import net.somethingdreadful.MAL.tasks.ForumJob;

public class MessageDialogFragment extends DialogFragment implements View.OnClickListener, View.OnLongClickListener {
    private EditText message;
    private TextView header;
    private ForumJob task;
    private int id;
    private View view;
    private onSendClickListener callback;
    private TextView send;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(setView(inflater.inflate(R.layout.dialog_message, null)));

        task = (ForumJob) getArguments().getSerializable("task");
        String hint = getArguments().getString("hint", null);
        String message = getArguments().getString("message", null);
        String title = getArguments().getString("title", null);
        id = getArguments().getInt("id");

        setHeader(title);
        setClickListener();

        if (task == null)
            send.setText(getString(R.string.dialog_label_update));
        if (message != null)
            this.message.setText((new HtmlUtil(getActivity())).convertMALComment(message));
        if (hint != null)
            this.message.setHint(hint);
        this.message.setSelection(this.message.getText().length());

        Dialog dialog = builder.create();
        DisplayMetrics dm = new DisplayMetrics();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.message.setMaxHeight(dm.heightPixels / 3); //fill only 30% of the screen with the message

        return dialog;
    }

    /**
     * Changes the header title depending on the arguments and parameters.
     *
     * @param title The title (can be null)
     */
    private void setHeader(String title) {
        if (title != null)
            header.setText(title);
        else
            switch (task) {
                default:
                    header.setText(getString(R.string.dialog_title_add_comment));
                    break;
            }
    }

    /**
     * Set all the required variables.
     *
     * @param view The parent view
     * @return View The view to make init simple
     */
    private View setView(View view) {
        Theme.setBackground(getActivity(), view);

        header = (TextView) view.findViewById(R.id.dialog_message_header);
        send = (TextView) view.findViewById(R.id.dialog_message_send);
        message = (EditText) view.findViewById(R.id.dialog_message_message);

        Theme.setBackground(getActivity(), send);
        view.findViewById(R.id.dialog_message_close).setOnLongClickListener(this);

        this.view = view;
        return view;
    }

    /**
     * Add all the onClickListener events.
     */
    private void setClickListener() {
        view.findViewById(R.id.dialog_message_close).setOnClickListener(this);
        send.setOnClickListener(this);
    }

    /**
     * Handle all the click events.
     *
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_message_close:
                if (callback != null) {
                    message.clearFocus();
                    callback.onCloseClicked(message.getText().toString());
                }
                if (message.isEnabled())
                    dismiss();
                break;
            case R.id.dialog_message_send:
                message.clearFocus();
                callback.onSendClicked(message.getText().toString(), id);
                dismiss();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        message.setText("");
        return true;
    }

    /**
     * The interface for callback
     */
    public interface onSendClickListener {
        void onSendClicked(String message, int id);

        void onCloseClicked(String message);
    }

    /**
     * Set the Callback for update/send purpose.
     *
     * @param callback The activity/fragment where the callback is located
     * @return MessageDialogFragment This will return the dialog itself to make init simple
     */
    public MessageDialogFragment setOnSendClickListener(onSendClickListener callback) {
        this.callback = callback;
        return this;
    }

    /**
     * This will let the dialog remain on the screen after an orientation.
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}