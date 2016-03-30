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

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime();
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(true);
        createGeneralBaseModel(model);

        model.setDuration(getDuration());
        model.setStatus(getAiringStatus());
        model.setEpisodes(getTotalEpisodes());
        model.setYoutubeId(getYoutubeId());
        model.setAiring(getAiring());
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(false);
        return model;
    }
}
