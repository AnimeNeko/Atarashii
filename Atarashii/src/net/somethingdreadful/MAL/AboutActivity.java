package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.activity_about, true);
        Theme.setActionBar(this);
        setTitle(R.string.title_activity_about);

        Card.fastInit(this, R.id.atarashii_card, R.layout.card_about_atarashii);
        Card.fastInit(this, R.id.contributors_card, R.layout.card_about_contributors);
        Card.fastInit(this, R.id.community_card, R.layout.card_about_community);
        Card.fastInit(this, R.id.translations_card, R.layout.card_about_translations);
        Card.fastInit(this, R.id.acknowledgements_card, R.layout.card_about_acknowledgements);
        createLinks((TextView) findViewById(R.id.contributor_anima_name));
        createLinks((TextView) findViewById(R.id.contributor_motokochan_name));
        createLinks((TextView) findViewById(R.id.contributor_apkawa_name));
        createLinks((TextView) findViewById(R.id.contributor_dsko_name));
        createLinks((TextView) findViewById(R.id.contributor_ratan12_name));
        createLinks((TextView) findViewById(R.id.acknowledgements_card_content));
        createLinks((TextView) findViewById(R.id.community_card_content));
        createLinks((TextView) findViewById(R.id.translations_card_content));
        createLinks((TextView) findViewById(R.id.notlisted_content));

        Card atarashii = (Card) findViewById(R.id.atarashii_card);

        try {
            atarashii.Header.setText(getString(R.string.app_name) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        NfcHelper.disableBeam(this);
    }

    private void createLinks(TextView textView) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }
}
