package net.somethingdreadful.MAL.api;

import android.app.Activity;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.AnimeList;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.MangaList;
import net.somethingdreadful.MAL.api.MALModels.ForumMain;
import net.somethingdreadful.MAL.api.MALModels.Friend;
import net.somethingdreadful.MAL.api.MALModels.History;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MALApi {
    // Use version 2.0 of the API interface
    private static final String API_HOST = "https://api.atarashiiapp.com/2/";

    //It's not best practice to use internals, but there is no other good way to get the OkHttp default UA
    private static final String okUa = okhttp3.internal.Version.userAgent();
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ") " + okUa;
    private Activity activity = null;

    private MALInterface service;
    private String username;

    public MALApi() {
        username = AccountService.getUsername();
        setupRESTService(username, AccountService.getPassword());
    }

    public MALApi(Activity activity) {
        this.activity = activity;
        username = AccountService.getUsername();
        setupRESTService(username, AccountService.getPassword());
    }

    /*
     * Only use for verifying.
     */
    public MALApi(String username, String password) {
        setupRESTService(username, password);
    }

    public static String getListTypeString(ListType type) {
        return type.name().toLowerCase();
    }

    private void setupRESTService(String username, String password) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(60, TimeUnit.SECONDS);
        client.writeTimeout(60, TimeUnit.SECONDS);
        client.readTimeout(60, TimeUnit.SECONDS);
        client.interceptors().add(new UserAgentInterceptor(USER_AGENT));

        final String credential = Credentials.basic(username, password);
        client.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, okhttp3.Response response) throws IOException {
                if (credential.equals(response.request().header("Authorization"))) {
                    return null; //If we already failed when trying the credentials, exit and don't retry
                }

                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }
        });

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setVersion(1)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client.build())
                .baseUrl(API_HOST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(MALInterface.class);
    }

    public boolean isAuth() {
        return APIHelper.isOK(service.verifyAuthentication(), "isAuth");
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.searchAnime(query, page).execute();
            return AnimeList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "searchAnime", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.searchManga(query, page).execute();
            return MangaList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "searchManga", e);
            return new ArrayList<>();
        }
    }

    public UserList getAnimeList() {
        Response<AnimeList> response = null;
        try {
            response = service.getAnimeList(username).execute();
            return AnimeList.createBaseModel(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAnimeList", e);
            return null;
        }
    }

    public UserList getMangaList() {
        Response<MangaList> response = null;
        try {
            response = service.getMangaList(username).execute();
            return MangaList.createBaseModel(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getMangaList", e);
            return null;
        }
    }

    public Anime getAnime(int id, int mine) {
        Response<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime> response = null;
        try {
            response = service.getAnime(id, mine).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAnime", e);
            return null;
        }
    }

    public Manga getManga(int id, int mine) {
        Response<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga> response = null;
        try {
            response = service.getManga(id, mine).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getManga", e);
            return null;
        }
    }

    public boolean addOrUpdateAnime(Anime anime) {
        if (anime.getCreateFlag())
            return APIHelper.isOK(service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()), "addOrUpdateAnime");
        else {
            if (anime.isDirty()) {
                // map anime property names to api field names
                HashMap<String, String> nameMap = new HashMap<>();
                nameMap.put("watchedStatus", "status");
                nameMap.put("watchedEpisodes", "episodes");
                nameMap.put("score", "score");
                nameMap.put("watchingStart", "start");
                nameMap.put("watchingEnd", "end");
                nameMap.put("priority", "priority");
                nameMap.put("personalTags", "tags");
                nameMap.put("personalComments", "comments");
                nameMap.put("fansubGroup", "fansubber");
                nameMap.put("storage", "storage_type");
                nameMap.put("storageValue", "storage_amt");
                nameMap.put("epsDownloaded", "downloaded_eps");
                nameMap.put("rewatchCount", "rewatch_count");
                nameMap.put("rewatchValue", "rewatch_value");
                HashMap<String, String> fieldMap = new HashMap<>();
                for (String dirtyField : anime.getDirty()) {
                    if (nameMap.containsKey(dirtyField)) {
                        if (anime.getPropertyType(dirtyField) == String.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getStringPropertyValue(dirtyField));
                        } else if (anime.getPropertyType(dirtyField) == int.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getIntegerPropertyValue(dirtyField).toString());
                        } else if (anime.getPropertyType(dirtyField) == ArrayList.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getArrayPropertyValue(dirtyField));
                        }
                    }
                }
                return APIHelper.isOK(service.updateAnime(anime.getId(), fieldMap), "addOrUpdateAnime");
            }
        }
        return false;
    }

    public boolean addOrUpdateManga(Manga manga) {
        if (manga.getCreateFlag())
            return APIHelper.isOK(service.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()), "addOrUpdateManga");
        else {
            if (manga.isDirty()) {
                // map manga property names to api field names
                HashMap<String, String> nameMap = new HashMap<>();
                nameMap.put("readStatus", "status");
                nameMap.put("chaptersRead", "chapters");
                nameMap.put("volumesRead", "volumes");
                nameMap.put("score", "score");
                nameMap.put("readingStart", "start");
                nameMap.put("readingEnd", "end");
                nameMap.put("priority", "priority");
                nameMap.put("personalTags", "tags");
                nameMap.put("rereadValue", "reread_value");
                nameMap.put("rereadCount", "reread_count");
                nameMap.put("personalComments", "comments");
                HashMap<String, String> fieldMap = new HashMap<>();
                for (String dirtyField : manga.getDirty()) {
                    if (nameMap.containsKey(dirtyField)) {
                        if (manga.getPropertyType(dirtyField) == String.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getStringPropertyValue(dirtyField));
                        } else if (manga.getPropertyType(dirtyField) == int.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getIntegerPropertyValue(dirtyField).toString());
                        } else if (manga.getPropertyType(dirtyField) == ArrayList.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getArrayPropertyValue(dirtyField));
                        }
                    }
                }
                return APIHelper.isOK(service.updateManga(manga.getId(), fieldMap), "addOrUpdateManga");
            }
        }
        return false;
    }

    public boolean deleteAnimeFromList(int id) {
        return APIHelper.isOK(service.deleteAnime(id), "deleteAnimeFromList");
    }

    public boolean deleteMangaFromList(int id) {
        return APIHelper.isOK(service.deleteManga(id), "deleteMangaFromList");
    }

    public BrowseList getMostPopularAnime(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.getPopularAnime(page).execute();
            return AnimeList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getMostPopularAnime", e);
            return null;
        }
    }

    public BrowseList getMostPopularManga(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.getPopularManga(page).execute();
            return MangaList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getMostPopularManga", e);
            return null;
        }
    }

    public BrowseList getTopRatedAnime(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.getTopRatedAnime(page).execute();
            return AnimeList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getTopRatedAnime", e);
            return null;
        }
    }

    public BrowseList getTopRatedManga(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.getTopRatedManga(page).execute();
            return MangaList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getTopRatedManga", e);
            return null;
        }
    }

    public BrowseList getJustAddedAnime(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.getJustAddedAnime(page).execute();
            return AnimeList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getJustAddedAnime", e);
            return null;
        }
    }

    public BrowseList getJustAddedManga(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.getJustAddedManga(page).execute();
            return MangaList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getJustAddedManga", e);
            return null;
        }
    }

    public BrowseList getUpcomingAnime(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = service.getUpcomingAnime(page).execute();
            return AnimeList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getUpcomingAnime", e);
            return null;
        }
    }

    public BrowseList getUpcomingManga(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = service.getUpcomingManga(page).execute();
            return MangaList.convertBaseBrowseList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getUpcomingManga", e);
            return null;
        }
    }

    public Profile getProfile(String user) {
        Response<net.somethingdreadful.MAL.api.MALModels.Profile> response = null;
        try {
            response = service.getProfile(user).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getProfile", e);
            return null;
        }
    }

    public ArrayList<Profile> getFriends(String user) {
        Response<ArrayList<Friend>> response = null;
        try {
            response = service.getFriends(user).execute();
            return Friend.convertBaseFriendList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getFriends", e);
            return new ArrayList<>();
        }
    }

    public ForumMain getForum() {
        Response<ForumMain> response = null;
        try {
            response = service.getForum().execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getForum", e);
            return null;
        }
    }

    public ForumMain getCategoryTopics(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = service.getCategoryTopics(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getCategoryTopics", e);
            return null;
        }
    }

    public ForumMain getForumAnime(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = service.getForumAnime(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getForumAnime", e);
            return null;
        }
    }

    public ForumMain getForumManga(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = service.getForumManga(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getForumManga", e);
            return null;
        }
    }

    public ForumMain getPosts(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = service.getPosts(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getPosts", e);
            return null;
        }
    }

    public ForumMain getSubBoards(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = service.getSubBoards(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getSubBoards", e);
            return null;
        }
    }

    public boolean addComment(int id, String message) {
        return APIHelper.isOK(service.addComment(id, message), "addComment");
    }

    public boolean updateComment(int id, String message) {
        return APIHelper.isOK(service.updateComment(id, message), "updateComment");
    }

    public boolean addTopic(int id, String title, String message) {
        return APIHelper.isOK(service.addTopic(id, title, message), "addTopic");
    }

    public ForumMain search(String query) {
        Response<ForumMain> response = null;
        try {
            response = service.search(query).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "search", e);
            return null;
        }
    }

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews>> response = null;
        try {
            response = service.getAnimeReviews(id, page).execute();
            return net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getAnimeReviews", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews>> response = null;
        try {
            response = service.getMangaReviews(id, page).execute();
            return net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getMangaReviews", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> getActivity(String username) {
        Response<ArrayList<History>> response = null;
        try {
            response = service.getActivity(username).execute();
            return History.convertBaseHistoryList(response.body(), username);
        } catch (Exception e) {
            APIHelper.logE(activity, response, getClass().getSimpleName(), "getActivity", e);
            return new ArrayList<>();
        }
    }

    public enum ListType {
        ANIME,
        MANGA
    }
}
