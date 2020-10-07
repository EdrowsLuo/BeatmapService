package com.edlplan.beatmapservice.site;

import java.util.List;

public interface IBeatmapDetailSite {

    List<BeatmapInfo> getBeatmapInfo(IBeatmapSetInfo setInfo);

    BeatmapInfoV2 getBeatmapInfoV2(IBeatmapSetInfo setInfo);
}
