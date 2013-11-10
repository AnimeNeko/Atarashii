package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.Anime;

public interface AnimeNetworkTaskFinishedListener {
	public void onAnimeNetworkTaskFinished(ArrayList<Anime> result);
}
