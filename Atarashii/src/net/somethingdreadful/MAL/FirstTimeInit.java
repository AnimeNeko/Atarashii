package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.AuthenticationCheckTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FirstTimeInit extends AppCompatActivity implements AuthenticationCheckTask.AuthenticationCheckListener, OnClickListener {
    private String MalUser;
    private String MalPass;
    private Context context;
    private ProgressDialog dialog;

    @Bind(R.id.edittext_malUser)
    EditText malUser;
    @Bind(R.id.edittext_malPass)
    EditText malPass;
    @Bind(R.id.viewFlipper)
    ViewFlipper viewFlipper;
    @Bind(R.id.button_connectToMal)
    Button connectButton;
    @Bind(R.id.registerButton)
    Button registerButton;
    @Bind(R.id.webview)
    WebView webview;
    @Bind(R.id.myanimelist)
    TextView myanimelist;
    @Bind(R.id.anilist)
    TextView anilist;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);
        Theme.setActionBar(this);
        ButterKnife.bind(this);

        context = getApplicationContext();

        if (getIntent().getBooleanExtra("updatePassword", false))
            Theme.Snackbar(this, R.string.toast_info_password);
        connectButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        myanimelist.setOnClickListener(this);
        anilist.setOnClickListener(this);

        CookieManager.getInstance().removeAllCookie();
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
        if (MALApi.isNetworkAvailable(this)) {
            dialog = new ProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle(getString(R.string.dialog_title_Verifying));
            dialog.setMessage(getString(R.string.dialog_message_Verifying));
            dialog.show();
            if (MalPass != null)
                new AuthenticationCheckTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MalUser, MalPass);
            else
                new AuthenticationCheckTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MalUser);
        } else {
            Theme.Snackbar(this, R.string.toast_error_noConnectivity);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthenticationCheckFinished(boolean result) {
        try {
            if (result) {
                // load account before requesting the information
                AccountService.getAccount();
                Theme.setCrashData("site", AccountService.accountType.toString());
                PrefManager.setForceSync(true);
                PrefManager.commitChanges();
                Answers.getInstance().logLogin(new LoginEvent()
                        .putMethod(AccountService.accountType.toString())
                        .putSuccess(true));
                dialog.dismiss();
                Intent goHome = new Intent(context, Home.class);
                startActivity(goHome);
                finish();
            } else {
                dialog.dismiss();
                if (MALApi.isNetworkAvailable(this))
                    Theme.Snackbar(this, R.string.toast_error_VerifyProblem);
                else
                    Theme.Snackbar(this, R.string.toast_error_noConnectivity);
            }
        } catch (Exception e) {
            Theme.logTaskCrash("FirstTimeInit", "onAuthenticationCheckFinished", e);
            Answers.getInstance().logLogin(new LoginEvent()
                    .putSuccess(false));
            Theme.Snackbar(this, R.string.toast_error_VerifyProblem);
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
                if (MALApi.isNetworkAvailable(this))
                    viewFlipper.setDisplayedChild(2);
                else
                    Theme.Snackbar(this, R.string.toast_error_noConnectivity);
                break;
        }
    }
}

