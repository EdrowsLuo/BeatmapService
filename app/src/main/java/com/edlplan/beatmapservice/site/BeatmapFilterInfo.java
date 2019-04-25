package com.edlplan.beatmapservice.site;

import com.edlplan.beatmapservice.util.Pair;

import java.util.Locale;

public class BeatmapFilterInfo {

    private String keyWords = null;

    private int modes = GameModes.STD | GameModes.TAIKO | GameModes.CTB | GameModes.MANIA;

    private int rankedState = RankedState.BinaryCode.MASK;

    private int beatmapListType = BeatmapListType.HOT;

    private ValueLimit valueLimit;

    public void setValueLimit(ValueLimit valueLimit) {
        this.valueLimit = valueLimit;
    }

    public ValueLimit getValueLimit() {
        return valueLimit;
    }

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

    public static class ValueLimit {

        public static final int SIZE_NOT_MATCH = 1;
        public static final int FORMAT_NOT_MATCH = 2;

        //"star:0~10,AR:0~10,OD:0~10,CS:0~10,HP:0~10,length:0~999,BPM:0~9999,end"

        public Limit star = new Limit(0, 1000);

        public Limit ar = new Limit(0, 10);
        public Limit od = new Limit(0, 10);
        public Limit cs = new Limit(0, 10);
        public Limit hp = new Limit(0, 10);
        public Limit length = new Limit(0, 9999);

        public Limit bpm = new Limit(0, 9999);

        public String toSayoString() {
            star.check();
            ar.check();
            od.check();
            cs.check();
            hp.check();
            length.check();
            bpm.check();
            return String.format(Locale.getDefault(),
                    "star:%d~%d,AR:%d~%d,OD:%d~%d,CS:%d~%d,HP:%d~%d,length:%d~%d,BPM:%d~%d",
                    star.min, star.max,
                    ar.min, ar.max,
                    od.min, od.max,
                    cs.min, cs.max,
                    hp.min, hp.max,
                    length.min, length.max,
                    bpm.min, bpm.max);
        }

        public static int parseInto(String info, ValueLimit limit) {
            String[] list = info.split(",");
            if (list.length != 7) {
                return SIZE_NOT_MATCH;
            }

            for (int i = 0; i < 7; i++) {
                String[] sp = list[i].split(":");
                if (sp.length != 2) {
                    return FORMAT_NOT_MATCH;
                }
                String[] value = sp[1].split("~");
                if (value.length != 2) {
                    return FORMAT_NOT_MATCH;
                }
                Limit v;
                switch (sp[0].toLowerCase()) {
                    case "star":
                        v = limit.star;
                        break;
                    case "ar":
                        v = limit.ar;
                        break;
                    case "od":
                        v = limit.od;
                        break;
                    case "cs":
                        v = limit.cs;
                        break;
                    case "hp":
                        v = limit.hp;
                        break;
                    case "length":
                        v = limit.length;
                        break;
                    case "bpm":
                        v = limit.bpm;
                        break;
                    default:
                        v = null;
                }
                if (v == null) {
                    return FORMAT_NOT_MATCH;
                }
                try {
                    v.min = Integer.parseInt(value[0]);
                    v.max = Integer.parseInt(value[1]);
                } catch (NumberFormatException e) {
                    return FORMAT_NOT_MATCH;
                }
            }
            return 0;
        }


        public static class Limit {
            int max;
            int min;

            public Limit() {

            }

            public Limit(int min, int max) {
                this.min = min;
                this.max = max;
            }

            public void check() {
                if (min > max) {
                    min = max;
                }
                if (min == max) {
                    max = min + 1;
                }
            }
        }
    }
}
