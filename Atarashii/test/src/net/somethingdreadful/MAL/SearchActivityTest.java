package net.somethingdreadful.MAL;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import com.jayway.android.robotium.solo.Solo;

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
public class SearchActivityTest extends ActivityInstrumentationTestCase2<SearchActivity> {
    private Solo solo;

    public SearchActivityTest() {
        super(SearchActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testOnCreate() {
        assertNotNull(getActivity());
        assertTrue(solo.searchText("ANIME"));
        assertTrue(solo.searchText("MANGA"));
        assertTrue(solo.searchText("Search in MAL"));
    }

    public void testDoSearchAnime() {
        solo.clickOnEditText(0);
        solo.enterText(0, "Minami-ke");
        solo.sendKey(KeyEvent.KEYCODE_ENTER);
        assertTrue(solo.waitForText("Minami-ke Tadaima"));
    }

    public void testDoBeginSearchAnimeAndGoToDetailView() {
        this.testDoSearchAnime();
        solo.clickOnText("Minami-ke Tadaima");
        assertTrue(solo.waitForText("Minami-ke Tadaima"));
    }

    public void testDoSearchManga() {
        solo.clickOnText("MANGA");
        solo.clickOnEditText(0);
        solo.enterText(0, "Otaku no musume-san");
        solo.sendKey(KeyEvent.KEYCODE_ENTER);
        assertTrue(solo.waitForText("Otaku no musume-san"));
    }

    public void testDoBeginSearchMangaAndGoToDetailView() {
        this.testDoSearchManga();
        solo.clickOnText("Otaku no musume-san");
        assertTrue(solo.waitForText("Otaku no musume-san"));
    }

}
