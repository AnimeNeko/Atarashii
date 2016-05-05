package net.somethingdreadful.MAL.api;

import net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.BrowseAnimeList;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.BrowseMangaList;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.ALModels.Follow;
import net.somethingdreadful.MAL.api.ALModels.ForumAL;
import net.somethingdreadful.MAL.api.ALModels.ForumThread;
import net.somethingdreadful.MAL.api.ALModels.History;
import net.somethingdreadful.MAL.api.ALModels.OAuth;
import net.somethingdreadful.MAL.api.ALModels.Profile;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface ALInterface {
    @FormUrlEncoded
    @POST("auth/access_token")
    Call<OAuth> getAuthCode(@Field("grant_type") String grant_type, @Field("client_id") String client_id, @Field("client_secret") String client_secret,
                      @Field("redirect_uri") String redirect_uri, @Field("code") String code);

    @GET("user")
    Call<Profile> getCurrentUser();

    @GET("user/{username}")
    Call<Profile> getProfile(@Path("username") String username);

    @GET("user/{username}/activity")
    Call<ArrayList<History>> getActivity(@Path("username") String username, @Query("page") int page);

    @GET("user/{username}/animelist")
    Call<UserList> getAnimeList(@Path("username") String username);

    @GET("user/{username}/mangalist")
    Call<UserList> getMangaList(@Path("username") String username);

    @FormUrlEncoded
    @POST("auth/access_token")
    Call<OAuth> getAccesToken(@Field("grant_type") String grant_type, @Field("client_id") String client_id, @Field("client_secret") String client_secret,
                        @Field("refresh_token") String refresh_token);

    @GET("anime/{anime_id}")
    Call<Anime> getAnime(@Path("anime_id") int anime_id);

    @GET("manga/{manga_id}")
    Call<Manga> getManga(@Path("manga_id") int manga_id);

    @GET("anime/search/{query}")
    Call<ArrayList<Anime>> searchAnime(@Path("query") String query, @Query("page") int page);

    @GET("manga/search/{query}")
    Call<ArrayList<Manga>> searchManga(@Path("query") String query, @Query("page") int page);

    @GET("anime/browse/upcoming")
    Call<BrowseAnimeList> getUpcomingAnime(@Query("page") int page);

    @GET("manga/browse/upcoming")
    Call<BrowseMangaList> getUpcomingManga(@Query("page") int page);

    @GET("anime/browse/recent")
    Call<BrowseAnimeList> getJustAddedAnime(@Query("page") int page);

    @GET("manga/browse/recent")
    Call<BrowseMangaList> getJustAddedManga(@Query("page") int page);

    @GET("manga/browse/publishing")
    Call<BrowseMangaList> getAiringManga(@Query("page") int page);

    @GET("anime/browse/airing")
    Call<BrowseMangaList> getAiringAnime(@Query("page") int page);

    @GET("manga/browse/year/{year}")
    Call<BrowseMangaList> getYearManga(@Path("year") int year, @Query("page") int page);

    @GET("anime/browse/year/{year}")
    Call<BrowseAnimeList> getYearAnime(@Path("year") int year, @Query("page") int page);

    @GET("user/{username}/following")
    Call<ArrayList<Follow>> getFollowers(@Path("username") String username);

    @FormUrlEncoded
    @POST("animelist")
    Call<ResponseBody> addAnime(@Field("id") int id, @Field("list_status") String status, @Field("episodes_watched") int episodes,
                                @Field("score_raw") float score, @Field("notes") String notes, @Field("rewatched") int rewatched);

    @FormUrlEncoded
    @PUT("animelist")
    Call<ResponseBody> updateAnime(@Field("id") int id, @Field("list_status") String status, @Field("episodes_watched") int episodes,
                         @Field("score_raw") float score, @Field("notes") String notes, @Field("rewatched") int rewatched);

    @DELETE("animelist/{anime_id}")
    Call<ResponseBody> deleteAnime(@Path("anime_id") int anime_id);

    @DELETE("mangalist/{manga_id}")
    Call<ResponseBody> deleteManga(@Path("manga_id") int manga_id);

    @FormUrlEncoded
    @POST("mangalist")
    Call<ResponseBody> addManga(@Field("id") int id, @Field("list_status") String status, @Field("chapters_read") int chapters,
                      @Field("volumes_read") int volumes, @Field("score_raw") float score);

    @FormUrlEncoded
    @PUT("mangalist")
    Call<ResponseBody> updateManga(@Field("id") int id, @Field("list_status") String status, @Field("chapters_read") int chapters,
                         @Field("volumes_read") int volumes, @Field("score_raw") float score);

    /*
    TODO: Add forum support
    @GET("forum/recent")
    ForumMain getForumRecent();

    @GET("forum/new")
    ForumMain getForumNew();

    @GET("forum/subscribed")
    ForumMain getForumSubscribed();

    @GET("forum/thread/{id}")
    ForumMain getTopics(@Path("id") int id, @Query("page") int page);

    @GET("forum/search/{query}")
    ForumMain search(@Path("query") String query);

    @GET("forum/tag")
    ForumMain getAnime(@Path("tag") int id, @Query("page") int page);

    @FormUrlEncoded
    @POST("forum/{id}")
    Call<ResponseBody> addTopic(@Path("tags") int tags, @Path("tags_anime") int tags_anime, @Path("tags_manga") int tags_manga,
                      @Field("title") String title, @Field("body") String body);

    */

    @FormUrlEncoded
    @POST("forum/comment")
    Call<ResponseBody> addComment(@Field("thread_id") int id, @Field("comment") String message);

    @FormUrlEncoded
    @PUT("forum/comment")
    Call<ResponseBody> updateComment(@Field("id") int id, @Field("comment") String message);

    @GET("forum/search/{query}")
    Call<ForumAL> search(@Path("query") String query);

    @GET("forum/thread/{id}")
    Call<ForumThread> getPosts(@Path("id") int id, @Query("page") int page);

    @GET("forum/tag")
    Call<ForumAL> getTags(@Query("tags") int id, @Query("page") int page);

    @GET("anime/{id}/reviews")
    Call<ArrayList<Reviews>> getAnimeReviews(@Path("id") int id, @Query("page") int page);

    @GET("manga/{id}/reviews")
    Call<ArrayList<Reviews>> getMangaReviews(@Path("id") int id, @Query("page") int page);
}
