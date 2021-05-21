package com.edlplan.beatmapservice.site;

import java.util.ArrayList;

public interface IBeatmapListSite {

    boolean hasMoreBeatmapSet();

    void tryToLoadMoreBeatmapSet();

    int getLoadedBeatmapSetCount();

    ArrayList<Integer> getNewLoadedSetsIDs();

    IBeatmapSetInfo getInfoAt(int i);

    void applyFilterInfo(BeatmapFilterInfo filterInfo);

    void reset();

}
