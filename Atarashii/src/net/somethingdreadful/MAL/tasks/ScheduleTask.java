package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;

public class ScheduleTask extends AsyncTask<String, Void, Schedule> {
    private Activity activity = null;
    private ScheduleTaskListener callback;


    public ScheduleTask(Activity activity, ScheduleTaskListener callback) {
        this.activity = activity;
        this.callback = callback;
    }

    @Override
    protected Schedule doInBackground(String... params) {
        if (!APIHelper.isNetworkAvailable(activity)) {
            if (activity != null)
                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
            return null;
        }

        Schedule taskResult = new Schedule();
        ContentManager cManager = new ContentManager(activity);
        if (!AccountService.isMAL())
            cManager.verifyAuthentication();

        try {
            taskResult = cManager.getSchedule();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "ScheduleTask.doInBackground(): " + e.getMessage());
        }
        return taskResult;
    }

    @Override
    protected void onPostExecute(Schedule result) {
        callback.onScheduleTaskFinished(result);
    }

    public interface ScheduleTaskListener {
        void onScheduleTaskFinished(Schedule result);
    }
}
