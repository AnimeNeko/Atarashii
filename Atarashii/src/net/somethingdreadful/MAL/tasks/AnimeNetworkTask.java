package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.sql.DatabaseManager;

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
		if ( job == null ) {
			Log.e("MALX", "no job identifier, don't know what to do");
			return null;
		}
		switch (job) {
			case DOWNLOADANDSTORELIST:
				AnimeList animeList = api.getAnimeList();
				if (animeList != null) {
					result = animeList.getAnimes();
					DatabaseManager dbMan = new DatabaseManager(context);
					dbMan.saveAnimeList(result);
					if ( params != null ) // we got a listtype to return instead of complete list
						result = dbMan.getAnimeList(params[0]);
				}
				break;
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
			case SEARCH:
				if ( params != null )
					result = api.searchAnime(params[0]);
				break;
			default:
				Log.e("MALX", "invalid job identifier " + job.name());
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Anime> result) {
		if (callback != null)
			callback.onAnimeNetworkTaskFinished(result, job);
	}
}
