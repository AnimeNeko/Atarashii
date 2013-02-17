package net.somethingdreadful.MAL.api;

import android.test.AndroidTestCase;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 17.02.13
 * Time: 17:58
 * To change this template use File | Settings | File Templates.
 */
public class MALApiTest extends AndroidTestCase{
    public void testIsAuth() throws Exception {

        // TODO get testing user
        MALApi api = new MALApi("todo_username", "todo_password");
        assertTrue(api.isAuth());
    }

    public void testIsAuthFailure() throws Exception {
        MALApi api = new MALApi("test", "test");
        assertFalse(api.isAuth());
    }

    public void testSearchAnime() throws Exception {
        MALApi api = new MALApi(null, null);
        JSONArray result = api.search(MALApiListType.ANIME, "Minami-ke Tadaima");
        assertNotNull(result);
        JSONObject genre = (JSONObject) result.get(0);
        assertEquals(14511, genre.getInt("id"));
    }

    public void testSearchManga() throws Exception {
        MALApi api = new MALApi(null, null);
        JSONArray result = api.search(MALApiListType.MANGA, "berserk");
        assertNotNull(result);
        JSONObject genre = (JSONObject) result.get(0);
        assertEquals(2, genre.getInt("id"));
    }

}
