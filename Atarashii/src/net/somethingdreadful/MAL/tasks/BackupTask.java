package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.Backup;
import net.somethingdreadful.MAL.api.MALApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class BackupTask extends AsyncTask<String, Void, Object> {


    private final BackupTaskListener callback;
    private final Context context;
    private ArrayList<Anime> animeResult;
    private ArrayList<Manga> mangaResult;
    private ArrayList<File> files = new ArrayList<>();

    public BackupTask(BackupTaskListener callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected Object doInBackground(String... params) {
        try {
            if (AccountService.getAccount() == null) {
                callback.onBackupTaskFailed();
                return null;
            }

            MALManager mManager = new MALManager(context);
            PrefManager.create(context);
            mManager.verifyAuthentication();

            // creates directory if it doesn't exists
            File directory = new File(Environment.getExternalStorageDirectory() + "/Atarashii/");
            directory.mkdirs();

            if (PrefManager.getBackupLength() < directory.listFiles().length) {
                Crashlytics.log(Log.INFO, "MALX", "BackupTask.saveBackup(): Backup limit reached: " + PrefManager.getBackupLength() + " records: " + directory.length());
                Collections.addAll(files, directory.listFiles());
                File file = new File(files.get(0).getAbsolutePath());
                file.delete();
            }

            // check if the network is available
            if (MALApi.isNetworkAvailable(context)) {
                // clean dirty records to pull all the changes
                mManager.cleanDirtyAnimeRecords();
                mManager.cleanDirtyMangaRecords();
                animeResult = mManager.downloadAndStoreAnimeList(AccountService.getUsername());
                mangaResult = mManager.downloadAndStoreMangaList(AccountService.getUsername());
            }

            // Get results from the database if there weren't any records
            if (animeResult == null)
                animeResult = mManager.getAnimeListFromDB(String.valueOf(MALApi.ListType.ANIME));
            if (mangaResult == null)
                mangaResult = mManager.getMangaListFromDB(String.valueOf(MALApi.ListType.ANIME));

            // create the backup model and get the string
            Backup backup = new Backup();
            backup.setAccountType(AccountService.accountType);
            backup.setUsername(AccountService.getUsername());
            backup.setAnimeList(animeResult);
            backup.setMangaList(mangaResult);
            String jsonResult = (new Gson()).toJson(backup);

            // creates the file itself
            File file = new File(directory, "Backup" + System.currentTimeMillis() + "_" + AccountService.getUsername() + ".json");
            file.createNewFile();

            // writes the details in the files
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(jsonResult);
            outputStreamWriter.close();

            // notify user
            Crashlytics.log(Log.INFO, "MALX", "BackupTask.saveBackup(): Backup has been created");
            if (callback != null)
                callback.onBackupTaskFinished();
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "saveBackup()", e);
            if (callback != null) {
                callback.onBackupTaskFailed();
            }
        }
        return null;
    }

    public interface BackupTaskListener {
        void onBackupTaskFinished();

        void onBackupTaskFailed();
    }
}
