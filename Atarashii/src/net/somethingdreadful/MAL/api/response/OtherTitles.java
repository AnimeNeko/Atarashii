package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;
import java.util.ArrayList;

public class OtherTitles implements Serializable {
    private ArrayList<String> english;
    private ArrayList<String> japanese;
    private ArrayList<String> synonyms;

    public ArrayList<String> getEnglish() {
        return english;
    }

    public void setEnglish(ArrayList<String> english) {
        this.english = english;
    }

    public ArrayList<String> getJapanese() {
        return japanese;
    }

    public void setJapanese(ArrayList<String> japanese) {
        this.japanese = japanese;
    }

    public ArrayList<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(ArrayList<String> synonyms) {
        this.synonyms = synonyms;
    }
}
