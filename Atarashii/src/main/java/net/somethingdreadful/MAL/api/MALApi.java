package net.somethingdreadful.MAL.api;

import android.app.Activity;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.AnimeList;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.MangaList;
import net.somethingdreadful.MAL.api.MALModels.ForumMain;
import net.somethingdreadful.MAL.api.MALModels.Friend;
import net.somethingdreadful.MAL.api.MALModels.History;
import net.somethingdreadful.MAL.api.MALModels.Recommendations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Credentials;
import retrofit2.Response;

public class MALApi {
    // Use version 2.1 of the API interface
    private static final String API_HOST = "https://malapi.atarashiiapp.com/2.1/";
    private static final String MAL_HOST = "https://myanimelist.net/";
    private Activity activity = null;

    private MALInterface APIservice;
    private MALInterface MALservice;

    public MALApi() {
        setupRESTService(AccountService.getUsername(), AccountService.getPassword());
    }

    public MALApi(Activity activity) {
        this.activity = activity;
        setupRESTService(AccountService.getUsername(), AccountService.getPassword());
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
        APIservice = APIHelper.createClient(API_HOST, MALInterface.class, Credentials.basic(username, password));
        MALservice = APIHelper.createClient(MAL_HOST, MALInterface.class, Credentials.basic(username, password));
    }

    public boolean isAuth() {
        return APIHelper.isOK(MALservice.verifyAuthentication(), "isAuth");
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        if (PrefManager.getNSFWEnabled()) {
            Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
            try {
                response = APIservice.searchAnime(query, page).execute();
                return AnimeList.convertBaseArray(response.body());
            } catch (Exception e) {
                APIHelper.logE(activity, response, "MALApi", "searchAnime", e);
                return new ArrayList<>();
            }
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put("keyword", query);
            map.put("page", String.valueOf(page));
            map.put("genre_type", "1");
            map.put("genres", "Hentai");
            return getBrowseAnime(checkNSFW(map));
        }
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        if (PrefManager.getNSFWEnabled()) {
            Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
            try {
                response = APIservice.searchManga(query, page).execute();
                return MangaList.convertBaseArray(response.body());
            } catch (Exception e) {
                APIHelper.logE(activity, response, "MALApi", "searchManga", e);
                return new ArrayList<>();
            }
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put("keyword", query);
            map.put("page", String.valueOf(page));
            map.put("genre_type", "1");
            map.put("genres", "Hentai");
            return getBrowseManga(checkNSFW(map));
        }
    }

    public UserList getAnimeList(String username) {
        Response<AnimeList> response = null;
        try {
            response = APIservice.getAnimeList(username).execute();
            return AnimeList.createBaseModel(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getAnimeList", e);
            return null;
        }
    }

    public UserList getMangaList(String username) {
        Response<MangaList> response = null;
        try {
            response = APIservice.getMangaList(username).execute();
            return MangaList.createBaseModel(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getMangaList", e);
            return null;
        }
    }

    public Anime getAnime(int id, int mine) {
        Response<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime> response = null;
        try {
            response = APIservice.getAnime(id, mine).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getAnime", e);
            return null;
        }
    }

    public Manga getManga(int id, int mine) {
        Response<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga> response = null;
        try {
            response = APIservice.getManga(id, mine).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getManga", e);
            return null;
        }
    }

    public boolean addOrUpdateAnime(Anime anime) {
        if (anime.getCreateFlag())
            return APIHelper.isOK(APIservice.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()), "addOrUpdateAnime");
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
                nameMap.put("notes", "comments");
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
                return APIHelper.isOK(APIservice.updateAnime(anime.getId(), fieldMap), "addOrUpdateAnime");
            }
        }
        return false;
    }

    public boolean addOrUpdateManga(Manga manga) {
        if (manga.getCreateFlag())
            return APIHelper.isOK(APIservice.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()), "addOrUpdateManga");
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
                nameMap.put("notes", "comments");
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
                return APIHelper.isOK(APIservice.updateManga(manga.getId(), fieldMap), "addOrUpdateManga");
            }
        }
        return false;
    }

    public boolean deleteAnimeFromList(int id) {
        return APIHelper.isOK(APIservice.deleteAnime(id), "deleteAnimeFromList");
    }

    public boolean deleteMangaFromList(int id) {
        return APIHelper.isOK(APIservice.deleteManga(id), "deleteMangaFromList");
    }

    public ArrayList<Anime> getBrowseAnime(Map<String, String> queries) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        AppLog.log(Log.INFO, "Atarashii", "MALApi.getBrowseAnime(): queries=" + queries.toString());
        try {
            response = APIservice.getBrowseAnime(queries).execute();
            return AnimeList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getBrowseAnime: " + queries.toString(), e);
            return null;
        }
    }

    public ArrayList<Manga> getBrowseManga(Map<String, String> queries) {
        retrofit2.Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        AppLog.log(Log.INFO, "Atarashii", "MALApi.getBrowseManga(): queries=" + queries.toString());
        try {
            response = APIservice.getBrowseManga(queries).execute();
            return MangaList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getBrowseManga: " + queries.toString(), e);
            return null;
        }
    }

    private HashMap<String, String> checkNSFW(HashMap<String, String> map) {
        if (!PrefManager.getNSFWEnabled()) {
            map.put("genre_type", "1");
            map.put("genres", "Hentai");
        }
        return map;
    }

    public ArrayList<Anime> getMostPopularAnime(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = APIservice.getPopularAnime(page).execute();
            return AnimeList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getMostPopularAnime: page =" + page, e);
            return null;
        }
    }

    public ArrayList<Manga> getMostPopularManga(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = APIservice.getPopularManga(page).execute();
            return MangaList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getMostPopularManga: page =" + page, e);
            return null;
        }
    }

    public ArrayList<Anime> getTopRatedAnime(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime>> response = null;
        try {
            response = APIservice.getTopRatedAnime(page).execute();
            return AnimeList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getTopRatedAnime: page =" + page, e);
            return null;
        }
    }

    public ArrayList<Manga> getTopRatedManga(int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga>> response = null;
        try {
            response = APIservice.getTopRatedManga(page).execute();
            return MangaList.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getTopRatedManga: page =" + page, e);
            return null;
        }
    }

    public ArrayList<Anime> getJustAddedAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "9");
        map.put("reverse", "1");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(checkNSFW(map));
    }

    public ArrayList<Manga> getJustAddedManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "9");
        map.put("reverse", "1");
        map.put("page", String.valueOf(page));
        return getBrowseManga(checkNSFW(map));
    }

    public ArrayList<Anime> getUpcomingAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        map.put("sort", "2");
        map.put("reverse", "0");
        map.put("start_date", sdf.format(new Date()));
        map.put("page", String.valueOf(page));
        return getBrowseAnime(checkNSFW(map));
    }

    public ArrayList<Manga> getUpcomingManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        map.put("sort", "2");
        map.put("reverse", "0");
        map.put("start_date", sdf.format(new Date()));
        map.put("page", String.valueOf(page));
        return getBrowseManga(checkNSFW(map));
    }

    public Profile getProfile(String user) {
        Response<net.somethingdreadful.MAL.api.MALModels.Profile> response = null;
        try {
            response = APIservice.getProfile(user).execute();
            return response.body().createBaseModel();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getProfile", e);
            return null;
        }
    }

    public ArrayList<Profile> getFriends(String user) {
        Response<ArrayList<Friend>> response = null;
        try {
            response = APIservice.getFriends(user).execute();
            return Friend.convertBaseFriendList(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getFriends", e);
            return new ArrayList<>();
        }
    }

    public ForumMain getForum() {
        Response<ForumMain> response = null;
        try {
            response = APIservice.getForum().execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getForum", e);
            return null;
        }
    }

    public ForumMain getCategoryTopics(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = APIservice.getCategoryTopics(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getCategoryTopics", e);
            return null;
        }
    }

    public ForumMain getForumAnime(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = APIservice.getForumAnime(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getForumAnime", e);
            return null;
        }
    }

    public ForumMain getForumManga(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = APIservice.getForumManga(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getForumManga", e);
            return null;
        }
    }

    public ForumMain getPosts(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = APIservice.getPosts(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getPosts", e);
            return null;
        }
    }

    public ForumMain getSubBoards(int id, int page) {
        Response<ForumMain> response = null;
        try {
            response = APIservice.getSubBoards(id, page).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getSubBoards", e);
            return null;
        }
    }

    public boolean addComment(int id, String message) {
        return APIHelper.isOK(APIservice.addComment(id, message), "addComment");
    }

    public boolean updateComment(int id, String message) {
        return APIHelper.isOK(APIservice.updateComment(id, message), "updateComment");
    }

    public boolean addTopic(int id, String title, String message) {
        return APIHelper.isOK(APIservice.addTopic(id, title, message), "addTopic");
    }

    public ForumMain search(String query) {
        Response<ForumMain> response = null;
        try {
            response = APIservice.search(query).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "search", e);
            return null;
        }
    }

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews>> response = null;
        try {
            response = APIservice.getAnimeReviews(id, page).execute();
            return net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getAnimeReviews", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        Response<ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews>> response = null;
        try {
            response = APIservice.getMangaReviews(id, page).execute();
            return net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews.convertBaseArray(response.body());
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getMangaReviews", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> getActivity(String username) {
        Response<ArrayList<History>> response = null;
        try {
            response = APIservice.getActivity(username).execute();
            return History.convertBaseHistoryList(response.body(), username);
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getActivity", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Recommendations> getAnimeRecs(int id) {
        Response<ArrayList<Recommendations>> response = null;
        try {
            response = APIservice.getAnimeRecs(id).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getAnimeRecs", e);
            return new ArrayList<>();
        }
    }

    public ArrayList<Recommendations> getMangaRecs(int id) {
        Response<ArrayList<Recommendations>> response = null;
        try {
            response = APIservice.getMangaRecs(id).execute();
            return response.body();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getMangaRecs", e);
            return new ArrayList<>();
        }
    }

    public Schedule getSchedule() {
        Response<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Schedule> response = null;
        try {
            response = APIservice.getSchedule().execute();
            return response.body().convertBaseSchedule();
        } catch (Exception e) {
            APIHelper.logE(activity, response, "MALApi", "getSchedule", e);
            return new Schedule();
        }
    }

    public ArrayList<Anime> getPopularSeasonAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "7");
        map.put("reverse", "1");
        map.put("status", "1");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(checkNSFW(map));
    }

    public ArrayList<Manga> getPopularSeasonManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "7");
        map.put("reverse", "1");
        map.put("status", "1");
        map.put("page", String.valueOf(page));
        return getBrowseManga(checkNSFW(map));
    }

    public ArrayList<Anime> getPopularYearAnime(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "7");
        map.put("reverse", "1");
        map.put("start_date", sdf.format(new Date()) + "-01-01");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(checkNSFW(map));
    }

    public ArrayList<Manga> getPopularYearManga(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "7");
        map.put("reverse", "1");
        map.put("start_date", sdf.format(new Date()) + "-01-01");
        map.put("page", String.valueOf(page));
        return getBrowseManga(checkNSFW(map));
    }

    public ArrayList<Anime> getTopSeasonAnime(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "3");
        map.put("reverse", "1");
        map.put("status", "1");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(checkNSFW(map));
    }

    public ArrayList<Manga> getTopSeasonManga(int page) {
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "3");
        map.put("reverse", "1");
        map.put("status", "1");
        map.put("page", String.valueOf(page));
        return getBrowseManga(checkNSFW(map));
    }

    public ArrayList<Anime> getTopYearAnime(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "3");
        map.put("reverse", "1");
        map.put("start_date", sdf.format(new Date()) + "-01-01");
        map.put("page", String.valueOf(page));
        return getBrowseAnime(checkNSFW(map));
    }

    public ArrayList<Manga> getTopYearManga(int page) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        HashMap<String, String> map = new HashMap<>();
        map.put("sort", "3");
        map.put("reverse", "1");
        map.put("start_date", sdf.format(new Date()) + "-01-01");
        map.put("page", String.valueOf(page));
        return getBrowseManga(checkNSFW(map));
    }

    public enum ListType {
        ANIME,
        MANGA
    }
}
