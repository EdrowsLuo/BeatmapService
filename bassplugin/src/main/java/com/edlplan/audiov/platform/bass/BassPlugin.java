package com.edlplan.audiov.platform.bass;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.IAudioEntry;

public class BassPlugin implements AudioVCore.PlatformAudio {

    public static BassPlugin INSTANCE = new BassPlugin();

    @Override
    public IAudioEntry.AFactory getAudioFactory() {
        return BassEntry.FACTORY;
    }
}
