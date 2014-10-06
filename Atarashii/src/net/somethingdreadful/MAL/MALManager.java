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

    public Anime getAnimeRecord(int id) {
        try {
            return malApi.getAnime(id);
        } catch (RetrofitError e) {
            Log.e("MALX", "error downloading anime details: " + e.getMessage());
        }
        return null;
    }

    public Manga getMangaRecord(int id) {
        try {
            return malApi.getManga(id);
        } catch (RetrofitError e) {
            Log.e("MALX", "error downloading manga details: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Anime> downloadAndStoreAnimeList(String username) {
        ArrayList<Anime> result = null;
        AnimeList animeList = malApi.getAnimeList();
        if (animeList != null) {
            result = animeList.getAnimes();
            dbMan.saveAnimeList(result, username);
        }
        return result;
    }

    public ArrayList<Manga> downloadAndStoreMangaList(String username) {
        ArrayList<Manga> result = null;
        MangaList mangaList = malApi.getMangaList();
        if (mangaList != null) {
            result = mangaList.getManga();
            dbMan.saveMangaList(result, username);
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
        Anime anime_api = malApi.getAnime(id);
        if (anime_api != null) {
            anime.setSynopsis(anime_api.getSynopsis());
            anime.setMembersScore(anime_api.getMembersScore());
            // only store anime with user status in database
            if (anime.getWatchedStatus() != null)
                dbMan.saveAnime(anime, false, username);
        }

        return anime;
    }

    public ArrayList<User> downloadAndStoreFriendList(String user) {
        ArrayList<User> result;
        try {
            Log.d("MALX", "downloading friendlist of " + user);
            result = malApi.getFriends(user);
            if (result.size() > 0) {
                dbMan.saveFriendList(result, user);
            }
        } catch (Exception e) {
            Log.e("MALX", "error downloading friendlist: " + e.getMessage());
        }
        return dbMan.getFriendList(user);
    }

    public ArrayList<User> getFriendListFromDB(String username) {
        return dbMan.getFriendList(username);
    }

    public User downloadAndStoreProfile(String name) {
        User result = null;
        try {
            Log.d("MALX", "downloading profile of " + name);
            Profile profile = malApi.getProfile(name);
            if (profile != null) {
                result = new User();
                result.setName(name);
                result.setProfile(profile);
                dbMan.saveUser(result, true);
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

    public Manga updateWithDetails(int id, Manga manga, String username) {
        Manga manga_api = malApi.getManga(id);
        if (manga_api != null) {
            manga.setSynopsis(manga_api.getSynopsis());
            manga.setMembersScore(manga_api.getMembersScore());
            // only store manga with user status in database
            if (manga.getReadStatus() != null)
                dbMan.saveManga(manga, false, username);
        }

        return manga;
    }

    public void saveAnimeToDatabase(Anime anime, boolean ignoreSynopsis, String username) {
        dbMan.saveAnime(anime, ignoreSynopsis, username);
    }

    public void saveMangaToDatabase(Manga manga, boolean ignoreSynopsis, String username) {
        dbMan.saveManga(manga, ignoreSynopsis, username);
    }

    public boolean deleteAnimeFromDatabase(Anime anime) {
        return dbMan.deleteAnime(anime.getId());
    }

    public boolean deleteAnimeFromAnimelist(Anime anime, String username) {
        return dbMan.deleteAnimeFromAnimelist(anime.getId(), username);
    }

    public boolean deleteMangaFromDatabase(Manga manga) {
        return dbMan.deleteManga(manga.getId());
    }

    public boolean deleteMangaFromMangalist(Manga manga, String username) {
        return dbMan.deleteMangaFromMangalist(manga.getId(), username);
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

    public boolean cleanDirtyAnimeRecords(String username) {
        boolean totalSuccess = true;

        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList(username);

        if (dirtyAnimes != null) {
            Log.v("MALX", "Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

            for (Anime anime : dirtyAnimes) {
                totalSuccess = writeAnimeDetailsToMAL(anime);
                if (totalSuccess) {
                    anime.setDirty(false);
                    saveAnimeToDatabase(anime, false, username);
                }

                if (!totalSuccess)
                    break;
            }
            Log.v("MALX", "Cleaned dirty anime records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords(String username) {
        boolean totalSuccess = true;

        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList(username);

        if (dirtyMangas != null) {
            Log.v("MALX", "Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

            for (Manga manga : dirtyMangas) {
                totalSuccess = writeMangaDetailsToMAL(manga);
                if (totalSuccess) {
                    manga.setDirty(false);
                    saveMangaToDatabase(manga, false, username);
                }

                if (!totalSuccess)
                    break;
            }
            Log.v("MALX", "Cleaned dirty manga records, status: " + totalSuccess);
        }
        return totalSuccess;
    }
}
