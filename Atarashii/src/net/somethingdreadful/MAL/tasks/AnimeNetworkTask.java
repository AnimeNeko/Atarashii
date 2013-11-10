package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;

import android.content.Context;
import android.os.AsyncTask;

public class AnimeNetworkTask extends AsyncTask<String, Void, ArrayList<Anime>> {
	int job;
	int page = 1;
	Context context;
	AnimeNetworkTaskFinishedListener callback;
	
	public AnimeNetworkTask(int job, int page, Context context, AnimeNetworkTaskFinishedListener callback) {
		this.job = job;
		this.page = page;
		this.context = context;
		this.callback = callback;
	}

	public AnimeNetworkTask(int job, Context context, AnimeNetworkTaskFinishedListener callback) {
		this.job = job;
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected ArrayList<Anime> doInBackground(String... params) {
		ArrayList<Anime> result = null;
		MALApi api = new MALApi(context);
		switch (job) {
			case 1:
				result = api.getMostPopularAnime(1);
				break;
			case 2:
				result = api.getTopRatedAnime(1);
				break;
			case 3:
				result = api.getJustAddedAnime(1);
				break;
			case 4:
				result = api.getUpcomingAnime(1);
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Anime> result) {
		if (callback != null)
			callback.onAnimeNetworkTaskFinished(result);
	}
}
