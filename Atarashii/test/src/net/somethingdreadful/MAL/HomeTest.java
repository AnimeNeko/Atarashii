package net.somethingdreadful.MAL;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class net.somethingdreadful.MAL.HomeTest \
 * net.somethingdreadful.MAL.tests/android.test.InstrumentationTestRunner
 */
public class HomeTest extends ActivityInstrumentationTestCase2<Home> {

    public HomeTest() {
        super(Home.class);
    }

    public void testGeneric() {
        Home activity = getActivity();
        assertNotNull(activity);
    }

    public void testFailed() {
        assertEquals(1, 0);
    }

/*    public void _testExcludeDirtyFromList() {
        MALSqlHelper helper = new MALSqlHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] col = {"recordID"};
        Cursor cursor = db.query("anime", col, "status <> 'null'", null, null, null, "recordName ASC");
        assertTrue(cursor.getCount() > 1);
    }*/
}
