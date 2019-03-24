package com.edlplan.audiov.core.audio;

@FunctionalInterface
public interface OnAudioChangeListener {
    void onAudioChange(IAudioEntry pre, IAudioEntry next);
}
