package net.somethingdreadful.MAL;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_settings);
        Theme.setTheme(this, 0, false);
        Theme.setActionBar(this);
        setTitle(R.string.title_activity_settings);

        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent firstRunInit = new Intent(this, BackupActivity.class);
            startActivity(firstRunInit);
        } else {
            Theme.Snackbar(this, R.string.toast_permission_backup);
        }
    }
}