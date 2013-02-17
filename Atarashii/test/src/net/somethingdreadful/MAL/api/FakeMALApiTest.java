package net.somethingdreadful.MAL.api;

import org.json.JSONException;
import org.junit.Test;
import org.junit.Assert;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 17.02.13
 * Time: 0:40
 * To change this template use File | Settings | File Templates.
 */
public class FakeMALApiTest {

    @Test
    public void testNewObject() {
        Assert.assertNotNull(new FakeMALApi("test", "test"));
    }

    @Test
    public void testBaseObject() {
        BaseMALApi api = new FakeMALApi("test", "test");
        Assert.assertTrue(api.isAuth());
        FakeMALApi f_api = (FakeMALApi) api;
    }

    @Test
    public void testJSONObjectExample() throws JSONException {
        String json_string = "[{\"id\":7311,\"title\":\"Suzumiya Haruhi no Shoushitsu\",\"other_titles\":{},\"synopsis\":\"It is mid-December, and SOS Brigade chief Haruhi Suzumiya announces that the Brigade is going to hold a Christmas party in their clubroom, with Japanese hotpot for dinner. The brigade members Kyon, Yu...\",\"type\":\"Movie\",\"rank\":null,\"popularity_rank\":null,\"image_url\":\"http://cdn.myanimelist.net/images/anime/9/24646t.jpg\",\"episodes\":1,\"status\":null,\"start_date\":\"2010-02-06\",\"end_date\":\"2010-02-06\",\"genres\":[],\"tags\":[],\"classification\":\"PG-13\",\"members_score\":9.01,\"members_count\":null,\"favorited_count\":null,\"manga_adaptations\":[],\"prequels\":[],\"sequels\":[],\"side_stories\":[],\"parent_story\":null,\"character_anime\":[],\"spin_offs\":[],\"summaries\":[],\"alternative_versions\":[],\"listed_anime_id\":null,\"watched_episodes\":null,\"score\":null,\"watched_status\":null}]";
        json_string = new String("1");
        System.out.print(json_string);
        JSONObject jsonObject = new JSONObject();
        Assert.assertNotNull(jsonObject);


    }
}
