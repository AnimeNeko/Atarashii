package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Anime extends GenericRecord implements Serializable {
    @Getter
    @Setter
    private int duration;
    @Getter
    @Setter
    @SerializedName("airing_status")
    private String airingStatus;
    @Getter
    @Setter
    @SerializedName("total_episodes")
    private int totalEpisodes;
    @Getter
    @Setter
    @SerializedName("youtube_id")
    private String youtubeId;

    @Getter
    @Setter
    @SerializedName("list_stats")
    private ListStats listStats;
    @Getter
    @Setter
    private Airing airing;

    public static class Airing implements Serializable {
        @Getter
        @Setter
        private String time;
        @Getter
        @Setter
        private int countdown;
        @Getter
        @Setter
        @SerializedName("next_episode")
        private int nextEpisode;
    }

    public class ListStats implements Serializable {
        @Getter
        @Setter
        @SerializedName("plan_to_watch")
        private int planToWatch;
        @Getter
        @Setter
        private int watching;
        @Getter
        @Setter
        private int completed;
        @Getter
        @Setter
        @SerializedName("on_hold")
        private int onHold;
        @Getter
        @Setter
        private int dropped;
    }

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime();
        createGeneralBaseModel(model);

        model.setDuration(getDuration());
        model.setStatus(getAiringStatus());
        model.setEpisodes(getTotalEpisodes());
        model.setYoutubeId(getYoutubeId());
        model.setListStats(getListStats());
        model.setAiring(getAiring());
        return model;
    }
}
