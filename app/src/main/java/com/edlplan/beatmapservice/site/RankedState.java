package com.edlplan.beatmapservice.site;

public class RankedState {

    public static final class BinaryCode {

        public static final int PENDING = 1, RANKED = 2, QUALIFIED = 4, LOVED = 8, GRAVEYARD = 16;

        public static final int MASK = PENDING | RANKED | QUALIFIED | LOVED | GRAVEYARD;

    }

    public static final int PENDING = 0, RANKED = 1, QUALIFIED = 3, LOVED = 4, GRAVEYARD = -2, WIP = -1, APPROVED = 2;

    private static final String[] STATE_NAME_LIST = {
            "GRAVEYARD",
            "WIP",
            "PENDING",
            "RANKED",
            "APPROVED",
            "QUALIFIED",
            "LOVED",
            "GRAVEYARD"
    };

    public static String stateIntToString(int state) {
        return STATE_NAME_LIST[state + 2];
    }

}
