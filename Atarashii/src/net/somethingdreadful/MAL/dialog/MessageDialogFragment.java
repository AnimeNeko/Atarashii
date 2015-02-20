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

    EditText subject;
    EditText message;
    TextView header;
    ForumJob task;
    int id;
    View view;
    onSendClickListener callback;
    TextView send;
    onCloseClickListener closeCallback;

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

        subject.setVisibility(task == ForumJob.ADDTOPIC ? View.VISIBLE : View.GONE);
        setHeader(title);
        setClickListener();

        if (task == null)
            send.setText(getString(R.string.dialog_label_update));
        if (message != null)
            this.message.setText((new HtmlUtil(getActivity())).convertComment(message));
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
                case UPDATECOMMENT:
                    header.setText(getString(R.string.dialog_title_edit_comment));
                    break;
                case ADDTOPIC:
                    header.setText(getString(R.string.dialog_title_add_topic));
                    break;
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
        subject = (EditText) view.findViewById(R.id.dialog_message_title);
        message = (EditText) view.findViewById(R.id.dialog_message_message);

        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_bold));
        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_italic));
        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_underlined));
        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_striped));
        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_spoiler));
        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_center));
        Theme.setBackground(getActivity(), view.findViewById(R.id.dialog_message_close));
        Theme.setBackground(getActivity(), send);
        view.findViewById(R.id.dialog_message_close).setOnLongClickListener(this);

        this.view = view;
        return view;
    }

    /**
     * This will insert a BBCode in the message field.
     * note: It also changes the cursor position
     *
     * @param BBCode The BBCode string that should be in the message field
     */
    private void insert(String BBCode) {
        int curPos = message.getSelectionStart();
        String str = message.getText().toString();
        String str1 = str.substring(0, curPos);
        String str2 = str.substring(curPos);
        message.setText(str1 + BBCode +str2);
        message.setSelection(curPos + ((BBCode.length() - 1) / 2));
    }

    /**
     * Add all the onClickListener events.
     */
    private void setClickListener() {
        if (getArguments().getBoolean("BBCode", true)) {
            view.findViewById(R.id.dialog_message_bold).setOnClickListener(this);
            view.findViewById(R.id.dialog_message_italic).setOnClickListener(this);
            view.findViewById(R.id.dialog_message_underlined).setOnClickListener(this);
            view.findViewById(R.id.dialog_message_striped).setOnClickListener(this);
            view.findViewById(R.id.dialog_message_spoiler).setOnClickListener(this);
            view.findViewById(R.id.dialog_message_center).setOnClickListener(this);
        }
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
                if (closeCallback != null) {
                    message.clearFocus();
                    closeCallback.onCloseClicked(message.getText().toString() != null ? message.getText().toString() : "");
                }
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
                subject.clearFocus();
                message.clearFocus();
                if (message.getText().toString() != null && !message.getText().toString().equals(""))
                    callback.onSendClicked(message.getText().toString(), subject.getText().toString(), task, id);
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
        public void onSendClicked(String message, String subject, ForumJob task, int id);
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
     * The interface for callback
     */
    public interface onCloseClickListener {
        public void onCloseClicked(String message);
    }

    /**
     * Set the Callback for close purpose.
     *
     * @param callback The activity/fragment where the callback is located
     * @return MessageDialogFragment This will return the dialog itself to make init simple
     */
    public MessageDialogFragment setOnCloseClickListener(onCloseClickListener callback) {
        this.closeCallback = callback;
        return this;
    }

    public MessageDialogFragment setListeners(onSendClickListener callback, onCloseClickListener callback2){
        setOnSendClickListener(callback);
        setOnCloseClickListener(callback2);
        return this;
    }

    /**
     * This will let the dialog remain on the sceen after an orientation.
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}