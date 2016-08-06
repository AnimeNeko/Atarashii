package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class ListStats implements Serializable {
    @Setter
    @SerializedName("plan_to_watch")
    private int planToWatch = 0;
    @Setter
    @SerializedName("plan_to_read")
    private int planToRead = 0;
    @Setter
    private int watching = 0;
    @Setter
    private int reading = 0;
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

    public int getPlanned() {
        return planToWatch > 0 ? planToWatch : planToRead;
    }

    public int getReadWatch() {
        return watching > 0 ? watching : reading;
    }
}