package net.somethingdreadful.MAL.api;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

public interface MALInterface {
	@GET("/account/verify_credentials")
	Response verifyAuthentication();

	@GET("/anime/{anime_id}")
	Anime getAnime(@Path("anime_id") int anime_id);
	@GET("/anime/search")
	ArrayList<Anime> searchAnime(@Query("q") String query);
	@GET("/anime/popular")
	ArrayList<Anime> getPopularAnime(@Query("page") int page);
	@GET("/anime/top")
	ArrayList<Anime> getTopRatedAnime(@Query("page") int page);
	@GET("/anime/upcoming")
	ArrayList<Anime> getUpcomingAnime(@Query("page") int page);
	@GET("/anime/just_added")
	ArrayList<Anime> getJustAddedAnime(@Query("page") int page);
	@GET("/animelist/{username}")
	AnimeList getAnimeList(@Path("username") String username);
	@DELETE("/animelist/anime/{anime_id}")
	Response deleteAnime(@Path("anime_id") int anime_id);
	@Multipart
	@POST("/animelist/anime")
	Response addAnime(@Part("anime_id") int id, @Part("status") String status, @Part("episodes") int episodes, 
			@Part("score") int score);
	@Multipart
	@PUT("/animelist/anime/{anime_id}")
	Response updateAnime(@Path("anime_id") int id, @Part("status") String status, @Part("episodes") int episodes, 
			@Part("score") int score);
	
	@GET("/manga/{manga_id}")
	Manga getManga(@Path("manga_id") int manga_id);
	@GET("/manga/search")
	ArrayList<Manga> searchManga(@Query("q") String query);
	@GET("/manga/popular")
	ArrayList<Manga> getPopularManga(@Query("page") int page);
	@GET("/manga/top")
	ArrayList<Manga> getTopRatedManga(@Query("page") int page);
	@GET("/manga/upcoming")
	ArrayList<Manga> getUpcomingManga(@Query("page") int page);
	@GET("/manga/just_added")
	ArrayList<Manga> getJustAddedManga(@Query("page") int page);
	@GET("/mangalist/{username}")
	MangaList getMangaList(@Path("username") String username);
	@DELETE("/mangalist/manga/{manga_id}")
	Response deleteManga(@Path("manga_id") int manga_id);
	@Multipart
	@POST("/mangalist/manga")
	Response addManga(@Part("manga_id") int id, @Part("status") String status, @Part("chapters") int chapters, 
			@Part("volumes") int volumes, @Part("score") int score);
	@PUT("/mangalist/manga/{manga_id}")
	Response updateManga(@Path("manga_id") int id, @Part("status") String status, @Part("chapters") int chapters, 
			@Part("volumes") int volumes, @Part("score") int score);
}
