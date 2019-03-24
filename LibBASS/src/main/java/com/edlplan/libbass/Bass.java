package com.edlplan.libbass;


import com.un4seen.bass.BASS;

public class Bass {
    public static void prepare() {

    }

    static {
        BASS.BASS_Init(-1, 441000, 0);
    }
}

