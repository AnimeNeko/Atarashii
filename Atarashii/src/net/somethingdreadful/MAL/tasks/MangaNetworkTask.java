package net.somethingdreadful.MAL.tasks;

import java.util.List;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Manga;

import android.content.Context;
import android.os.AsyncTask;

public class MangaNetworkTask extends AsyncTask<String, Void, List<Manga>> {
	int job;
	int page = 1;
	Context context;
	MangaNetworkTaskFinishedListener callback;

	public MangaNetworkTask(int job, int page, Context context, MangaNetworkTaskFinishedListener callback) {
		this.job = job;
		this.page = page;
		this.context = context;
		this.callback = callback;
	}
	
	public MangaNetworkTask(int job, Context context, MangaNetworkTaskFinishedListener callback) {
		this.job = job;
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected List<Manga> doInBackground(String... params) {
		List<Manga> result = null;
		MALApi api = new MALApi(context);
		switch (job) {
			case 0:
				result = api.getMostPopularManga(1);
				break;
			case 1:
				result = api.getTopRatedManga(1);
				break;
			case 2:
				result = api.getJustAddedManga(1);
				break;
			case 3:
				result = api.getUpcomingManga(1);
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(List<Manga> result) {
		if (callback != null)
			callback.onMangaNetworkTaskFinished(result);
	}
}
