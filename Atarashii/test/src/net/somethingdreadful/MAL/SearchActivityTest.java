package net.somethingdreadful.MAL;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

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

    public SearchActivityTest() {
        super(SearchActivity.class);
    }

    public void testOnCreate() {
        assertNotNull(getActivity());
    }

    public void testSearchResults() {
        ListView result = (ListView) getActivity().findViewById(R.id.searchResult);
        assertNotNull(result);
        assertEquals((String) result.getAdapter().getItem(0), "test");
    }

}
