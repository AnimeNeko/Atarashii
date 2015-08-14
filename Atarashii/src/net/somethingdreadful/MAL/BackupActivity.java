package net.somethingdreadful.MAL;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.BackupGridviewAdapter;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;
import net.somethingdreadful.MAL.dialog.InformationDialogFragment;
import net.somethingdreadful.MAL.tasks.BackupTask;
import net.somethingdreadful.MAL.tasks.RestoreTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BackupActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, ChooseDialogFragment.onClickListener, BackupGridviewAdapter.onClickListener, BackupTask.BackupTaskListener, RestoreTask.RestoreTaskListener {
    ProgressDialog dialog;
    @InjectView(R.id.listview)
    GridView Gridview;
    BackupGridviewAdapter backupGridviewAdapter;
    ArrayList<File> files = new ArrayList<>();
    int position = 0;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.swiperefresh)
    public SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Theme.setBackground(this, findViewById(R.id.friends_parent), Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        Theme.setActionBar(this);
        ButterKnife.inject(this);

        backupGridviewAdapter = new BackupGridviewAdapter<>(this, files, this);
        backupGridviewAdapter.setNotifyOnChange(true);
        Gridview.setVisibility(View.VISIBLE);
        Gridview.setOnItemClickListener(this);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
        toggle(1);
        getListFiles();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        NfcHelper.disableBeam(this);
    }

    /**
     * Switch between the loading screen & list
     *
     * @param number The number of the item that you want to view
     */
    private void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.activity_backup_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.backup:
                dialog = new ProgressDialog(this);
                dialog.setIndeterminate(true);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle(getString(R.string.title_activity_backup));
                dialog.setMessage(getString(R.string.dialog_message_backup_requesting));
                dialog.show();

                new BackupTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
        return true;
    }

    /**
     * Get all the backup files
     */
    private void getListFiles() {
        files.clear();
        File file = new File(Environment.getExternalStorageDirectory() + "/Atarashii/");
        if (file.length() != 0)
            Collections.addAll(files, file.listFiles());
        refresh();
    }

    /**
     * Refresh the backup list
     */
    public void refresh() {
        Gridview.setAdapter(backupGridviewAdapter);
        try {
            backupGridviewAdapter.supportAddAll(files);
            backupGridviewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "BackupActivity.refresh(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        toggle(0);
    }

    /**
     * Restore a backup
     *
     * @param filename The filename of the backup
     */
    private void restoreBackup(String filename) {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(getString(R.string.title_activity_backup));
        dialog.setMessage(getString(R.string.dialog_message_backup_reading));
        dialog.show();

        new RestoreTask(this, this, filename).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.position = position;
        ChooseDialogFragment lcdf = new ChooseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.dialog_title_restore));
        bundle.putString("message", getString(R.string.dialog_message_restore));
        bundle.putString("positive", getString(android.R.string.yes));
        lcdf.setArguments(bundle);
        lcdf.setCallback(this);
        lcdf.show(getFragmentManager(), "fragment_restoreConfirmationDialog");

        // check if the username equals the backup name.
        String filename = files.get(position).getName();
        if (!filename.substring(filename.lastIndexOf("_") + 1, filename.length() - 5).equals(AccountService.getUsername())) {
            InformationDialogFragment info = new InformationDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", getString(R.string.dialog_title_userername));
            args.putString("message", getString(R.string.dialog_message_username));
            info.setArguments(args);
            info.show(getFragmentManager(), "fragment_forum");
        }
    }

    @Override
    public void onRefresh() {
        swipeRefresh.setRefreshing(true);
        swipeRefresh.setEnabled(false);
        getListFiles();
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onPositiveButtonClicked() {
        restoreBackup(files.get(position).getAbsolutePath());
    }

    @Override
    public void onRemoveClicked(int position) {
        File file = new File(files.get(position).getAbsolutePath());
        file.delete();
        onRefresh();
        Theme.Snackbar(this, R.string.toast_info_backup_remove);
    }

    @Override
    public void onBackupTaskFinished() {
        Theme.Snackbar(this, R.string.toast_info_backup_saved);

        // run UI changes on the main activity
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getListFiles();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackupTaskFailed() {
        // run UI changes on the main activity
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRestoreTaskFinished() {
        Theme.Snackbar(this, R.string.toast_info_backup_revert);

        // run UI changes on the main activity
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRestoreTaskFailed() {
        // run UI changes on the main activity
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }
}
