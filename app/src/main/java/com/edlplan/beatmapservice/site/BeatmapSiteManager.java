package com.edlplan.beatmapservice.site;

import com.edlplan.beatmapservice.site.sayo.SayoBeatmapInfoSite;

public class BeatmapSiteManager {

    private static BeatmapSiteManager beatmapSiteManager = new BeatmapSiteManager();

    public static BeatmapSiteManager get() {
        return beatmapSiteManager;
    }

    private IBeatmapInfoSite infoSite = new SayoBeatmapInfoSite();

    public IBeatmapInfoSite getInfoSite() {
        return infoSite;
    }

}
