package net.somethingdreadful.MAL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.account.AccountType;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.tasks.AuthenticationCheckFinishedListener;
import net.somethingdreadful.MAL.tasks.AuthenticationCheckTask;

public class FirstTimeInit extends ActionBarActivity implements AuthenticationCheckFinishedListener, OnClickListener {
    EditText malUser;
    EditText malPass;
    String MalUser;
    String MalPass;
    ProgressDialog dialog;
    Context context;
    ViewFlipper viewFlipper;
    Button connectButton;
    Button registerButton;
    WebView webview;
    TextView myanimelist;
    TextView anilist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        malUser = (EditText) findViewById(R.id.edittext_malUser);
        malPass = (EditText) findViewById(R.id.edittext_malPass);
        connectButton = (Button) findViewById(R.id.button_connectToMal);
        registerButton = (Button) findViewById(R.id.registerButton);
        webview = (WebView) findViewById(R.id.webview);
        myanimelist = (TextView) findViewById(R.id.myanimelist);
        anilist = (TextView) findViewById(R.id.anilist);

        context = getApplicationContext();

        connectButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        myanimelist.setOnClickListener(this);
        anilist.setOnClickListener(this);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String code = ALApi.getCode(url);
                if (code != null) {
                    MalUser = code;
                    tryConnection();
                    return true;
                } else {
                    return false;
                }
            }
        });
        webview.loadUrl(ALApi.getAnilistURL());

        PrefManager.deleteAccount();
        NfcHelper.disableBeam(this);
    }

    private void tryConnection() {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(getString(R.string.dialog_title_Verifying));
        dialog.setMessage(getString(R.string.dialog_message_Verifying));
        dialog.show();
        if (MalPass != null)
            new AuthenticationCheckTask(this).execute(MalUser, MalPass);
        else
            new AuthenticationCheckTask(this).execute(MalUser);
    }

    @Override
    public void onAuthenticationCheckFinished(boolean result, String username) {
        if (result) {
            if (username == null)
                AccountService.addAccount(MalUser, MalPass, AccountType.MyAnimeList);
            Crashlytics.setString("site", AccountService.accountType.toString());
            PrefManager.setForceSync(true);
            PrefManager.commitChanges();
            dialog.dismiss();
            Intent goHome = new Intent(context, Home.class);
            startActivity(goHome);
            finish();
        } else {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), R.string.toast_error_VerifyProblem, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewFlipper.getDisplayedChild() == 1 || viewFlipper.getDisplayedChild() == 2)
            viewFlipper.setDisplayedChild(0);
        else
            finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_connectToMal:
                MalUser = malUser.getText().toString().trim();
                MalPass = malPass.getText().toString().trim();
                tryConnection();
                break;
            case R.id.registerButton:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://myanimelist.net/register.php"));
                startActivity(browserIntent);
                break;
            case R.id.myanimelist:
                viewFlipper.setDisplayedChild(1);
                break;
            case R.id.anilist:
                viewFlipper.setDisplayedChild(2);
                break;
        }
    }
}
