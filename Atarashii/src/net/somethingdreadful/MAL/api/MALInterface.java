package net.somethingdreadful.MAL.api;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import retrofit.http.GET;
import retrofit.http.Path;

public interface MALInterface {
	@GET("/anime/{anime_id}")
	Anime getAnime(@Path("anime_id") int anime_id);
	@GET("/animelist/{username}")
	AnimeList getAnimeList(@Path("username") String username);
	
	@GET("/manga/{manga_id}")
	Manga getManga(@Path("manga_id") int manga_id);
	@GET("/mangalist/{username}")
	MangaList getMangaList(@Path("username") String username);
}
