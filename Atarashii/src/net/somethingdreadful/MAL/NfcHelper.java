package net.somethingdreadful.MAL;

import android.annotation.TargetApi;
import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Build;

public class NfcHelper {

	// disables "push to beam" for an activity
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void disableBeam(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
			if (adapter != null) {
				adapter.setNdefPushMessage(null, activity);
			}
		}
	}
}
