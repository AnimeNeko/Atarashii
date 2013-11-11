package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class AnimeNetworkTask extends AsyncTask<String, Void, ArrayList<Anime>> {
	TaskJob job;
	int page = 1;
	Context context;
	AnimeNetworkTaskFinishedListener callback;
	
	public AnimeNetworkTask(TaskJob job, int page, Context context, AnimeNetworkTaskFinishedListener callback) {
		this.job = job;
		this.page = page;
		this.context = context;
		this.callback = callback;
	}

	public AnimeNetworkTask(TaskJob job, Context context, AnimeNetworkTaskFinishedListener callback) {
		this.job = job;
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected ArrayList<Anime> doInBackground(String... params) {
		ArrayList<Anime> result = null;
		MALApi api = new MALApi(context);
		switch (job) {
			case GETMOSTPOPULAR:
				result = api.getMostPopularAnime(1);
				break;
			case GETTOPRATED:
				result = api.getTopRatedAnime(1);
				break;
			case GETJUSTADDED:
				result = api.getJustAddedAnime(1);
				break;
			case GETUPCOMING:
				result = api.getUpcomingAnime(1);
				break;
			default:
				Log.e("MALX", "invalid job identifier " + job.name());
				result = null;
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Anime> result) {
		if (callback != null)
			callback.onAnimeNetworkTaskFinished(result, job);
	}
}
