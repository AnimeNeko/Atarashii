package net.somethingdreadful.MAL.api;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;

import java.util.ArrayList;

import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface MALInterface {
    @GET("/account/verify_credentials")
    Response verifyAuthentication();

    @GET("/anime/{anime_id}?mine=1")
    Anime getAnime(@Path("anime_id") int anime_id);

    @GET("/anime/search")
    ArrayList<Anime> searchAnime(@Query("q") String query, @Query("page") int page);

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

    @FormUrlEncoded
    @POST("/animelist/anime")
    Response addAnime(@Field("anime_id") int id, @Field("status") String status, @Field("episodes") int episodes,
                      @Field("score") int score);

    @FormUrlEncoded
    @PUT("/animelist/anime/{anime_id}")
    Response updateAnime(@Path("anime_id") int id, @Field("status") String status, @Field("episodes") int episodes,
                         @Field("score") int score, @Field("start") String start, @Field("end") String end);

    @GET("/manga/{manga_id}?mine=1")
    Manga getManga(@Path("manga_id") int manga_id);

    @GET("/manga/search")
    ArrayList<Manga> searchManga(@Query("q") String query, @Query("page") int page);

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

    @FormUrlEncoded
    @POST("/mangalist/manga")
    Response addManga(@Field("manga_id") int id, @Field("status") String status, @Field("chapters") int chapters,
                      @Field("volumes") int volumes, @Field("score") int score);

    @FormUrlEncoded
    @PUT("/mangalist/manga/{manga_id}")
    Response updateManga(@Path("manga_id") int id, @Field("status") String status, @Field("chapters") int chapters,
                         @Field("volumes") int volumes, @Field("score") int score, @Field("start") String readingStart, @Field("end") String readingEnd);

    @GET("/profile/{username}")
    Profile getProfile(@Path("username") String username);

    @GET("/friends/{username}")
    ArrayList<User> getFriends(@Path("username") String username);

    @GET("/forum")
    ForumMain getForum();

    @GET("/forum/{id}")
    ArrayList<Forum> getTopics(@Path("id") int id, @Query("page") int page);

    @GET("/forum/topic/{id}")
    ArrayList<Forum> getPosts(@Path("id") int id, @Query("page") int page);

    @GET("/forum/board/{id}")
    ArrayList<Forum> getSubBoards(@Path("id") int id, @Query("page") int page);
}
