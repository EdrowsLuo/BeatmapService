package com.edlplan.beatmapservice.site;

import com.edlplan.beatmapservice.site.sayo.SayoBeatmapDetailSite;
import com.edlplan.beatmapservice.site.sayo.SayoBeatmapListSite;

public class BeatmapSiteManager {

    private static BeatmapSiteManager beatmapSiteManager = new BeatmapSiteManager();

    public static BeatmapSiteManager get() {
        return beatmapSiteManager;
    }

    private IBeatmapListSite infoSite = new SayoBeatmapListSite();

    private IBeatmapDetailSite detailSite = new SayoBeatmapDetailSite();

    public IBeatmapListSite getInfoSite() {
        return infoSite;
    }

    public IBeatmapDetailSite getDetailSite() {
        return detailSite;
    }
}
