package com.edlplan.beatmapservice.site;

public class BeatmapSetInfo implements IBeatmapSetInfo {

    private String title, artist, creator;

    private int beatmapSetID = -1;

    private int modes = 0;

    private int rankedState = 0;

    private int favCount;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setBeatmapSetID(int beatmapSetID) {
        this.beatmapSetID = beatmapSetID;
    }

    public void setModes(int modes) {
        this.modes = modes;
    }

    public void setRankedState(int rankedState) {
        this.rankedState = rankedState;
    }

    @Override
    public int getFavCount() {
        return favCount;
    }

    public void setFavCount(int favCount) {
        this.favCount = favCount;
    }

    @Override
    public int getRankedState() {
        return rankedState;
    }

    @Override
    public int getModes() {
        return modes;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public int getBeatmapSetID() {
        return beatmapSetID;
    }
}
