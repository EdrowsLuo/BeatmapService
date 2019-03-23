package com.edlplan.beatmapservice.site;

import java.util.List;

public interface IBeatmapDetailSite {

    List<BeatmapInfo> getBeatmapInfo(IBeatmapSetInfo setInfo);

}
