package net.somethingdreadful.MAL.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;

import org.apache.commons.lang3.text.WordUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileDetailsMAL extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    View view;
    Card imagecard;
    Card animecard;
    Card mangacard;
    private ProfileActivity activity;

    @Bind(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;
    @Bind(R.id.progressBar) ProgressBar progressBar;
    @Bind(R.id.network_Card) Card networkCard;

    @Bind(R.id.birthdaysmall) TextView tv1;
    @Bind(R.id.locationsmall) TextView tv2;
    @Bind(R.id.commentspostssmall) TextView tv3;
    @Bind(R.id.forumpostssmall) TextView tv4;
    @Bind(R.id.lastonlinesmall) TextView tv5;
    @Bind(R.id.gendersmall) TextView tv6;
    @Bind(R.id.joindatesmall) TextView tv7;
    @Bind(R.id.accessranksmall) TextView tv8;
    @Bind(R.id.animelistviewssmall) TextView tv9;
    @Bind(R.id.mangalistviewssmall) TextView tv10;
    @Bind(R.id.atimedayssmall) TextView tv11;
    @Bind(R.id.awatchingsmall) TextView tv12;
    @Bind(R.id.acompletedpostssmall) TextView tv13;
    @Bind(R.id.aonholdsmall) TextView tv14;
    @Bind(R.id.adroppedsmall) TextView tv15;
    @Bind(R.id.aplantowatchsmall) TextView tv16;
    @Bind(R.id.atotalentriessmall) TextView tv17;
    @Bind(R.id.mtimedayssmall) TextView tv18;
    @Bind(R.id.mwatchingsmall) TextView tv19;
    @Bind(R.id.mcompletedpostssmall) TextView tv20;
    @Bind(R.id.monholdsmall) TextView tv21;
    @Bind(R.id.mdroppedsmall) TextView tv22;
    @Bind(R.id.mplantowatchsmall) TextView tv23;
    @Bind(R.id.mtotalentriessmall) TextView tv24;
    @Bind(R.id.websitesmall) TextView tv25;
    @Bind(R.id.websitefront) TextView tv26;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_profile_mal, container, false);

        imagecard = ((Card) view.findViewById(R.id.name_card));
        animecard = (Card) view.findViewById(R.id.Anime_card);
        mangacard = (Card) view.findViewById(R.id.Manga_card);

        imagecard.setContent(R.layout.card_image);
        ((Card) view.findViewById(R.id.details_card)).setContent(R.layout.card_profile_details);
        animecard.setContent(R.layout.card_profile_anime);
        mangacard.setContent(R.layout.card_profile_manga);

        ButterKnife.bind(this, view);

        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        TextView tv25 = (TextView) view.findViewById(R.id.websitesmall);
        tv25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webstiteclick = Uri.parse(activity.record.getDetails().getWebsite());
                startActivity(new Intent(Intent.ACTION_VIEW, webstiteclick));
            }
        });

        activity.setDetails(this);

        if (activity.record == null)
            toggle(1);

        NfcHelper.disableBeam(activity);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
    }

    public void card() { //settings for hide a card and text userprofile
        if (PrefManager.getHideAnime())
            animecard.setVisibility(View.GONE);
        if (PrefManager.getHideManga())
            mangacard.setVisibility(View.GONE);
        if (activity.record.getMangaStats() == null || activity.record.getMangaStats().getTotalEntries() < 1)  //if manga (total entry) is beneath the int then hide
            mangacard.setVisibility(View.GONE);
        if (activity.record.getAnimeStats() == null || activity.record.getAnimeStats().getTotalEntries() < 1)  //if anime (total entry) is beneath the int then hide
            animecard.setVisibility(View.GONE);

        Card namecard = (Card) view.findViewById(R.id.name_card);
        namecard.Header.setText(WordUtils.capitalize(activity.record.getUsername()));
    }

    public void setcolor() {
        TextView tv8 = (TextView) view.findViewById(R.id.accessranksmall);
        String name = activity.record.getUsername();
        String rank = activity.record.getDetails().getAccessRank() != null ? activity.record.getDetails().getAccessRank() : "";
        if (!PrefManager.getTextColor()) {
            setColor(true);
            setColor(false);
            if (rank.contains("Administrator")) {
                tv8.setTextColor(Color.parseColor("#850000"));
            } else if (rank.contains("Moderator")) {
                tv8.setTextColor(Color.parseColor("#003385"));
            } else {
                tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
            }
            TextView tv11 = (TextView) view.findViewById(R.id.websitesmall);
            tv11.setTextColor(Color.parseColor("#002EAB"));
        }
        if (Profile.isDeveloper(name)) {
            tv8.setText(R.string.access_rank_atarashii_developer); //Developer
            tv8.setTextColor(getResources().getColor(R.color.primary)); //Developer
        }
    }

    public void setColor(boolean type) {
        int Hue;
        TextView textview;
        if (type) {
            textview = (TextView) view.findViewById(R.id.atimedayssmall); //anime
            Hue = (int) (activity.record.getAnimeStats().getTimeDays() * 2.5);
        } else {
            textview = (TextView) view.findViewById(R.id.mtimedayssmall); // manga
            Hue = (int) (activity.record.getMangaStats().getTimeDays() * 5);
        }
        if (Hue > 359)
            Hue = 359;
        textview.setTextColor(Color.HSVToColor(new float[]{Hue, 1, (float) 0.7}));
    }

    private String getStringFromResourceArray(int resArrayId, int notFoundStringId, int index) {
        try { // getResources will cause a crash if an users clicks the profile fast away
            Resources res = getResources();
            try {
                String[] types = res.getStringArray(resArrayId);
                if (index < 0 || index >= types.length) // make sure to have a valid array index
                    return res.getString(notFoundStringId);
                else
                    return types[index];
            } catch (Exception e) {
                return res.getString(notFoundStringId);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "ProfileDetailsMAL.getStringFromResourceArray(): " + e.getMessage());
            return "Error: could not receive resources";
        }
    }

    public void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void setText() {
        if (activity.record.getDetails().getBirthday() == null) {
            tv1.setText(R.string.not_specified);
        } else {
            String birthday = DateTools.parseDate(activity.record.getDetails().getBirthday(), false);
            tv1.setText(birthday.equals("") ? activity.record.getDetails().getBirthday() : birthday);
        }
        if (activity.record.getDetails().getLocation() == null)
            tv2.setText(R.string.not_specified);
        else
            tv2.setText(activity.record.getDetails().getLocation());
        if (activity.record.getDetails().getWebsite() != null && activity.record.getDetails().getWebsite().contains("http://") && activity.record.getDetails().getWebsite().contains(".")) { // filter fake websites
            tv25.setText(activity.record.getDetails().getWebsite().replace("http://", ""));
        } else {
            tv25.setVisibility(View.GONE);
            tv26.setVisibility(View.GONE);
        }
        tv3.setText(String.valueOf(activity.record.getDetails().getComments()));
        tv4.setText(String.valueOf(activity.record.getDetails().getForumPosts()));
        if (activity.record.getDetails().getLastOnline() != null) {
            String lastOnline = DateTools.parseDate(activity.record.getDetails().getLastOnline(), true);
            tv5.setText(lastOnline.equals("") ? activity.record.getDetails().getLastOnline() : lastOnline);
        } else
            tv5.setText("-");
        tv6.setText(getStringFromResourceArray(R.array.gender, R.string.not_specified, activity.record.getDetails().getGenderInt()));
        if (activity.record.getDetails().getJoinDate() != null) {
            String joinDate = DateTools.parseDate(activity.record.getDetails().getJoinDate(), false);
            tv7.setText(joinDate.equals("") ? activity.record.getDetails().getJoinDate() : joinDate);
        } else
            tv7.setText("-");
        tv8.setText(activity.record.getDetails().getAccessRank());
        tv9.setText(String.valueOf(activity.record.getDetails().getAnimeListViews()));
        tv10.setText(String.valueOf(activity.record.getDetails().getMangaListViews()));

        tv11.setText(activity.record.getAnimeStats().getTimeDays().toString());
        tv12.setText(String.valueOf(activity.record.getAnimeStats().getWatching()));
        tv13.setText(String.valueOf(activity.record.getAnimeStats().getCompleted()));
        tv14.setText(String.valueOf(activity.record.getAnimeStats().getOnHold()));
        tv15.setText(String.valueOf(activity.record.getAnimeStats().getDropped()));
        tv16.setText(String.valueOf(activity.record.getAnimeStats().getPlanToWatch()));
        tv17.setText(String.valueOf(activity.record.getAnimeStats().getTotalEntries()));

        tv18.setText(activity.record.getMangaStats().getTimeDays().toString());
        tv19.setText(String.valueOf(activity.record.getMangaStats().getReading()));
        tv20.setText(String.valueOf(activity.record.getMangaStats().getCompleted()));
        tv21.setText(String.valueOf(activity.record.getMangaStats().getOnHold()));
        tv22.setText(String.valueOf(activity.record.getMangaStats().getDropped()));
        tv23.setText(String.valueOf(activity.record.getMangaStats().getPlanToRead()));
        tv24.setText(String.valueOf(activity.record.getMangaStats().getTotalEntries()));
    }

    public void refresh() {
        try {
            if (activity.record == null) {
                if (MALApi.isNetworkAvailable(activity)) {
                    Theme.Snackbar(activity, R.string.toast_error_UserRecord);
                } else {
                    toggle(2);
                }
            } else {
                card();
                setText();
                setcolor();

                Picasso.with(activity)
                        .load(activity.record.getImageUrl())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                imagecard.wrapImage(bitmap.getWidth(), bitmap.getHeight());
                                ((ImageView) view.findViewById(R.id.Image)).setImageBitmap(bitmap);
                                toggle(0);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                toggle(0);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                toggle(0);
                            }
                        });
                toggle(0);
            }
        } catch (IllegalStateException e) {
            Crashlytics.log(Log.ERROR, "MALX", "ProfileDetailsMAL.refresh(): has been closed too fast");
        }
    }

    @Override
    public void onRefresh() {
        activity.getRecords();
    }
}
