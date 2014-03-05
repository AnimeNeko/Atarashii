package net.somethingdreadful.MAL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent receivedIntent) {
		Toast.makeText(context, "Created by UpdateReceiver!",
				Toast.LENGTH_SHORT).show();

	}

}
