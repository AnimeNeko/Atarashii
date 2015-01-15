package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Forum implements Serializable {
    @Setter @Getter private int id = 0;
    @Setter @Getter private String name;
    @Setter @Getter private String username;
    @Setter @Getter private int replies = 0;
    @Setter @Getter private String description;
    @Setter @Getter private Forum reply;
    @Setter @Getter private ArrayList<Forum> children;
    @Setter @Getter private String comment;
    @Setter @Getter private String time;
    @Setter @Getter private Profile profile;
}
