package net.somethingdreadful.MAL.api;

import org.junit.Test;
import org.junit.Assert;

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
}
