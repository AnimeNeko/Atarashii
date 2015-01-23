package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;

import org.apache.commons.lang3.text.WordUtils;

public class ProfileDetails extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    Context context;
    View view;
    Card imagecard;
    Card animecard;
    Card mangacard;
    SwipeRefreshLayout swipeRefresh;
    ProgressBar progressBar;
    Card networkCard;

    private ProfileActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        imagecard = ((Card) view.findViewById(R.id.name_card));
        imagecard.setContent(R.layout.card_image);
        ((Card) view.findViewById(R.id.details_card)).setContent(R.layout.card_profile_details);
        animecard = (Card) view.findViewById(R.id.Anime_card);
        animecard.setContent(R.layout.card_profile_anime);
        mangacard = (Card) view.findViewById(R.id.Manga_card);
        mangacard.setContent(R.layout.card_profile_manga);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        networkCard = (Card) view.findViewById(R.id.network_Card);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        TextView tv25 = (TextView) view.findViewById(R.id.websitesmall);
        tv25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webstiteclick = Uri.parse(activity.record.getProfile().getDetails().getWebsite());
                startActivity(new Intent(Intent.ACTION_VIEW, webstiteclick));
            }
        });

        activity.setDetails(this);
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
        if (PrefManager.getHideAnime()) {
            animecard.setVisibility(View.GONE);
        }
        if (PrefManager.getHideManga()) {
            mangacard.setVisibility(View.GONE);
        }
        if (activity.record.getProfile().getMangaStats().getTotalEntries() < 1) { //if manga (total entry) is beneath the int then hide
            mangacard.setVisibility(View.GONE);
        }
        if (activity.record.getProfile().getAnimeStats().getTotalEntries() < 1) { //if anime (total entry) is beneath the int then hide
            animecard.setVisibility(View.GONE);
        }
        Card namecard = (Card) view.findViewById(R.id.name_card);
        namecard.Header.setText(WordUtils.capitalize(activity.record.getName()));
    }

    public void setcolor() {
        TextView tv8 = (TextView) view.findViewById(R.id.accessranksmall);
        String name = activity.record.getName();
        String rank = activity.record.getProfile().getDetails().getAccessRank() != null ? activity.record.getProfile().getDetails().getAccessRank() : "";
        if (!PrefManager.getTextColor()) {
            setColor(true);
            setColor(false);
            if (rank.contains("Administrator")) {
                tv8.setTextColor(Color.parseColor("#850000"));
            } else if (rank.contains("Moderator")) {
                tv8.setTextColor(Color.parseColor("#003385"));
            } else if (User.isDeveloperRecord(name)) {
                tv8.setTextColor(Color.parseColor("#008583")); //Developer
            } else {
                tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
            }
            TextView tv11 = (TextView) view.findViewById(R.id.websitesmall);
            tv11.setTextColor(Color.parseColor("#002EAB"));
        }
        if (User.isDeveloperRecord(name)) {
            tv8.setText(R.string.access_rank_atarashii_developer); //Developer
        }
    }

    public void setColor(boolean type) {
        int Hue;
        TextView textview;
        if (type) {
            textview = (TextView) view.findViewById(R.id.atimedayssmall); //anime
            Hue = (int) (activity.record.getProfile().getAnimeStats().getTimeDays() * 2.5);
        } else {
            textview = (TextView) view.findViewById(R.id.mtimedayssmall); // manga
            Hue = (int) (activity.record.getProfile().getMangaStats().getTimeDays() * 5);
        }
        if (Hue > 359) {
            Hue = 359;
        }
        textview.setTextColor(Color.HSVToColor(new float[]{Hue, 1, (float) 0.7}));
    }

    private String getStringFromResourceArray(int resArrayId, int notFoundStringId, int index) {
        Resources res = getResources();
        try {
            String[] types = res.getStringArray(resArrayId);
            if (index < 0 || index >= types.length) // make sure to have a valid array index
                return res.getString(notFoundStringId);
            else
                return types[index];
        } catch (Resources.NotFoundException e) {
            Crashlytics.logException(e);
            return res.getString(notFoundStringId);
        }
    }

    public void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void setText() {
        TextView tv1 = (TextView) view.findViewById(R.id.birthdaysmall);
        if (activity.record.getProfile().getDetails().getBirthday() == null) {
            tv1.setText(R.string.not_specified);
        } else {
            String birthday = MALDateTools.formatDateString(activity.record.getProfile().getDetails().getBirthday(), activity, false);
            tv1.setText(birthday.equals("") ? activity.record.getProfile().getDetails().getBirthday() : birthday);
        }
        TextView tv2 = (TextView) view.findViewById(R.id.locationsmall);
        if (activity.record.getProfile().getDetails().getLocation() == null) {
            tv2.setText(R.string.not_specified);
        } else {
            tv2.setText(activity.record.getProfile().getDetails().getLocation());
        }
        TextView tv25 = (TextView) view.findViewById(R.id.websitesmall);
        TextView tv26 = (TextView) view.findViewById(R.id.websitefront);
        Card tv36 = (Card) view.findViewById(R.id.details_card);
        if (activity.record.getProfile().getDetails().getWebsite() != null && activity.record.getProfile().getDetails().getWebsite().contains("http://") && activity.record.getProfile().getDetails().getWebsite().contains(".")) { // filter fake websites
            tv25.setText(activity.record.getProfile().getDetails().getWebsite().replace("http://", ""));
        } else {
            tv25.setVisibility(View.GONE);
            tv26.setVisibility(View.GONE);
        }
        TextView tv3 = (TextView) view.findViewById(R.id.commentspostssmall);
        tv3.setText(String.valueOf(activity.record.getProfile().getDetails().getComments()));
        TextView tv4 = (TextView) view.findViewById(R.id.forumpostssmall);
        tv4.setText(String.valueOf(activity.record.getProfile().getDetails().getForumPosts()));
        TextView tv5 = (TextView) view.findViewById(R.id.lastonlinesmall);
        if (activity.record.getProfile().getDetails().getLastOnline() != null) {
            String lastOnline = MALDateTools.formatDateString(activity.record.getProfile().getDetails().getLastOnline(), activity, true);
            tv5.setText(lastOnline.equals("") ? activity.record.getProfile().getDetails().getLastOnline() : lastOnline);
        } else
            tv5.setText("-");
        TextView tv6 = (TextView) view.findViewById(R.id.gendersmall);
        tv6.setText(getStringFromResourceArray(R.array.gender, R.string.not_specified, activity.record.getProfile().getDetails().getGenderInt()));
        TextView tv7 = (TextView) view.findViewById(R.id.joindatesmall);
        if (activity.record.getProfile().getDetails().getJoinDate() != null) {
            String joinDate = MALDateTools.formatDateString(activity.record.getProfile().getDetails().getJoinDate(), activity, false);
            tv7.setText(joinDate.equals("") ? activity.record.getProfile().getDetails().getJoinDate() : joinDate);
        } else
            tv7.setText("-");
        TextView tv8 = (TextView) view.findViewById(R.id.accessranksmall);
        tv8.setText(activity.record.getProfile().getDetails().getAccessRank());
        TextView tv9 = (TextView) view.findViewById(R.id.animelistviewssmall);
        tv9.setText(String.valueOf(activity.record.getProfile().getDetails().getAnimeListViews()));
        TextView tv10 = (TextView) view.findViewById(R.id.mangalistviewssmall);
        tv10.setText(String.valueOf(activity.record.getProfile().getDetails().getMangaListViews()));

        TextView tv11 = (TextView) view.findViewById(R.id.atimedayssmall);
        tv11.setText(activity.record.getProfile().getAnimeStats().getTimeDays().toString());
        TextView tv12 = (TextView) view.findViewById(R.id.awatchingsmall);
        tv12.setText(String.valueOf(activity.record.getProfile().getAnimeStats().getWatching()));
        TextView tv13 = (TextView) view.findViewById(R.id.acompletedpostssmall);
        tv13.setText(String.valueOf(activity.record.getProfile().getAnimeStats().getCompleted()));
        TextView tv14 = (TextView) view.findViewById(R.id.aonholdsmall);
        tv14.setText(String.valueOf(activity.record.getProfile().getAnimeStats().getOnHold()));
        TextView tv15 = (TextView) view.findViewById(R.id.adroppedsmall);
        tv15.setText(String.valueOf(activity.record.getProfile().getAnimeStats().getDropped()));
        TextView tv16 = (TextView) view.findViewById(R.id.aplantowatchsmall);
        tv16.setText(String.valueOf(activity.record.getProfile().getAnimeStats().getPlanToWatch()));
        TextView tv17 = (TextView) view.findViewById(R.id.atotalentriessmall);
        tv17.setText(String.valueOf(activity.record.getProfile().getAnimeStats().getTotalEntries()));

        TextView tv18 = (TextView) view.findViewById(R.id.mtimedayssmall);
        tv18.setText(activity.record.getProfile().getMangaStats().getTimeDays().toString());
        TextView tv19 = (TextView) view.findViewById(R.id.mwatchingsmall);
        tv19.setText(String.valueOf(activity.record.getProfile().getMangaStats().getReading()));
        TextView tv20 = (TextView) view.findViewById(R.id.mcompletedpostssmall);
        tv20.setText(String.valueOf(activity.record.getProfile().getMangaStats().getCompleted()));
        TextView tv21 = (TextView) view.findViewById(R.id.monholdsmall);
        tv21.setText(String.valueOf(activity.record.getProfile().getMangaStats().getOnHold()));
        TextView tv22 = (TextView) view.findViewById(R.id.mdroppedsmall);
        tv22.setText(String.valueOf(activity.record.getProfile().getMangaStats().getDropped()));
        TextView tv23 = (TextView) view.findViewById(R.id.mplantowatchsmall);
        tv23.setText(String.valueOf(activity.record.getProfile().getMangaStats().getPlanToRead()));
        TextView tv24 = (TextView) view.findViewById(R.id.mtotalentriessmall);
        tv24.setText(String.valueOf(activity.record.getProfile().getMangaStats().getTotalEntries()));
    }

    public void refresh() {
        if (activity.record == null) {
            if (MALApi.isNetworkAvailable(context)) {
                Toast.makeText(context, R.string.toast_error_UserRecord, Toast.LENGTH_SHORT).show();
            } else {
                toggle(2);
            }
        } else {
            card();
            setText();
            setcolor();

            Picasso.with(context)
                    .load(activity.record.getProfile()
                            .getAvatarUrl())
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
        }
    }

    @Override
    public void onRefresh() {
        activity.getRecords();
    }
}
