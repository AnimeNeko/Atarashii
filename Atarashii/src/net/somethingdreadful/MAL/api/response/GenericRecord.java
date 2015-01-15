package net.somethingdreadful.MAL.api.response;

import android.text.Html;
import android.text.Spanned;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class GenericRecord implements Serializable {

    // these are the same for both, so put them in here
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ONHOLD = "on-hold";
    public static final String STATUS_DROPPED = "dropped";

    @Setter @Getter private int id;
    @Setter @Getter private String title;
    @Setter @SerializedName("image_url") private String imageUrl;
    @Setter @Getter private String type;
    @Setter @Getter private String status;
    @Setter @Getter private ArrayList<String> genres;
    @Setter @Getter private ArrayList<String> tags;
    @Setter @Getter private int score;
    @Setter @Getter private int rank;
    @Setter @Getter @SerializedName("members_score") private float membersScore;
    @Setter @Getter @SerializedName("members_count") private int membersCount;
    @Setter @Getter @SerializedName("favorited_count") private int favoritedCount;
    @Setter @Getter @SerializedName("popularity_rank") private int popularityRank;
    @Setter @Getter private String synopsis;
    @Setter @Getter @SerializedName("other_titles") private HashMap<String, ArrayList<String>> otherTitles;

    @Setter @Getter private boolean dirty;
    @Setter @Getter private Date lastUpdate;
    @Setter @Getter @SerializedName("flag_create") private boolean createFlag;
    @Setter @Getter @SerializedName("flag_delete") private boolean deleteFlag;

    @Setter @Getter private transient boolean fromCursor = false;

    public String getImageUrl() {
        // if not loaded from cursor the image might point to an thumbnail
        if (fromcursor)
            return imageUrl;
        else
            return imageUrl.replaceFirst("t.jpg$", ".jpg");
    }

    public ArrayList<Integer> getGenresInt() {
        String[] genres = {
                "Action", "Adventure", "Cars",
                "Comedy", "Dementia", "Demons",
                "Drama", "Ecchi", "Fantasy",
                "Game", "Harem", "Hentai",
                "Historical", "Horror", "Josei",
                "Kids", "Magic", "Martial Arts",
                "Mecha", "Military", "Music",
                "Mystery", "Parody", "Police",
                "Psychological", "Romance", "Samurai",
                "School", "Sci-Fi", "Seinen",
                "Shoujo", "Shoujo Ai", "Shounen",
                "Shounen Ai", "Slice of Life", "Space",
                "Sports", "Super Power", "Supernatural",
                "Thriller", "Vampire", "Yaoi",
                "Yuri"
        };
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (getGenres() != null)
            for (String genre : getGenres())
                result.add(Arrays.asList(genres).indexOf(genre));
        
        return result;
    }

    private ArrayList<String> getOtherTitlesByLanguage(String lang) {
        if (otherTitles == null) {
            return null;
        }
        return otherTitles.get(lang);
    }

    public ArrayList<String> getOtherTitlesJapanese() {
        return getOtherTitlesByLanguage("japanese");
    }

    public ArrayList<String> getOtherTitlesEnglish() {
        return getOtherTitlesByLanguage("english");
    }

    public ArrayList<String> getOtherTitlesSynonyms() {
        return getOtherTitlesByLanguage("synonyms");
    }

    // Use this to get a formatted version of the text suited for display in the application
    public Spanned getSpannedSynopsis() {
        return (getSynopsis() != null ? Html.fromHtml(getSynopsis()) : null);
    }

    public int getUserStatusInt(String statusString) {
        String[] status = {
                "completed",
                "on-hold",
                "dropped",
                "watching",
                "plan to watch",
                "reading",
                "plan to read"
        };
        return Arrays.asList(status).indexOf(statusString);
    }
}
