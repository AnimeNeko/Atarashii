package net.somethingdreadful.MAL.api;

import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import net.somethingdreadful.MAL.BuildConfig;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.BrowseAnimeList;
import net.somethingdreadful.MAL.api.ALModels.AnimeManga.BrowseMangaList;
import net.somethingdreadful.MAL.api.ALModels.Follow;
import net.somethingdreadful.MAL.api.ALModels.History;
import net.somethingdreadful.MAL.api.ALModels.OAuth;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class ALApi {
    private static String anilistURL = "http://anilist.co/api";
    private static String accesToken;

    //It's not best practice to use internals, but there is no other good way to get the OkHttp default UA
    private static final String okUa = com.squareup.okhttp.internal.Version.userAgent();
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ") " + okUa;

    ALInterface service;

    public ALApi() {
        setupRESTService();
    }

    public static String getAnilistURL() {
        return anilistURL + "/auth/authorize?grant_type=authorization_code&client_id=" + BuildConfig.ANILIST_CLIENT_ID + "&redirect_uri=" + BuildConfig.ANILIST_CLIENT_REDIRECT_URI + "&response_type=code";
    }

    public static String getCode(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter("code");
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void setupRESTService() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(45, TimeUnit.SECONDS);
        client.setReadTimeout(45, TimeUnit.SECONDS);
        client.setWriteTimeout(45, TimeUnit.SECONDS);

        client.interceptors().add(new UserAgentInterceptor(USER_AGENT));

        if (accesToken == null && AccountService.getAccount() != null)
            accesToken = AccountService.getAccesToken();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setVersion(2)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + accesToken);
                    }
                })
                .setEndpoint(anilistURL)
                .setConverter(new GsonConverter(gson))
                .build();
        service = restAdapter.create(ALInterface.class);
    }

    public OAuth getAuthCode(String code) {
        OAuth auth = service.getAuthCode("authorization_code", BuildConfig.ANILIST_CLIENT_ID, BuildConfig.ANILIST_CLIENT_SECRET, BuildConfig.ANILIST_CLIENT_REDIRECT_URI, code);
        accesToken = auth.access_token;
        setupRESTService();
        return auth;
    }

    public ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> getActivity(String username) {
        return History.convertBaseHistoryList(service.getActivity(username), username);
    }

    public Profile getCurrentUser() {
        return service.getCurrentUser().createBaseModel();
    }

    public Profile getProfile(String name) {
        return service.getProfile(name).createBaseModel();
    }

    public UserList getAnimeList(String username) {
        return service.getAnimeList(username).createBaseModel();
    }

    public UserList getMangaList(String username) {
        return service.getMangaList(username).createBaseModel();
    }

    public void getAccesToken() {
        OAuth auth = service.getAccesToken("refresh_token", BuildConfig.ANILIST_CLIENT_ID, BuildConfig.ANILIST_CLIENT_SECRET, AccountService.getRefreshToken());
        accesToken = AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
        setupRESTService();
    }

    public Anime getAnime(int id) {
        return service.getAnime(id).createBaseModel();
    }

    public Manga getManga(int id) {
        return service.getManga(id).createBaseModel();
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        return BrowseAnimeList.convertBaseArray(service.searchAnime(query, page));
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        return BrowseMangaList.convertBaseArray(service.searchManga(query, page));
    }

    public BrowseList getUpcomingManga(int page) {
        return service.getUpcomingManga(page).createBaseModel();
    }

    public BrowseList getUpcomingAnime(int page) {
        return service.getUpcomingAnime(page).createBaseModel();
    }

    public BrowseList getJustAddedManga(int page) {
        return service.getJustAddedManga(page).createBaseModel();
    }

    public BrowseList getJustAddedAnime(int page) {
        return service.getJustAddedAnime(page).createBaseModel();
    }

    public BrowseList getAiringAnime(int page) {
        return service.getAiringAnime(page).createBaseModel();
    }

    public BrowseList getPublishingManga(int page) {
        return service.getAiringManga(page).createBaseModel();
    }

    public BrowseList getYearAnime(int year, int page) {
        return service.getYearAnime(year, page).createBaseModel();
    }

    public BrowseList getYearManga(int year, int page) {
        return service.getYearManga(year, page).createBaseModel();
    }

    public ArrayList<Profile> getFollowers(String user) {
        return Follow.convertBaseFollowList(service.getFollowers(user));
    }

    public boolean addOrUpdateAnime(Anime anime) {
        boolean result;
        if (anime.getCreateFlag())
            result = service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore(), anime.getNotes(), anime.getRewatchCount()).getStatus() == 200;
        else
            result = service.updateAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore(), anime.getNotes(), anime.getRewatchCount()).getStatus() == 200;
        return result;
    }

    public boolean deleteAnimeFromList(int id) {
        return service.deleteAnime(id).getStatus() == 200;
    }

    public boolean deleteMangaFromList(int id) {
        return service.deleteManga(id).getStatus() == 200;
    }

    public boolean addOrUpdateManga(Manga manga) {
        boolean result;
        if (manga.getCreateFlag())
            result = service.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
        else
            result = service.updateManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
        return result;
    }

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews.convertBaseArray(service.getAnimeReviews(id, page));
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        return net.somethingdreadful.MAL.api.ALModels.AnimeManga.Reviews.convertBaseArray(service.getMangaReviews(id, page));
    }
}
