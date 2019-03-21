package com.edlplan.beatmapservice.site;

public class BeatmapFilterInfo {

    private String keyWords = null;

    private int modes = GameModes.STD | GameModes.TAIKO | GameModes.CTB | GameModes.MANIA;

    private int rankedState = RankedState.BinaryCode.MASK;

    private int beatmapListType = BeatmapListType.HOT;

    public int getRankedState() {
        return rankedState;
    }

    public int getModes() {
        return modes;
    }

    public int getBeatmapListType() {
        return beatmapListType;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setRankedState(int rankedState) {
        this.rankedState = rankedState;
    }

    public void setModes(int modes) {
        this.modes = modes;
    }

    public void setBeatmapListType(int beatmapListType) {
        this.beatmapListType = beatmapListType;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }
}
