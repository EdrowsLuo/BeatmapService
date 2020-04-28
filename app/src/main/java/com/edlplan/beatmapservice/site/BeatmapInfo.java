package com.edlplan.beatmapservice.site;

import java.io.Serializable;

public class BeatmapInfo implements Serializable {

    int bid;

    int mode; //1

    String version; //1

    int length; //1

    double bpm;

    double circleSize; //2

    double approachRate; //2

    double overallDifficulty; //2

    double hP; //2

    double star; //1

    double aim; //3

    double speed; //3

    double pp; //1

    int circleCount; //3

    int sliderCount; //3

    int spinnerCount; //3

    int maxCombo; //1

    int playcount; //3

    int passcount; //3

    int[] strainAim; //3

    int[] strainSpeed; //3

    String backgroundUrl;

    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    public double getBpm() {
        return bpm;
    }

    public int[] getStrainAim() {
        return strainAim;
    }

    public void setStrainAim(int[] strainAim) {
        this.strainAim = strainAim;
    }

    public int[] getStrainSpeed() {
        return strainSpeed;
    }

    public void setStrainSpeed(int[] strainSpeed) {
        this.strainSpeed = strainSpeed;
    }

    public int getPasscount() {
        return passcount;
    }

    public void setPasscount(int passcount) {
        this.passcount = passcount;
    }

    public int getPlaycount() {
        return playcount;
    }

    public void setPlaycount(int playcount) {
        this.playcount = playcount;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getCircleSize() {
        return circleSize;
    }

    public void setCircleSize(double circleSize) {
        this.circleSize = circleSize;
    }

    public double getApproachRate() {
        return approachRate;
    }

    public void setApproachRate(double approachRate) {
        this.approachRate = approachRate;
    }

    public double getOverallDifficulty() {
        return overallDifficulty;
    }

    public void setOverallDifficulty(double overallDifficulty) {
        this.overallDifficulty = overallDifficulty;
    }

    public double getHP() {
        return hP;
    }

    public void setHP(double hP) {
        this.hP = hP;
    }

    public double getStar() {
        return star;
    }

    public void setStar(double star) {
        this.star = star;
    }

    public double getAim() {
        return aim;
    }

    public void setAim(double aim) {
        this.aim = aim;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getPP() {
        return pp;
    }

    public void setPP(double pp) {
        this.pp = pp;
    }

    public int getCircleCount() {
        return circleCount;
    }

    public void setCircleCount(int circleCount) {
        this.circleCount = circleCount;
    }

    public int getSliderCount() {
        return sliderCount;
    }

    public void setSliderCount(int sliderCount) {
        this.sliderCount = sliderCount;
    }

    public int getSpinnerCount() {
        return spinnerCount;
    }

    public void setSpinnerCount(int spinnerCount) {
        this.spinnerCount = spinnerCount;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public void setMaxCombo(int maxCombo) {
        this.maxCombo = maxCombo;
    }
}
