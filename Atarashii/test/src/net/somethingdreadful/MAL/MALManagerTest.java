package net.somethingdreadful.MAL;

import android.test.AndroidTestCase;
/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 15.02.13
 * Time: 23:00
 * To change this template use File | Settings | File Templates.
 */
public class MALManagerTest extends AndroidTestCase {
    // TODO adding testing user and/or mock http
    private String username = "";
    private String password = "";

    static MALManager manager;

    public MALManagerTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = new MALManager(getContext());
    }

    public void testLoginSuccess() {
        assertTrue(MALManager.verifyAccount(username, password));
    }

    public void testFalireLogin() {
        assertFalse(MALManager.verifyAccount("example", "example"));
    }

    public void testWriteDetailsToMal() {
        // TODO:
        //assertTrue(manager.writeDetailsToMAL());
    }



}
