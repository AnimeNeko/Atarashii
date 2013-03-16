package net.somethingdreadful.MAL;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
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
    }

    public void testDoSearch() {
        solo.enterText((EditText) getActivity().findViewById(R.id.searchQuery), "Minami-ke");
        solo.clickOnText("Go");
        assertTrue(solo.searchText("Minami-ke Tadaima"));
    }

    public void testDoSearchAndGoToDetailView() {
        this.testDoSearch();
        solo.clickOnText("Minami-ke");
        assertTrue(solo.waitForText("Minami-ke"));
    }

}
