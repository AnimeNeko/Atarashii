package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class Logout extends SherlockActivity {

    static PrefManager prefManager;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        prefManager = new PrefManager(context);

        prefManager.setInit(false);
        prefManager.setUser("");
        prefManager.setPass("");
        prefManager.commitChanges();

        context.deleteDatabase("MAL.db");

        startActivity(new Intent(this, Home.class));
    }
}
