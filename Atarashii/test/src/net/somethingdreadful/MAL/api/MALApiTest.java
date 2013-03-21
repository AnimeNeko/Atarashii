package net.somethingdreadful.MAL.api;

import android.test.AndroidTestCase;
import net.somethingdreadful.MAL.TestSettings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class MALApiTest extends AndroidTestCase {

    MALApi api;

    @Override
    public void setUp() throws Exception {
        api = new MALApi(TestSettings.MAL_USERNAME, TestSettings.MAL_PASSWORD);
    }

    public void testIsAuth() throws Exception {
        assertTrue(api.isAuth());
    }

    public void testIsAuthFailure() throws Exception {
        MALApi api = new MALApi("todo_username", "todo_password");
        assertFalse(api.isAuth());
    }

    public void testSearchAnime() throws Exception {
        JSONArray result = api.search(BaseMALApi.ListType.ANIME, "Minami-ke Tadaima");
        assertNotNull(result);
        JSONObject genre = (JSONObject) result.get(0);
        assertEquals(14511, genre.getInt("id"));
    }

    public void testSearchManga() throws Exception {
        MALApi api = new MALApi(TestSettings.MAL_USERNAME, TestSettings.MAL_PASSWORD);
        JSONArray result = api.search(BaseMALApi.ListType.MANGA, "berserk");
        assertNotNull(result);
        JSONObject genre = (JSONObject) result.get(0);
        assertEquals(2, genre.getInt("id"));
    }

    public void testGetAnimeList() throws Exception {
        JSONArray result = api.getList(BaseMALApi.ListType.ANIME);
        assertNotNull(result);
        assertTrue(isInMALList(BaseMALApi.ListType.ANIME, 3225));
    }

    public void testAddUpdateDeleteAnimeGenre() throws Exception {
        BaseMALApi.ListType listType = BaseMALApi.ListType.ANIME;

        HashMap<String, String> statusMap = new HashMap<String, String>();
        statusMap.put("status", "watched_status");
        statusMap.put("episodes", "watched_episodes");
        statusMap.put("score", "score");

        boolean hasCreate = true;
        HashMap<String, String> listRecord = new HashMap<String, String>();
        Integer genre_id = 1887;

        // DELETE
        api.deleteGenreFromList(listType, genre_id.toString());
        assertFalse(isInMALList(listType, genre_id));

        // CREATE
        listRecord.put("status", "watching");
        listRecord.put("episodes", "1");
        listRecord.put("score", "9");
        api.addOrUpdateGenreInList(hasCreate, listType, genre_id.toString(), listRecord);
        JSONObject genre = getGenreFromMALList(listType, genre_id);
        assertNotNull(genre);
        for (HashMap.Entry<String, String> entry : listRecord.entrySet()) {
            assertEquals(entry.getValue(), genre.getString(statusMap.get(entry.getKey())));
        }
        // UPDATE
        listRecord.put("status", "completed");
        listRecord.put("episodes", "24");
        listRecord.put("score", "9");
        hasCreate = false;
        api.addOrUpdateGenreInList(hasCreate, listType, genre_id.toString(), listRecord);
        genre = getGenreFromMALList(listType, genre_id);
        assertNotNull(genre);
        for (HashMap.Entry<String, String> entry : listRecord.entrySet()) {
            assertEquals(entry.getValue(), genre.getString(statusMap.get(entry.getKey())));
        }
        // DELETE
        api.deleteGenreFromList(listType, genre_id.toString());
        assertFalse(isInMALList(listType, genre_id));
    }

    private JSONArray getMALList(BaseMALApi.ListType listType) {
        return api.getList(listType);
    }

    private JSONObject getGenreFromJsonArray(JSONArray jsonArray, Integer genre_id) throws Exception {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject genre = (JSONObject) jsonArray.get(i);
            if (genre.getInt("id") == genre_id) {
                return genre;
            }
        }
        return null;

    }

    private JSONObject getGenreFromMALList(BaseMALApi.ListType listType, Integer genre_id) throws Exception {
        JSONArray result = getMALList(listType);
        assertNotNull(result);
        JSONObject genre = getGenreFromJsonArray(result, genre_id);
        assertNotNull(genre);
        return genre;
    }

    private boolean isInMALList(BaseMALApi.ListType listType, Integer genre_id) throws Exception {
        JSONArray result = getMALList(listType);
        JSONObject genre = getGenreFromJsonArray(result, genre_id);
        return genre != null;
    }
}
