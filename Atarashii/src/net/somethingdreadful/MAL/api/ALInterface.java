package net.somethingdreadful.MAL.api;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.OAuth;
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

public interface ALInterface {
    @FormUrlEncoded
    @POST("/auth/access_token")
    OAuth getAuthCode(@Field("grant_type") String grant_type, @Field("client_id") String client_id, @Field("client_secret") String client_secret,
                          @Field("redirect_uri") String redirect_uri, @Field("code") String code);

    @GET("/user")
    Profile getCurrentUser();

    @GET("/user/{username}")
    Profile getProfile(@Path("username") String username);

    @GET("/user/{username}/animelist")
    AnimeList getAnimeList(@Path("username") String username);

    @GET("/user/{username}/mangalist")
    MangaList getMangaList(@Path("username") String username);

    @FormUrlEncoded
    @POST("/auth/access_token")
    OAuth getAccesToken(@Field("grant_type") String grant_type, @Field("client_id") String client_id, @Field("client_secret") String client_secret,
                        @Field("refresh_token") String refresh_token);

    @GET("/anime/{anime_id}")
    Anime getAnime(@Path("anime_id") int anime_id);

    @GET("/manga/{manga_id}")
    Manga getManga(@Path("manga_id") int manga_id);

    @GET("/anime/search/{query}")
    ArrayList<Anime> searchAnime(@Path("query") String query, @Query("page") int page);

    @GET("/manga/search/{query}")
    ArrayList<Manga> searchManga(@Path("query") String query, @Query("page") int page);

    @GET("/anime/browse/upcoming")
    AnimeList getUpcomingAnime(@Query("page") int page);

    @GET("/manga/browse/upcoming")
    MangaList getUpcomingManga(@Query("page") int page);

    @GET("/anime/browse/recent")
    AnimeList getJustAddedAnime(@Query("page") int page);

    @GET("/manga/browse/recent")
    MangaList getJustAddedManga(@Query("page") int page);

    @GET("/manga/browse/publishing")
    MangaList getAiringManga(@Query("page") int page);

    @GET("/anime/browse/airing")
    AnimeList getAiringAnime(@Query("page") int page);

    @GET("/manga/browse/year/{year}")
    MangaList getYearManga(@Path("year") int year, @Query("page") int page);

    @GET("/anime/browse/year/{year}")
    AnimeList getYearAnime(@Path("year") int year, @Query("page") int page);

    @GET("/user/{username}/followers")
    ArrayList<User> getFollowers(@Path("username") String username);

    @FormUrlEncoded
    @POST("/animelist")
    Response addAnime(@Field("id") int id, @Field("list_status") String status, @Field("episodes_watched") int episodes,
                      @Field("score") int score);

    @FormUrlEncoded
    @PUT("/animelist")
    Response updateAnime(@Field("id") int id, @Field("list_status") String status, @Field("episodes_watched") int episodes,
                         @Field("score") int score);

    @DELETE("/animelist/{anime_id}")
    Response deleteAnime(@Path("anime_id") int anime_id);

    @DELETE("/mangalist/{manga_id}")
    Response deleteManga(@Path("manga_id") int manga_id);

    @FormUrlEncoded
    @POST("/mangalist")
    Response addManga(@Field("id") int id, @Field("list_status") String status, @Field("chapters_read") int chapters,
                      @Field("volumes_read") int volumes, @Field("score") int score);

    @FormUrlEncoded
    @PUT("/mangalist")
    Response updateManga(@Field("id") int id, @Field("list_status") String status, @Field("chapters_read") int chapters,
                         @Field("volumes_read") int volumes, @Field("score") int score);

    @GET("/forum/recent")
    ForumMain getForumRecent();

    @GET("/forum/new")
    ForumMain getForumNew();

    @GET("/forum/subscribed")
    ForumMain getForumSubscribed();

    @GET("/forum/thread/{id}")
    ForumMain getTopics(@Path("id") int id, @Query("page") int page);

    @FormUrlEncoded
    @POST("/forum/comment")
    Response addComment(@Field("thread_id") int id, @Field("comment") String message);

    @FormUrlEncoded
    @PUT("/forum/comment")
    Response updateComment(@Path("id") int id, @Field("comment") String message);

    @GET("/forum/search/{query}")
    ForumMain search(@Path("query") String query);

    @GET("/forum/tag")
    ForumMain getAnime(@Path("tag") int id, @Query("page") int page);

    @GET("/forum/tag")
    ForumMain getManga(@Path("tag") int id, @Query("page") int page);

    @FormUrlEncoded
    @POST("/forum/{id}")
    Response addTopic(@Path("tags") int tags, @Path("tags_anime") int tags_anime, @Path("tags_manga") int tags_manga,
                      @Field("title") String title, @Field("body") String body);

    @GET("/forum/thread/{id}")
    ForumMain getPosts(@Path("id") int id, @Query("page") int page);
}
