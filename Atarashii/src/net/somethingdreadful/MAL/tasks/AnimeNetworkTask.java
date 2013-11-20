package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.MALManager;
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
		if ( job == null ) {
			Log.e("MALX", "no job identifier, don't know what to do");
			return null;
		}
		ArrayList<Anime> result = null;
		MALManager mManager = new MALManager(context);
		switch (job) {
			case GETLIST:
				if ( params != null )
					result = mManager.getAnimeListFromDB(params[0]);
				else
					result = mManager.getAnimeListFromDB();
				break;
			case FORCESYNC:
				mManager.cleanDirtyAnimeRecords();
				result = mManager.downloadAndStoreAnimeList();
				if ( result != null && params != null )
					result = mManager.getAnimeListFromDB(params[0]);
				break;
			case GETMOSTPOPULAR:
				result = mManager.getAPIObject().getMostPopularAnime(1);
				break;
			case GETTOPRATED:
				result = mManager.getAPIObject().getTopRatedAnime(1);
				break;
			case GETJUSTADDED:
				result = mManager.getAPIObject().getJustAddedAnime(1);
				break;
			case GETUPCOMING:
				result = mManager.getAPIObject().getUpcomingAnime(1);
				break;
			case SEARCH:
				if ( params != null )
					result = mManager.getAPIObject().searchAnime(params[0]);
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
