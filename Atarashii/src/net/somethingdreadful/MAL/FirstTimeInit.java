package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ProgressDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import net.somethingdreadful.MAL.api.MALApi;

public class FirstTimeInit extends Activity {
    static EditText malUser;
    static EditText malPass;
    static String testMalUser;
    static String testMalPass;
    static ProgressDialog dialog;
    static Thread netThread;
    static Context context;
    static Handler messenger;
    static PrefManager prefManager;

    @SuppressLint("HandlerLeak")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);

        malUser = (EditText) findViewById(R.id.edittext_malUser);
        malPass = (EditText) findViewById(R.id.edittext_malPass);
        Button connectButton = (Button) findViewById(R.id.button_connectToMal);
        Button registerButton = (Button) findViewById(R.id.registerButton);
        context = getApplicationContext();

        prefManager = new PrefManager(context);

        connectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                testMalUser = malUser.getText().toString().trim();
                testMalPass = malPass.getText().toString().trim();
                tryConnection();
            }
        });

        registerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://myanimelist.net/register.php"));
                startActivity(browserIntent);

            }
        });

        messenger = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 2) {
                	dialog.dismiss();
                    Crouton.makeText(FirstTimeInit.this, R.string.crouton_error_VerifyProblem , Style.ALERT).show();
                }
                if (msg.what == 3) {
                	dialog.dismiss();

                    prefManager.setUser(testMalUser);
                    prefManager.setPass(testMalPass);
                    prefManager.setInit(true);
                    prefManager.setsync_time_last(0);
                    prefManager.commitChanges();

                    Intent home = new Intent(getApplicationContext(), Home.class);
                    startActivity(home);
                    finish();
                }
                super.handleMessage(msg);
            }
        };

        NfcHelper.disableBeam(this);
    }

    private void tryConnection() {
    	dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(getString(R.string.dialog_title_Verifying));
        dialog.setMessage(getString(R.string.dialog_message_Verifying));
        dialog.show();
        netThread = new networkThread();
        netThread.start();
    }

    public class networkThread extends Thread {
        @Override
        public void run() {
            boolean valid = new MALApi(testMalUser, testMalPass).isAuth();
            Message msg = new Message();
            if (!valid) {
                msg.what = 2;
                messenger.sendMessage(msg);
            } else {
                msg.what = 3;
                messenger.sendMessage(msg);
            }
        }
    }
}
