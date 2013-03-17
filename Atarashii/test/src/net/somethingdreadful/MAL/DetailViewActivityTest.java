package net.somethingdreadful.MAL;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
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
public class DetailViewActivityTest extends ActivityInstrumentationTestCase2<DetailView> {
    private Solo solo;

    public DetailViewActivityTest() {
        super(DetailView.class);
    }

    @Override
    public DetailView getActivity() {
        return getActivity(1887, "anime");
    }

    public DetailView getActivity(int recordID, String recordType) {
        Intent intent = new Intent();
        intent.putExtra("net.somethingdreadful.MAL.recordID", recordID);
        intent.putExtra("net.somethingdreadful.MAL.recordType", recordType);
        setActivityIntent(intent);
        return super.getActivity();
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void setUp(DetailView detailView) throws Exception {
        solo = new Solo(getInstrumentation(), detailView);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testOnCreate() {
        assertNotNull(getActivity());
        assertTrue(solo.waitForText("Lucky☆Star"));
    }
}
