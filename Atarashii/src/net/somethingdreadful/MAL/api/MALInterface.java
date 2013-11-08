package net.somethingdreadful.MAL.api;

import java.util.List;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;

public interface MALInterface {
	@GET("/account/verify_credentials")
	Response verifyAuthentication();

	@GET("/anime/{anime_id}")
	Anime getAnime(@Path("anime_id") int anime_id);
	@GET("/anime/search?q={query}")
	List<Anime> searchAnime(@Path("query") String query);
	@GET("/anime/popular?page={page}")
	List<Anime> getPopularAnime(@Path("page") int page);
	@GET("/anime/top?page={page}")
	List<Anime> getTopRatedAnime(@Path("page") int page);
	@GET("/anime/upcoming?page={page}")
	List<Anime> getUpcomingAnime(@Path("page") int page);
	@GET("/anime/just_added?page={page}")
	List<Anime> getJustAddedAnime(@Path("page") int page);
	@GET("/animelist/{username}")
	AnimeList getAnimeList(@Path("username") String username);
	
	@GET("/manga/{manga_id}")
	Manga getManga(@Path("manga_id") int manga_id);
	@GET("/manga/search?q={query}")
	List<Manga> searchManga(@Path("query") String query);
	@GET("/manga/popular?page={page}")
	List<Manga> getPopularManga(@Path("page") int page);
	@GET("/manga/top?page={page}")
	List<Manga> getTopRatedManga(@Path("page") int page);
	@GET("/manga/upcoming?page={page}")
	List<Manga> getUpcomingManga(@Path("page") int page);
	@GET("/manga/just_added?page={page}")
	List<Manga> getJustAddedManga(@Path("page") int page);
	@GET("/mangalist/{username}")
	MangaList getMangaList(@Path("username") String username);
}
