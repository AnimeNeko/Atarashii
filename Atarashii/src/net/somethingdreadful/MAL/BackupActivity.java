package net.somethingdreadful.MAL;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.BackupGridviewAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.response.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.response.Backup;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;
import net.somethingdreadful.MAL.dialog.InformationDialogFragment;
import net.somethingdreadful.MAL.sql.DatabaseManager;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.NetworkTaskCallbackListener;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BackupActivity extends ActionBarActivity implements NetworkTaskCallbackListener, APIAuthenticationErrorListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, ChooseDialogFragment.onClickListener, BackupGridviewAdapter.onClickListener {
    ProgressDialog dialog;
    ArrayList<Anime> animeList;
    ArrayList<Manga> mangaList;
    @InjectView(R.id.listview) GridView Gridview;
    BackupGridviewAdapter backupGridviewAdapter;
    ArrayList<File> files = new ArrayList<>();
    boolean animeLoaded = false;
    boolean mangaLoaded = false;
    int position = 0;
    @InjectView(R.id.progressBar) ProgressBar progressBar;
    @InjectView(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends);
        Theme.setBackground(this, findViewById(R.id.friends_parent), Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
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

                // AnimeList request
                ArrayList<String> animelistArgs = new ArrayList<String>();
                animelistArgs.add(AccountService.getUsername());
                animelistArgs.add(MALManager.listSortFromInt(0, MALApi.ListType.ANIME));
                new NetworkTask(TaskJob.GETLIST, MALApi.ListType.MANGA, this, new Bundle(), this, this)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, animelistArgs.toArray(new String[animelistArgs.size()]));

                // MangaList request
                ArrayList<String> mangalistArgs = new ArrayList<String>();
                mangalistArgs.add(AccountService.getUsername());
                mangalistArgs.add(MALManager.listSortFromInt(0, MALApi.ListType.ANIME));
                new NetworkTask(TaskJob.GETLIST, MALApi.ListType.ANIME, this, new Bundle(), this, this)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mangalistArgs.toArray(new String[mangalistArgs.size()]));
                break;
        }
        return true;
    }

    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled) {
        if (type.equals(MALApi.ListType.ANIME)) {
            animeList = (ArrayList<Anime>) result;
            animeLoaded = true;
        } else {
            mangaList = (ArrayList<Manga>) result;
            mangaLoaded = true;
        }

        if (animeLoaded && mangaLoaded) {
            animeLoaded = false;
            mangaLoaded = false;
            saveBackup();
        }
    }

    @Override
    public void onNetworkTaskError(TaskJob job, MALApi.ListType type, Bundle data, boolean cancelled) {
        dialog.dismiss();
        Theme.Snackbar(this, R.string.toast_error_Records);
    }

    /**
     * Get all the backup files
     */
    private void getListFiles() {
        files.clear();
        Collections.addAll(files, (new File(Environment.getExternalStorageDirectory() + "/Atarashii/")).listFiles());
        refresh();
    }

    /**
     * Saves the backup
     */
    private void saveBackup() {
        dialog.setMessage(getString(R.string.dialog_message_backup_converting));

        Backup backup = new Backup();
        backup.setAccountType(AccountService.accountType);
        backup.setUsername(AccountService.getUsername());
        backup.setAnimeList(animeList);
        backup.setMangaList(mangaList);
        String jsonResult = (new Gson()).toJson(backup);

        dialog.setMessage(getString(R.string.dialog_message_backup_saving));
        try {
            // creates directory if it doesn't exists
            File directory = new File(Environment.getExternalStorageDirectory() + "/Atarashii/");
            directory.mkdirs();

            // creates the file itself
            File file = new File(directory, "Backup" + System.currentTimeMillis() + "_" + AccountService.getUsername() + ".json");
            file.createNewFile();

            // writes the details in the files
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(jsonResult);
            outputStreamWriter.close();

            // notify user
            dialog.dismiss();
            Theme.Snackbar(this, R.string.toast_info_backup_saved);
            Crashlytics.log(Log.INFO, "MALX", "BackupActivity.saveBackup(): Backup has been created");
            getListFiles();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "BackupActivity.saveBackup(): " + e.getMessage());
            Crashlytics.logException(e);
            dialog.dismiss();
        }
    }

    /**
     * Refresh the backup list
     */
    public void refresh() {
        Gridview.setAdapter(backupGridviewAdapter);
        try {
            backupGridviewAdapter.supportAddAll(files);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Crashlytics.log(Log.ERROR, "MALX", "FriendsActivity.refresh(): " + e.getMessage());
        }
        backupGridviewAdapter.notifyDataSetChanged();
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

        try {
            StringBuilder backupJson = new StringBuilder();
            FileInputStream fIn = new FileInputStream(new File(filename));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fIn));
            String buffer;
            while ((buffer = bufferedReader.readLine()) != null)
                backupJson.append(buffer);
            bufferedReader.close();
            dialog.setMessage(getString(R.string.dialog_message_backup_converting));
            Backup backup = (new Gson()).fromJson(backupJson.toString(), Backup.class);
            DatabaseManager databaseManager = new DatabaseManager(this);
            databaseManager.restoreLists(backup.getAnimeList(), backup.getMangaList());
            dialog.dismiss();
            Theme.Snackbar(this, R.string.toast_info_backup_revert);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "BackupActivity.restoreBackup(): " + e.getMessage());
            Crashlytics.logException(e);
            dialog.dismiss();
        }
    }

    @Override
    public void onAPIAuthenticationError(MALApi.ListType type, TaskJob job) {
        Theme.Snackbar(this, R.string.toast_error_VerifyProblem);
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
}
