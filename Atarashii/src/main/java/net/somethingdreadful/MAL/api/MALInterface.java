package net.somethingdreadful.MAL.api;

import net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.AnimeList;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.MangaList;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.MALModels.ForumMain;
import net.somethingdreadful.MAL.api.MALModels.Profile;
import net.somethingdreadful.MAL.api.MALModels.Recommendations;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

interface MALInterface {
    @GET("api/account/verify_credentials.xml")
    Call<ResponseBody> verifyAuthentication();

    @GET("anime/{anime_id}")
    Call<Anime> getAnime(@Path("anime_id") int anime_id, @Query("mine") int mine);

    @GET("anime/search")
    Call<ArrayList<Anime>> searchAnime(@Query("q") String query, @Query("page") int page);

    @GET("anime/browse")
    Call<ArrayList<Anime>> getBrowseAnime(@QueryMap Map<String, String> params);

    @GET("manga/browse")
    Call<ArrayList<Manga>> getBrowseManga(@QueryMap Map<String, String> params);

    @GET("animelist/{username}")
    Call<AnimeList> getAnimeList(@Path("username") String username);

    @DELETE("animelist/anime/{anime_id}")
    Call<ResponseBody> deleteAnime(@Path("anime_id") int anime_id);

    @FormUrlEncoded
    @POST("animelist/anime")
    Call<ResponseBody> addAnime(@Field("anime_id") int id, @Field("status") String status, @Field("episodes") int episodes,
                                @Field("score") float score);

    @FormUrlEncoded
    @PUT("animelist/anime/{anime_id}")
    Call<ResponseBody> updateAnime(@Path("anime_id") int id, @FieldMap Map<String, String> params);

    @GET("manga/{manga_id}")
    Call<Manga> getManga(@Path("manga_id") int manga_id, @Query("mine") int mine);

    @GET("manga/search")
    Call<ArrayList<Manga>> searchManga(@Query("q") String query, @Query("page") int page);

    @GET("mangalist/{username}")
    Call<MangaList> getMangaList(@Path("username") String username);

    @DELETE("mangalist/manga/{manga_id}")
    Call<ResponseBody> deleteManga(@Path("manga_id") int manga_id);

    @FormUrlEncoded
    @POST("mangalist/manga")
    Call<ResponseBody> addManga(@Field("manga_id") int id, @Field("status") String status, @Field("chapters") int chapters,
                                @Field("volumes") int volumes, @Field("score") float score);

    @FormUrlEncoded
    @PUT("mangalist/manga/{manga_id}")
    Call<ResponseBody> updateManga(@Path("manga_id") int id, @FieldMap Map<String, String> params);

    @GET("profile/{username}")
    Call<Profile> getProfile(@Path("username") String username);

    @GET("friends/{username}")
    Call<ArrayList<net.somethingdreadful.MAL.api.MALModels.Friend>> getFriends(@Path("username") String username);

    @GET("forum")
    Call<ForumMain> getForum();

    @GET("forum/{id}")
    Call<ForumMain> getCategoryTopics(@Path("id") int id, @Query("page") int page);

    @GET("forum/anime/{id}")
    Call<ForumMain> getForumAnime(@Path("id") int id, @Query("page") int page);

    @GET("forum/manga/{id}")
    Call<ForumMain> getForumManga(@Path("id") int id, @Query("page") int page);

    @GET("forum/topic/{id}")
    Call<ForumMain> getPosts(@Path("id") int id, @Query("page") int page);

    @GET("forum/board/{id}")
    Call<ForumMain> getSubBoards(@Path("id") int id, @Query("page") int page);

    @GET("forum/search")
    Call<ForumMain> search(@Query("query") String query);

    @FormUrlEncoded
    @POST("forum/topic/{id}")
    Call<ResponseBody> addComment(@Path("id") int id, @Field("message") String message);

    @FormUrlEncoded
    @PUT("forum/topic/{id}")
    Call<ResponseBody> updateComment(@Path("id") int id, @Field("message") String message);

    @FormUrlEncoded
    @POST("forum/{id}")
    Call<ResponseBody> addTopic(@Path("id") int id, @Field("title") String title, @Field("message") String message);

    @GET("anime/reviews/{id}")
    Call<ArrayList<Reviews>> getAnimeReviews(@Path("id") int id, @Query("page") int page);

    @GET("manga/reviews/{id}")
    Call<ArrayList<Reviews>> getMangaReviews(@Path("id") int id, @Query("page") int page);

    @GET("history/{username}")
    Call<ArrayList<net.somethingdreadful.MAL.api.MALModels.History>> getActivity(@Path("username") String username);

    @GET("anime/recs/{id}")
    Call<ArrayList<Recommendations>> getAnimeRecs(@Path("id") int id);

    @GET("manga/recs/{id}")
    Call<ArrayList<Recommendations>> getMangaRecs(@Path("id") int id);

    @GET("anime/schedule")
    Call<Schedule> getSchedule();

    @GET("anime/popular")
    Call<ArrayList<Anime>> getPopularAnime(@Query("page") int page);

    @GET("anime/top")
    Call<ArrayList<Anime>> getTopRatedAnime(@Query("page") int page);

    @GET("manga/popular")
    Call<ArrayList<Manga>> getPopularManga(@Query("page") int page);

    @GET("manga/top")
    Call<ArrayList<Manga>> getTopRatedManga(@Query("page") int page);
}
