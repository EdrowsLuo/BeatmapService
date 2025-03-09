package com.edlplan.beatmapservice.site;

import java.io.Serializable;

public interface IBeatmapSetInfo extends Serializable {

    String getTitle();

    String getArtist();

    String getCreator();

    int getBeatmapSetID();

    int getModes();

    int getRankedState();

    int getFavCount();

    int getLastUpdate();
}
