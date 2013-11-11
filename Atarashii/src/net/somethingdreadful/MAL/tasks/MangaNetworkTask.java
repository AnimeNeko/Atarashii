package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Manga;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MangaNetworkTask extends AsyncTask<String, Void, ArrayList<Manga>> {
	TaskJob job;
	int page = 1;
	Context context;
	MangaNetworkTaskFinishedListener callback;

	public MangaNetworkTask(TaskJob job, int page, Context context, MangaNetworkTaskFinishedListener callback) {
		this.job = job;
		this.page = page;
		this.context = context;
		this.callback = callback;
	}
	
	public MangaNetworkTask(TaskJob job, Context context, MangaNetworkTaskFinishedListener callback) {
		this.job = job;
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected ArrayList<Manga> doInBackground(String... params) {
		ArrayList<Manga> result = null;
		MALApi api = new MALApi(context);
		switch (job) {
			case GETMOSTPOPULAR:
				result = api.getMostPopularManga(1);
				break;
			case GETTOPRATED:
				result = api.getTopRatedManga(1);
				break;
			case GETJUSTADDED:
				result = api.getJustAddedManga(1);
				break;
			case GETUPCOMING:
				result = api.getUpcomingManga(1);
				break;
			default:
				Log.e("MALX", "invalid job identifier " + job.name());
				result = null;
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Manga> result) {
		if (callback != null)
			callback.onMangaNetworkTaskFinished(result);
	}
}
