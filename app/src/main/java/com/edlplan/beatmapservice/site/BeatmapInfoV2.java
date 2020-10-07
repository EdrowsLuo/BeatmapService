package com.edlplan.beatmapservice.site;

import java.io.Serializable;
import java.util.List;

/**
 * Auto-generated: 2020-10-07 16:21:19
 *
 * @author www.jsons.cn
 * @website http://www.jsons.cn/json2java/
 */
public class BeatmapInfoV2 implements Serializable {

    private int approved;
    private int approvedDate;
    private String artist;
    private String artistu;
    private List<BidData> bidData;
    private int bidsAmount;
    private int bpm;
    private String creator;
    private int creatorId;
    private int favouriteCount;
    private int genre;
    private int language;
    private int lastUpdate;
    private int localUpdate;
    private int preview;
    private int sid;
    private String source;
    private int storyboard;
    private String tags;
    private String title;
    private String titleu;
    private int video;

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getApproved() {
        return approved;
    }

    public void setApprovedDate(int approvedDate) {
        this.approvedDate = approvedDate;
    }

    public int getApprovedDate() {
        return approvedDate;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtistu(String artistu) {
        this.artistu = artistu;
    }

    public String getArtistu() {
        return artistu;
    }

    public void setBidData(List<BidData> bidData) {
        this.bidData = bidData;
    }

    public List<BidData> getBidData() {
        return bidData;
    }

    public void setBidsAmount(int bidsAmount) {
        this.bidsAmount = bidsAmount;
    }

    public int getBidsAmount() {
        return bidsAmount;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getBpm() {
        return bpm;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setFavouriteCount(int favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public int getFavouriteCount() {
        return favouriteCount;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }

    public int getGenre() {
        return genre;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public int getLanguage() {
        return language;
    }

    public void setLastUpdate(int lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getLastUpdate() {
        return lastUpdate;
    }

    public void setLocalUpdate(int localUpdate) {
        this.localUpdate = localUpdate;
    }

    public int getLocalUpdate() {
        return localUpdate;
    }

    public void setPreview(int preview) {
        this.preview = preview;
    }

    public int getPreview() {
        return preview;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getSid() {
        return sid;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setStoryboard(int storyboard) {
        this.storyboard = storyboard;
    }

    public int getStoryboard() {
        return storyboard;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitleu(String titleu) {
        this.titleu = titleu;
    }

    public String getTitleu() {
        return titleu;
    }

    public void setVideo(int video) {
        this.video = video;
    }

    public int getVideo() {
        return video;
    }

    /**
     * Auto-generated: 2020-10-07 16:21:19
     *
     * @author www.jsons.cn
     * @website http://www.jsons.cn/json2java/
     */
    public static class BidData implements Serializable {

        private int ar;
        private int cs;
        private int hp;
        private double od;
        private double aim;
        private String audioUrl;
        private String backgroundUrl;
        private int bid;
        private int circles;
        private int hit300window;
        private String img;
        private int length;
        private int maxcombo;
        private int mode;
        private int passcount;
        private int playcount;
        private double pp;
        private double ppAcc;
        private double ppAim;
        private double ppSpeed;
        private int sliders;
        private double speed;
        private int spinners;
        private double star;
        private String strainAim;
        private String strainSpeed;
        private String version;

        public void setAr(int ar) {
            this.ar = ar;
        }

        public int getAr() {
            return ar;
        }

        public void setCs(int cs) {
            this.cs = cs;
        }

        public int getCs() {
            return cs;
        }

        public void setHp(int hp) {
            this.hp = hp;
        }

        public int getHp() {
            return hp;
        }

        public void setOd(double od) {
            this.od = od;
        }

        public double getOd() {
            return od;
        }

        public void setAim(double aim) {
            this.aim = aim;
        }

        public double getAim() {
            return aim;
        }

        public void setAudioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
        }

        public String getAudioUrl() {
            return audioUrl;
        }

        public void setBackgroundUrl(String backgroundUrl) {
            this.backgroundUrl = backgroundUrl;
        }

        public String getBackgroundUrl() {
            return backgroundUrl;
        }

        public void setBid(int bid) {
            this.bid = bid;
        }

        public int getBid() {
            return bid;
        }

        public void setCircles(int circles) {
            this.circles = circles;
        }

        public int getCircles() {
            return circles;
        }

        public void setHit300window(int hit300window) {
            this.hit300window = hit300window;
        }

        public int getHit300window() {
            return hit300window;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getImg() {
            return img;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public void setMaxcombo(int maxcombo) {
            this.maxcombo = maxcombo;
        }

        public int getMaxcombo() {
            return maxcombo;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }

        public void setPasscount(int passcount) {
            this.passcount = passcount;
        }

        public int getPasscount() {
            return passcount;
        }

        public void setPlaycount(int playcount) {
            this.playcount = playcount;
        }

        public int getPlaycount() {
            return playcount;
        }

        public void setPp(double pp) {
            this.pp = pp;
        }

        public double getPp() {
            return pp;
        }

        public void setPpAcc(double ppAcc) {
            this.ppAcc = ppAcc;
        }

        public double getPpAcc() {
            return ppAcc;
        }

        public void setPpAim(double ppAim) {
            this.ppAim = ppAim;
        }

        public double getPpAim() {
            return ppAim;
        }

        public void setPpSpeed(double ppSpeed) {
            this.ppSpeed = ppSpeed;
        }

        public double getPpSpeed() {
            return ppSpeed;
        }

        public void setSliders(int sliders) {
            this.sliders = sliders;
        }

        public int getSliders() {
            return sliders;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpinners(int spinners) {
            this.spinners = spinners;
        }

        public int getSpinners() {
            return spinners;
        }

        public void setStar(double star) {
            this.star = star;
        }

        public double getStar() {
            return star;
        }

        public void setStrainAim(String strainAim) {
            this.strainAim = strainAim;
        }

        public String getStrainAim() {
            return strainAim;
        }

        public void setStrainSpeed(String strainSpeed) {
            this.strainSpeed = strainSpeed;
        }

        public String getStrainSpeed() {
            return strainSpeed;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}


