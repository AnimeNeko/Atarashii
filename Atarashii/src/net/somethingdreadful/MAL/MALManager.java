package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.Log;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.sql.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import retrofit.RetrofitError;

public class MALManager {
    MALApi malApi;
    DatabaseManager dbMan;

    public MALManager(Context context) {
        malApi = new MALApi(context);
        dbMan = new DatabaseManager(context);
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
            default:
                if (type.equals(MALApi.ListType.ANIME))
                    r = Anime.STATUS_WATCHING;
                else
                    r = Manga.STATUS_READING;
                break;
        }

        return r;
    }

    public MALApi getAPIObject() {
        return malApi;
    }

    public Anime getAnimeRecordFromMAL(int id) {
        try {
            return malApi.getAnime(id);
        } catch (RetrofitError e) {
            Log.e("MALX", "error downloading anime details: " + e.getMessage());
        }
        return null;
    }

    public Manga getMangaRecordFromMAL(int id) {
        try {
            return malApi.getManga(id);
        } catch (RetrofitError e) {
            Log.e("MALX", "error downloading manga details: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Anime> downloadAndStoreAnimeList() {
        ArrayList<Anime> result = null;
        AnimeList animeList = malApi.getAnimeList();
        if (animeList != null) {
            result = animeList.getAnimes();
            dbMan.saveAnimeList(result);
        }
        return result;
    }

    public ArrayList<Manga> downloadAndStoreMangaList() {
        ArrayList<Manga> result = null;
        MangaList mangaList = malApi.getMangaList();
        if (mangaList != null) {
            result = mangaList.getManga();
            dbMan.saveMangaList(result);
        }
        return result;
    }

    public ArrayList<Anime> getAnimeListFromDB(String ListType) {
        return dbMan.getAnimeList(ListType);
    }

    public ArrayList<Manga> getMangaListFromDB(String ListType) {
        return dbMan.getMangaList(ListType);
    }

    public Anime updateWithDetails(int id, Anime anime) {
        Anime anime_api = malApi.getAnime(id);
        if (anime_api != null) {
            anime.setSynopsis(anime_api.getSynopsis());
            anime.setMembersScore(anime_api.getMembersScore());
            // only store anime with user status in database
            if (anime.getWatchedStatus() != null)
                dbMan.saveAnime(anime, false);
        }

        return anime;
    }

    public ArrayList<User> downloadAndStoreFriendList(String user) {
        ArrayList<User> result = null;
        try {
            result = malApi.getFriends(user);
            if (result.size() > 0) {
                dbMan.saveFriendList(result);
                Collections.sort(result, new FriendlistComparator());
            }
        } catch (Exception e) {
            Log.e("MALX", "error downloading friendlist: " + e.getMessage());
        }
        return result;
    }

    public ArrayList<User> getFriendListFromDB() {
        return dbMan.getFriendList();
    }

    public User downloadAndStoreProfile(String name) {
        User result = null;
        try {
            Profile profile = malApi.getProfile(name);
            if (profile != null) {
                result = new User();
                result.setName(name);
                result.setProfile(profile);
                dbMan.saveUser(result);
            }
        } catch (Exception e) {
            Log.e("MALX", e.getMessage());
            result = null;
        }
        return result;
    }

    public User getProfileFromDB(String name) {
        return dbMan.getProfile(name);
    }

    public Manga updateWithDetails(int id, Manga manga) {
        Manga manga_api = malApi.getManga(id);
        if (manga_api != null) {
            manga.setSynopsis(manga_api.getSynopsis());
            manga.setMembersScore(manga_api.getMembersScore());
            // only store manga with user status in database
            if (manga.getReadStatus() != null)
                dbMan.saveManga(manga, false);
        }

        return manga;
    }

    public Anime getAnimeRecord(int recordID) {
        Anime result = dbMan.getAnime(recordID);
        if (result == null)
            result = getAnimeRecordFromMAL(recordID);
        return result;
    }

    public Manga getMangaRecord(int recordID) {
        Manga result = dbMan.getManga(recordID);
        if (result == null)
            result = getMangaRecordFromMAL(recordID);
        return result;
    }

    public void saveAnimeToDatabase(Anime anime, boolean ignoreSynopsis) {
        dbMan.saveAnime(anime, ignoreSynopsis);
    }

    public void saveMangaToDatabase(Manga manga, boolean ignoreSynopsis) {
        dbMan.saveManga(manga, ignoreSynopsis);
    }

    public boolean deleteAnimeFromDatabase(Anime anime) {
        return dbMan.deleteAnime(anime.getId());
    }

    public boolean deleteMangaFromDatabase(Manga manga) {
        return dbMan.deleteManga(manga.getId());
    }

    public boolean writeAnimeDetailsToMAL(Anime anime) {
        boolean result;
        if (anime.getDeleteFlag())
            result = malApi.deleteAnimeFromList(anime.getId());
        else
            result = malApi.addOrUpdateAnime(anime);
        return result;
    }

    public boolean writeMangaDetailsToMAL(Manga manga) {
        boolean result;
        if (manga.getDeleteFlag())
            result = malApi.deleteMangaFromList(manga.getId());
        else
            result = malApi.addOrUpdateManga(manga);
        return result;
    }

    public boolean cleanDirtyAnimeRecords() {
        boolean totalSuccess = true;

        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList();

        if (dirtyAnimes != null) {
            Log.v("MALX", "Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

            for (Anime anime : dirtyAnimes) {
                totalSuccess = writeAnimeDetailsToMAL(anime);
                if (totalSuccess) {
                    anime.setDirty(false);
                    saveAnimeToDatabase(anime, false);
                }

                if (!totalSuccess)
                    break;
            }
            Log.v("MALX", "Cleaned dirty anime records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords() {
        boolean totalSuccess = true;

        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList();

        if (dirtyMangas != null) {
            Log.v("MALX", "Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

            for (Manga manga : dirtyMangas) {
                totalSuccess = writeMangaDetailsToMAL(manga);
                if (totalSuccess) {
                    manga.setDirty(false);
                    saveMangaToDatabase(manga, false);
                }

                if (!totalSuccess)
                    break;
            }
            Log.v("MALX", "Cleaned dirty manga records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    private class FriendlistComparator implements Comparator<User> {
        @Override
        public int compare(User u1, User u2) {
            return u1.getName().compareTo(u2.getName());
        }
    }
}
