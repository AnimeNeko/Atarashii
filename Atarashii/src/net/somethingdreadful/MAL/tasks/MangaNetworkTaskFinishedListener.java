package net.somethingdreadful.MAL.tasks;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.Manga;

public interface MangaNetworkTaskFinishedListener {
	public void onMangaNetworkTaskFinished(ArrayList<Manga> result, TaskJob job, int page);
}
