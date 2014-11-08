package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.tasks.AuthenticationCheckFinishedListener;
import net.somethingdreadful.MAL.tasks.AuthenticationCheckTask;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ProgressDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FirstTimeInit extends Activity implements AuthenticationCheckFinishedListener {
    EditText malUser;
    EditText malPass;
    String MalUser;
    String MalPass;
    ProgressDialog dialog;
    Context context;
    PrefManager prefManager;

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
        prefManager.deleteAccount();

        connectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MalUser = malUser.getText().toString().trim();
                MalPass = malPass.getText().toString().trim();
                tryConnection();
            }
        });

        registerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://myanimelist.net/register.php"));
                startActivity(browserIntent);

            }
        });

        NfcHelper.disableBeam(this);
    }

    private void tryConnection() {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(getString(R.string.dialog_title_Verifying));
        dialog.setMessage(getString(R.string.dialog_message_Verifying));
        dialog.show();
        new AuthenticationCheckTask(this).execute(MalUser, MalPass);
    }

    @Override
    public void onAuthenticationCheckFinished(boolean result) {
        if (result) {
            AccountService.addAccount(context, MalUser, MalPass);
            prefManager.setForceSync(true);
            prefManager.commitChanges();
            dialog.dismiss();
            Intent goHome = new Intent(context, Home.class);
            startActivity(goHome);
            finish();
        } else {
            dialog.dismiss();
            Crouton.makeText(this, R.string.crouton_error_VerifyProblem, Style.ALERT).show();
        }
    }
}
