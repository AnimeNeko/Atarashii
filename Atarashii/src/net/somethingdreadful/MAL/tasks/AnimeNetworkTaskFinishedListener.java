package net.somethingdreadful.MAL.tasks;

import java.util.List;

import net.somethingdreadful.MAL.api.response.Anime;

public interface AnimeNetworkTaskFinishedListener {
	public void onAnimeNetworkTaskFinished(List<Anime> result);
}
