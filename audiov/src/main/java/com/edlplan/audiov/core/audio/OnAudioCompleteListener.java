package com.edlplan.audiov.core.audio;

@FunctionalInterface
public interface OnAudioCompleteListener {
    void onAudioComplete(IAudioEntry audioEntry);
}
