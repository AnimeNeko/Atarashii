package net.somethingdreadful.MAL.tasks;

import java.util.List;

import net.somethingdreadful.MAL.api.response.Manga;

public interface MangaNetworkTaskFinishedListener {
	public void onMangaNetworkTaskFinished(List<Manga> result);
}
