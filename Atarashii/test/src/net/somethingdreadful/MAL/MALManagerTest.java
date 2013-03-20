package net.somethingdreadful.MAL;

import android.test.AndroidTestCase;

public class MALManagerTest extends AndroidTestCase {
    // TODO adding testing user and/or mock http
    static MALManager manager;

    public MALManagerTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = new MALManager(getContext());
    }


}
