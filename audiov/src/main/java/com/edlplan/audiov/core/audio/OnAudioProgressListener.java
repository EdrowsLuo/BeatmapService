package com.edlplan.audiov.core.audio;

@FunctionalInterface
public interface OnAudioProgressListener {
    void onProgress(IAudioEntry audioEntry, double ms);
}
