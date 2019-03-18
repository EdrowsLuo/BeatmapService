package com.edlplan.beatmapservice.site;

public interface IBeatmapInfoSite {

    boolean hasMoreBeatmapSet();

    void tryToLoadMoreBeatmapSet();

    int getLoadedBeatmapSetCount();

    IBeatmapSetInfo getInfoAt(int i);

    void applyFilterInfo(BeatmapFilterInfo filterInfo);

    void reset();

}
