package net.somethingdreadful.MAL;

import android.app.ProgressDialog;
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
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import net.somethingdreadful.MAL.api.MALApi;

public class FirstTimeInit extends SherlockActivity {
    static EditText malUser;
    static EditText malPass;
    static String testMalUser;
    static String testMalPass;
    static ProgressDialog pd;
    static Thread netThread;
    static Context context;
    static private Handler messenger;
    static PrefManager prefManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        prefManager = new PrefManager(context);
        
        if(prefManager.getTheme() != 0) {
    		this.setTheme(prefManager.getTheme());
    	}
        
        setContentView(R.layout.firstrun);

        malUser = (EditText) findViewById(R.id.edittext_malUser);
        malPass = (EditText) findViewById(R.id.edittext_malPass);
        Button connectButton = (Button) findViewById(R.id.button_connectToMal);
        Button registerButton = (Button) findViewById(R.id.registerButton);

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
                    pd.dismiss();
                    Crouton.makeText(FirstTimeInit.this, R.string.crouton_error_VerifyProblem , Style.ALERT).show();
                }
                if (msg.what == 3) {
                    pd.dismiss();

                    prefManager.setUser(testMalUser);
                    prefManager.setPass(testMalPass);
                    prefManager.setInit(true);
                    prefManager.setsync_time_last(0);
                    prefManager.commitChanges();

                    Intent goHome = new Intent(context, Home.class);
                    startActivity(goHome);
                    System.exit(0);
                }
                super.handleMessage(msg);
            }
        };

        NfcHelper.disableBeam(this);
    }

    private void tryConnection() {
        pd = ProgressDialog.show(this, context.getString(R.string.dialog_title_Verifying), context.getString(R.string.dialog_message_Verifying));
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
