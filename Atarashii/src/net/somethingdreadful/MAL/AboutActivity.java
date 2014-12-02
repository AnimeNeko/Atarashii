package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_about);

        ((Card) findViewById(R.id.atarashii_card)).setContent(R.layout.card_about_atarashii);
        ((Card) findViewById(R.id.contributors_card)).setContent(R.layout.card_about_contributors);
        ((Card) findViewById(R.id.community_card)).setContent(R.layout.card_about_community);
        ((Card) findViewById(R.id.translations_card)).setContent(R.layout.card_about_translations);
        ((Card) findViewById(R.id.acknowledgements_card)).setContent(R.layout.card_about_acknowledgements);
        createLinks((TextView) findViewById(R.id.contributor_anima_name));
        createLinks((TextView) findViewById(R.id.contributor_motokochan_name));
        createLinks((TextView) findViewById(R.id.contributor_apkawa_name));
        createLinks((TextView) findViewById(R.id.contributor_dsko_name));
        createLinks((TextView) findViewById(R.id.contributor_ratan12_name));
        createLinks((TextView) findViewById(R.id.acknowledgements_card_content));
        createLinks((TextView) findViewById(R.id.community_card_content));
        createLinks((TextView) findViewById(R.id.translations_card_content));
        createLinks((TextView) findViewById(R.id.notlisted_content));

        NfcHelper.disableBeam(this);
    }

    public void createLinks(TextView textView) {
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
