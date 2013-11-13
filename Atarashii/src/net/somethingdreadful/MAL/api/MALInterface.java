package net.somethingdreadful.MAL.api;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.request.AnimeRequest;
import net.somethingdreadful.MAL.api.request.MangaRequest;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface MALInterface {
	@GET("/account/verify_credentials")
	Response verifyAuthentication();

	@GET("/anime/{anime_id}")
	Anime getAnime(@Path("anime_id") int anime_id);
	@GET("/anime/search?q={query}")
	ArrayList<Anime> searchAnime(@Path("query") String query);
	@GET("/anime/popular?page={page}")
	ArrayList<Anime> getPopularAnime(@Path("page") int page);
	@GET("/anime/top?page={page}")
	ArrayList<Anime> getTopRatedAnime(@Path("page") int page);
	@GET("/anime/upcoming?page={page}")
	ArrayList<Anime> getUpcomingAnime(@Path("page") int page);
	@GET("/anime/just_added?page={page}")
	ArrayList<Anime> getJustAddedAnime(@Path("page") int page);
	@GET("/animelist/{username}")
	AnimeList getAnimeList(@Path("username") String username);
	@DELETE("/animelist/anime/{anime_id}")
	Response deleteAnime(@Path("anime_id") int anime_id);
	@POST("/animelist/anime")
	Response addAnime(@Body AnimeRequest body);
	@PUT("/animelist/anime/{anime_id}")
	Response updateAnime(@Body AnimeRequest body, @Path("anime_id") int id);
	
	@GET("/manga/{manga_id}")
	Manga getManga(@Path("manga_id") int manga_id);
	@GET("/manga/search?q={query}")
	ArrayList<Manga> searchManga(@Path("query") String query);
	@GET("/manga/popular?page={page}")
	ArrayList<Manga> getPopularManga(@Path("page") int page);
	@GET("/manga/top?page={page}")
	ArrayList<Manga> getTopRatedManga(@Path("page") int page);
	@GET("/manga/upcoming?page={page}")
	ArrayList<Manga> getUpcomingManga(@Path("page") int page);
	@GET("/manga/just_added?page={page}")
	ArrayList<Manga> getJustAddedManga(@Path("page") int page);
	@GET("/mangalist/{username}")
	MangaList getMangaList(@Path("username") String username);
	@DELETE("/mangalist/manga/{manga_id}")
	Response deleteManga(@Path("manga_id") int manga_id);
	@POST("/mangalist/manga")
	Response addManga(@Body MangaRequest body);
	@PUT("/mangalist/manga/{manga_id}")
	Response updateManga(@Body MangaRequest body, @Path("manga_id") int id);
}