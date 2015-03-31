package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Activity;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.sql.DatabaseManager;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit.RetrofitError;

public class MALManager {
    MALApi malApi;
    ALApi alApi;
    DatabaseManager dbMan;
    Context context;

    public MALManager(Context context) {
        if (AccountService.isMAL())
            malApi = new MALApi();
        else
            alApi = new ALApi();
        dbMan = new DatabaseManager(context);
        this.context = context;
    }

    public static String listSortFromInt(int i, MALApi.ListType type) {
        String r;

        switch (i) {
            case 0:
                r = "";
                break;
            case 1:
                if (type.equals(MALApi.ListType.ANIME))
                    r = Anime.STATUS_WATCHING;
                else
                    r = Manga.STATUS_READING;
                break;
            case 2:
                r = Anime.STATUS_COMPLETED;
                break;
            case 3:
                r = Anime.STATUS_ONHOLD;
                break;
            case 4:
                r = Anime.STATUS_DROPPED;
                break;
            case 5:
                if (type.equals(MALApi.ListType.ANIME))
                    r = Anime.STATUS_PLANTOWATCH;
                else
                    r = Manga.STATUS_PLANTOREAD;
                break;
            case 6:
                r = Anime.STATUS_REWATCHING;
                break;
            default:
                if (type.equals(MALApi.ListType.ANIME))
                    r = Anime.STATUS_WATCHING;
                else
                    r = Manga.STATUS_READING;
                break;
        }

        return r;
    }

    public Anime getAnimeRecord(int id) {
        try {
            if (AccountService.isMAL())
                return malApi.getAnime(id);
            else
                return alApi.getAnime(id);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getAnimeRecord(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public Anime getAnime(int id, String username) {
        try {
            return dbMan.getAnime(id, username);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getAnime(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public Manga getMangaRecord(int id) {
        try {
            if (AccountService.isMAL())
                return malApi.getManga(id);
            else
                return alApi.getManga(id);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getMangaRecord(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public ForumMain getForum() {
        try {
            return malApi.getForum();
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getForum(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public ForumMain getTopics(int id, int page) {
        try {
            return malApi.getTopics(id, page);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getTopics(" + id + ", " + page + "): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public ForumMain getDiscussion(int id, int page, MALApi.ListType type) {
        try {
            if (type.equals(MALApi.ListType.ANIME))
                return malApi.getAnime(id, page);
            else
                return malApi.getManga(id, page);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getDiscussion(" + id + ", " + page + "): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public ForumMain getPosts(int id, int page) {
        try {
            return malApi.getPosts(id, page);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getPosts(" + id + ", " + page + "): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public ForumMain getSubBoards(int id, int page) {
        try {
            return malApi.getSubBoards(id, page);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getSubBoards(" + id + ", " + page + "): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public Boolean addComment(int id, String message) {
        return malApi.addComment(id, message);
    }

    public Boolean updateComment(int id, String message) {
        return malApi.updateComment(id, message);
    }

    public Boolean addTopic(int id, String title, String message) {
        return malApi.addTopic(id, title, message);
    }

    public ForumMain search(String query) {
        try {
            return malApi.search(query);
        } catch (RetrofitError e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.search(" + query + "): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    public ArrayList<Anime> downloadAndStoreAnimeList(String username) {
        ArrayList<Anime> result = null;
        AnimeList animeList;
        if (AccountService.isMAL())
            animeList = malApi.getAnimeList();
        else
            animeList = alApi.getAnimeList(username);

        if (animeList != null) {
            result = animeList.getAnimes();
            dbMan.saveAnimeList(result, username);
            dbMan.cleanupAnimeTable();
        }
        return result;
    }

    public ArrayList<Manga> downloadAndStoreMangaList(String username) {
        ArrayList<Manga> result = null;
        MangaList mangaList;
        if (AccountService.isMAL())
            mangaList = malApi.getMangaList();
        else
            mangaList = alApi.getMangaList(username);

        if (mangaList != null) {
            result = mangaList.getMangas();
            dbMan.saveMangaList(result, username);
            dbMan.cleanupMangaTable();
        }
        return result;
    }

    public ArrayList<Anime> getAnimeListFromDB(String ListType, String username) {
        return dbMan.getAnimeList(ListType, username);
    }

    public ArrayList<Manga> getMangaListFromDB(String ListType, String username) {
        return dbMan.getMangaList(ListType, username);
    }

    public Anime updateWithDetails(int id, Anime anime, String username) {
        Crashlytics.log(Log.DEBUG, "MALX", "MALManager.updateWithDetails(" + Integer.toString(id) + ", " + username + ")");
        Anime anime_api;
        if (AccountService.isMAL())
            anime_api = malApi.getAnime(id);
        else
            anime_api = alApi.getAnime(id).createBaseModel();
        if (anime_api != null) {
            dbMan.saveAnime(anime_api, false, username);
            return AccountService.isMAL() ? anime_api : dbMan.getAnime(id, AccountService.getUsername());
        }
        return anime;
    }

    public ArrayList<User> downloadAndStoreFriendList(String user) {
        ArrayList<User> result;
        try {
            Crashlytics.log(Log.DEBUG, "MALX", "MALManager.downloadAndStoreFriendList(): Downloading friendlist of " + user);
            if (AccountService.isMAL())
                result = malApi.getFriends(user);
            else
                result = alApi.getFollowers(user);

            if (result != null && result.size() > 0) {
                dbMan.saveFriendList(result, user);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.downloadAndStoreFriendList(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return dbMan.getFriendList(user);
    }

    public ArrayList<User> getFriendListFromDB(String username) {
        return dbMan.getFriendList(username);
    }

    public User downloadAndStoreProfile(String name) {
        User result;
        try {
            Crashlytics.log(Log.DEBUG, "MALX", "MALManager.downloadAndStoreProfile(): Downloading profile of " + name);
            Profile profile;
            if (AccountService.isMAL())
                profile = malApi.getProfile(name);
            else
                profile = alApi.getProfile(name);

            result = new User();
            result.setName(name);
            result.setProfile(profile);

            if (profile != null && AccountService.isMAL()) {
                dbMan.saveUser(result, true);
            } else if (profile != null && !AccountService.isMAL()) {
                dbMan.saveProfile(profile);
            } else if (profile != null)
                result.getProfile().setAvatarUrl(profile.getImageUrl());
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.downloadAndStoreProfile(): " + e.getMessage());
            Crashlytics.logException(e);
            result = null;
        }
        return result;
    }

    public void verifyAuthentication() {
        if (AccountService.isMAL())
            malApi.verifyAuthentication();
        else if (AccountService.getAccesToken() == null)
            alApi.getAccesToken();
    }

    public User getProfileFromDB(String name) {
        return dbMan.getProfile(name);
    }

    public Manga updateWithDetails(int id, Manga manga, String username) {
        Manga manga_api = malApi.getManga(id);
        if (manga_api != null) {
            dbMan.saveManga(manga_api, false, username);
            return manga_api;
        }
        return manga;
    }

    public void saveAnimeToDatabase(Anime anime, boolean ignoreSynopsis, String username) {
        dbMan.saveAnime(anime, ignoreSynopsis, username);
    }

    public void saveMangaToDatabase(Manga manga, boolean ignoreSynopsis, String username) {
        dbMan.saveManga(manga, ignoreSynopsis, username);
    }

    public boolean deleteAnimeFromAnimelist(Anime anime, String username) {
        return dbMan.deleteAnimeFromAnimelist(anime.getId(), username);
    }

    public boolean deleteMangaFromMangalist(Manga manga, String username) {
        return dbMan.deleteMangaFromMangalist(manga.getId(), username);
    }

    public boolean writeAnimeDetailsToMAL(Anime anime) {
        boolean result;
        if (anime.getDeleteFlag()) {
            if (AccountService.isMAL())
                result = malApi.deleteAnimeFromList(anime.getId());
            else
                result = alApi.deleteAnimeFromList(anime.getId());
        } else {
            if (AccountService.isMAL())
                result = malApi.addOrUpdateAnime(anime);
            else
                result = alApi.addOrUpdateAnime(anime);
        }

        return result;
    }

    public boolean writeMangaDetailsToMAL(Manga manga) {
        boolean result;
        if (manga.getDeleteFlag()) {
            if (AccountService.isMAL())
                result = malApi.deleteMangaFromList(manga.getId());
            else
                result = alApi.deleteMangaFromList(manga.getId());
        } else {
            if (AccountService.isMAL())
                result = malApi.addOrUpdateManga(manga);
            else
                result = alApi.addOrUpdateManga(manga);
        }
        return result;
    }

    public boolean cleanDirtyAnimeRecords(String username) {
        boolean totalSuccess = true;

        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList(username);

        if (dirtyAnimes != null) {
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyAnimeRecords(): Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

            for (Anime anime : dirtyAnimes) {
                totalSuccess = writeAnimeDetailsToMAL(anime);
                if (totalSuccess) {
                    anime.clearDirty();
                    saveAnimeToDatabase(anime, false, username);
                }

                if (!totalSuccess)
                    break;
            }
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyAnimeRecords(): Cleaned dirty anime records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords(String username) {
        boolean totalSuccess = true;

        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList(username);

        if (dirtyMangas != null) {
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyMangaRecords(): Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

            for (Manga manga : dirtyMangas) {
                totalSuccess = writeMangaDetailsToMAL(manga);
                if (totalSuccess) {
                    manga.clearDirty();
                    saveMangaToDatabase(manga, false, username);
                }

                if (!totalSuccess)
                    break;
            }
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyMangaRecords(): Cleaned dirty manga records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public ArrayList<Anime> getMostPopularAnime(int page) {
        if (AccountService.isMAL())
            return malApi.getMostPopularAnime(page);
        else
            return alApi.getAiringAnime(page);
    }

    public ArrayList<Manga> getMostPopularManga(int page) {
        if (AccountService.isMAL())
            return malApi.getMostPopularManga(page);
        else
            return alApi.getAiringManga(page);
    }

    public ArrayList<Anime> getTopRatedAnime(int page) {
        if (AccountService.isMAL())
            return malApi.getTopRatedAnime(page);
        else
            return alApi.getYearAnime(Calendar.getInstance().get(Calendar.YEAR), page);
    }

    public ArrayList<Manga> getTopRatedManga(int page) {
        if (AccountService.isMAL())
            return malApi.getTopRatedManga(page);
        else
            return alApi.getYearManga(Calendar.getInstance().get(Calendar.YEAR), page);
    }

    public ArrayList<Anime> getJustAddedAnime(int page) {
        if (AccountService.isMAL())
            return malApi.getJustAddedAnime(page);
        else
            return alApi.getJustAddedAnime(page);
    }

    public ArrayList<Manga> getJustAddedManga(int page) {
        if (AccountService.isMAL())
            return malApi.getJustAddedManga(page);
        else
            return alApi.getJustAddedManga(page);
    }

    public ArrayList<Anime> getUpcomingAnime(int page) {
        if (AccountService.isMAL())
            return malApi.getUpcomingAnime(page);
        else
            return alApi.getUpcomingAnime(page);
    }

    public ArrayList<Manga> getUpcomingManga(int page) {
        if (AccountService.isMAL())
            return malApi.getUpcomingManga(page);
        else
            return alApi.getUpcomingManga(page);
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        if (AccountService.isMAL())
            return malApi.searchAnime(query, page);
        else
            return alApi.searchAnime(query, page);
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        if (AccountService.isMAL())
            return malApi.searchManga(query, page);
        else
            return alApi.searchManga(query, page);
    }

    public ArrayList<Activity> getActivityFromDB(String username) {
        return dbMan.getActivity(username);
    }

    public ArrayList<Activity> downloadAndStoreActivity(String username) {
        ArrayList<Activity> result = alApi.getActivity(username);
        if (result != null && result.size() > 0) {
            dbMan.saveActivity(result, username);
        }
        return result;
    }
}
