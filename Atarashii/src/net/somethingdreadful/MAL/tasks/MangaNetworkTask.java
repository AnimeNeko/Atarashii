package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.response.Manga;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MangaNetworkTask extends AsyncTask<String, Void, ArrayList<Manga>> {
	TaskJob job;
	int page = 1;
	Context context;
	MangaNetworkTaskFinishedListener callback;

	public MangaNetworkTask(TaskJob job, int page, Context context,
			MangaNetworkTaskFinishedListener callback) {
		this.job = job;
		this.page = page;
		this.context = context;
		this.callback = callback;
	}

	public MangaNetworkTask(TaskJob job, Context context,
			MangaNetworkTaskFinishedListener callback) {
		this.job = job;
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected ArrayList<Manga> doInBackground(String... params) {
		if (job == null) {
			Log.e("MALX", "no job identifier, don't know what to do");
			return null;
		}
		ArrayList<Manga> result = null;
		MALManager mManager = new MALManager(context);
		try {
			switch (job) {
			case GETLIST:
				if (params != null)
					result = mManager.getMangaListFromDB(params[0]);
				else
					result = mManager.getMangaListFromDB();
				break;
			case FORCESYNC:
				mManager.cleanDirtyMangaRecords();
				result = mManager.downloadAndStoreMangaList();
				if (result != null && params != null)
					result = mManager.getMangaListFromDB(params[0]);
				break;
			case GETMOSTPOPULAR:
				result = mManager.getAPIObject().getMostPopularManga(page);
				break;
			case GETTOPRATED:
				result = mManager.getAPIObject().getTopRatedManga(page);
				break;
			case GETJUSTADDED:
				result = mManager.getAPIObject().getJustAddedManga(page);
				break;
			case GETUPCOMING:
				result = mManager.getAPIObject().getUpcomingManga(page);
				break;
			case SEARCH:
				result = mManager.getAPIObject().searchManga(params[0], page);
				break;
			default:
				Log.e("MALX", "invalid job identifier " + job.name());
			}

			/*
			 * returning null means there was an error, so return an empty
			 * ArrayList if there was no error but an empty result
			 */
			if (result == null)
				result = new ArrayList<Manga>();
		} catch (Exception e) {
			if (e.getMessage() == null) {
				result = new ArrayList<Manga>();
			} else {
				Log.e("MALX", "error getting mangalist: " + e.getMessage());
				result = null;
			}
		}
		return result;
	}

	@Override
	protected void onPostExecute(ArrayList<Manga> result) {
		if (callback != null)
			callback.onMangaNetworkTaskFinished(result, job, page);
	}
}
