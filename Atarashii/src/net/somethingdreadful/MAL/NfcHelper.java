package net.somethingdreadful.MAL;

import android.app.Activity;
import android.nfc.NfcAdapter;

public class NfcHelper {

    // disables "push to beam" for an activity
    public static void disableBeam(Activity activity) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) {
            adapter.setNdefPushMessage(null, activity);
        }
    }
}
