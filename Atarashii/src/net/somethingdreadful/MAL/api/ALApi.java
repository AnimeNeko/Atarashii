package net.somethingdreadful.MAL.api;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.somethingdreadful.MAL.BuildConfig;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALModels.Follow;
import net.somethingdreadful.MAL.api.ALModels.ForumAL;
import net.somethingdreadful.MAL.api.ALModels.ForumThread;
import net.somethingdreadful.MAL.api.ALModels.History;
import net.somethingdreadful.MAL.api.ALModels.OAuth;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ALApi {
    private static final String anilistURL = "https://anilist.co/api/";
    private static String accesToken;

    //It's not best practice to use internals, but there is no other good way to get the OkHttp default UA
    private static final String okUa = okhttp3.internal.Version.userAgent();
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ") " + okUa;
    private Activity activity = null;

    private ALInterface service;

    public ALApi() {
        setupRESTService();
    }

    public ALApi(Activity activity) {
        this.activity = activity;
        setupRESTService();
    }

    public static String getAnilistURL() {
        return anilistURL + "auth/authorize?grant_type=authorization_code&client_id=" + BuildConfig.ANILIST_CLIENT_ID + "&redirect_uri=" + BuildConfig.ANILIST_CLIENT_REDIRECT_URI + "&response_type=code";
    }

    public static String getCode(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter("code");
        } catch (Exception e) {
            return null;
        }
    }

    private void setupRESTService() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        if (accesToken == null && AccountService.getAccount() != null)
            accesToken = AccountService.getAccesToken();

        client.connectTimeout(60, TimeUnit.SECONDS);
        client.writeTimeout(60, TimeUnit.SECONDS);
        client.readTimeout(60, TimeUnit.SECONDS);
        client.interceptors().add(new APIInterceptor(USER_AGENT, "Bearer " + accesToken));

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setVersion(2)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client.build())
                .baseUrl(anilistURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(ALInterface.class);
    }

    public OAuth getAuthCode(String code) {
        retrofit2.Response<OAuth> response = null;
        OAuth auth = null;
        try {
            response = service.getAuthCode("authorization_code", BuildConfig.ANILIST_CLIENT_ID, BuildConfig.ANILIST_CLIENT_SECRET, BuildConfig.ANILIST_CLIENT_REDIRECT_URI, code).execute();
            auth = response.body();
            accesToken = auth.access_token;
            setupRESTService();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAuthCode", e);
        }
        return auth;
    }

    public ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> getActivity(String username, int page) {
        retrofit2.Response<ArrayList<History>> response = null;
        try {
            response = service.getActivity(username, page).execute();
            return History.convertBaseHistoryList(response.body(), username);
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getActivity", e);
            return new ArrayList<>();
        }
    }

    public Profile getCurrentUser() {
        retrofit2.Response<net.somethingdreadful.MAL.api.ALModels.Profile> response = null;
        try {
            response = service.getCurrentUser().execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getCurrentUser", e);
            return null;
        }
    }

    public Profile getProfile(String username) {
        retrofit2.Response<net.somethingdreadful.MAL.api.ALModels.Profile> response = null;
        try {
            response = service.getProfile(username).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getProfile", e);
            return null;
        }
    }

    public UserList getAnimeList(String username) {
        retrofit2.Response<net.somethingdreadful.MAL.api.ALModels.AnimeManga.UserList> response = null;
        try {
            response = service.getAnimeList(username).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAnimeList", e);
            return null;
        }
    }

    public UserList getMangaList(String username) {
        retrofit2.Response<net.somethingdreadful.MAL.api.ALModels.AnimeManga.UserList> response = null;
        try {
            response = service.getMangaList(username).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getMangaList", e);
            return null;
        }
    }

    public void getAccesToken() {
        retrofit2.Response<OAuth> response = null;
        try {
            response = service.getAccesToken("refresh_token", BuildConfig.ANILIST_CLIENT_ID, BuildConfig.ANILIST_CLIENT_SECRET, AccountService.getRefreshToken()).execute();
            OAuth auth = response.body();
            accesToken = AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
            setupRESTService();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAccesToken", e);
        }
    }

    public Anime getAnime(int id) {
        retrofit2.Response<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime> response = null;
        try {
            response = service.getAnime(id).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAnime", e);
            return null;
        }
    }

    public Manga getManga(int id) {
        retrofit2.Response<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Manga> response = null;
        try {
            response = service.getManga(id).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getManga", e);
            return null;
        }
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.searchAnime(query, page).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "searchAnime", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.searchManga(query, page).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Manga.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "searchManga", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Profile> getFollowing(String user) {
        retrofit2.Response<ArrayList<Follow>> response = null;
        try {
            response = service.getFollowing(user).execute();
            return Follow.convertBaseFollowList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getFollowing", e);
            return null;
        }
    }

    public ArrayList<Profile> getFollowers(String user) {
        retrofit2.Response<ArrayList<Follow>> response = null;
        try {
            response = service.getFollowers(user).execute();
            return Follow.convertBaseFollowList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getFollowers", e);
            return null;
        }
    }

    public boolean addOrUpdateAnime(Anime anime) {
        if (anime.getCreateFlag())
            return APIHelper.isOK(service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore(), anime.getNotes(), anime.getRewatchCount()), "addAnime");
        else
            return APIHelper.isOK(service.updateAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore(), anime.getNotes(), anime.getRewatchCount()), "updateAnime");
    }

    public boolean deleteAnimeFromList(int id) {
        return APIHelper.isOK(service.deleteAnime(id), "deleteAnimeFromList");
    }

    public boolean deleteMangaFromList(int id) {
        return APIHelper.isOK(service.deleteManga(id), "deleteMangaFromList");
    }

    public boolean addOrUpdateManga(Manga manga) {
        if (manga.getCreateFlag())
            return APIHelper.isOK(service.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()), "addManga");
        else
            return APIHelper.isOK(service.updateManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()), "updateManga");
    }

    public ForumThread getPosts(int id, int page) {
        retrofit2.Response<ForumThread> response = null;
        try {
            response = service.getPosts(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getPosts", e);
            return null;
        }
    }

    public ForumAL getTags(int id, int page) {
        retrofit2.Response<ForumAL> response = null;
        try {
            response = service.getTags(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getTags", e);
            return null;
        }
    }

    public boolean addComment(int id, String message) {
        return APIHelper.isOK(service.addComment(id, message), "addComment");
    }

    public boolean updateComment(int id, String message) {
        return APIHelper.isOK(service.updateComment(id, message), "updateComment");
    }

    public ForumAL search(String Query) {
        retrofit2.Response<ForumAL> response = null;
        try {
            response = service.search(Query).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "search", e);
            return null;
        }
    }

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews>> response = null;
        try {
            response = service.getAnimeReviews(id, page).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAnimeReviews", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews>> response = null;
        try {
            response = service.getMangaReviews(id, page).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getMangaReviews", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Anime> getMostPopularAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "popularity-desc");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getMostPopularManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "popularity-desc");
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public ArrayList<Anime> getTopRatedAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "score-desc");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getTopRatedManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "score-desc");
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public ArrayList<Anime> getJustAddedAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "id-desc");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getJustAddedManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "id-desc");
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public ArrayList<Anime> getUpcomingAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "id-desc");
        map.put("status", "Not Yet Aired");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getUpcomingManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "id-desc");
        map.put("status", "Not Yet Published");
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public Schedule getSchedule() {
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "Tv");
        map.put("airing_data", "true");
        map.put("status", "Currently Airing");
        map.put("full_page", "true");
        return getBrowseSchedule(map);
    }

    public ArrayList<Anime> getPopularSeasonAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "popularity-desc");
        map.put("status", "Currently Airing");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getPopularSeasonManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "popularity-desc");
        map.put("status", "Currently Airing");
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public ArrayList<Anime> getPopularYearAnime(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "popularity-desc");
        map.put("year", sdf.format(new Date()));
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getPopularYearManga(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "popularity-desc");
        map.put("year", sdf.format(new Date()));
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public ArrayList<Anime> getTopSeasonAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "score-desc");
        map.put("status", "Currently Airing");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getTopSeasonManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "score-desc");
        map.put("status", "Currently Airing");
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    public ArrayList<Anime> getTopYearAnime(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "score-desc");
        map.put("year", sdf.format(new Date()));
        map.put("page", String.valueOf(page));
        return getBrowseAnime(map);
    }

    public ArrayList<Manga> getTopYearManga(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "score-desc");
        map.put("year", sdf.format(new Date()));
        map.put("page", String.valueOf(page));
        return getBrowseManga(map);
    }

    private ArrayList<Anime> getBrowseAnime(Map<String, String> queries) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.getBrowseAnime(queries).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getBrowseAnime: " + queries.toString(), e);
            return null;
        }
    }

    private Schedule getBrowseSchedule(Map<String, String> queries) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.getBrowseAnime(queries).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.convertBaseSchedule(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getBrowseAnime: " + queries.toString(), e);
            return null;
        }
    }

    private ArrayList<Manga> getBrowseManga(Map<String, String> queries) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.ALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.getBrowseManga(queries).execute();
            return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Manga.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getBrowseManga: " + queries.toString(), e);
            return null;
        }
    }
}
