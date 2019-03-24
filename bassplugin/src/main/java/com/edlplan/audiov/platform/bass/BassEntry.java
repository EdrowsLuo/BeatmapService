package com.edlplan.audiov.platform.bass;

import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.libbass.BassChannel;

import java.io.File;

public class BassEntry implements IAudioEntry{

    public static final BassFactory FACTORY = new BassFactory();

    private BassChannel channel;

    public BassEntry(BassChannel channel) {
        this.channel = channel;
    }

    @Override
    public void prepare() {

    }

    @Override
    public void play() {
        channel.play();
    }

    @Override
    public void pause() {
        channel.pause();
    }

    @Override
    public void stop() {
        channel.stop();
    }

    @Override
    public void release() {
        channel.free();
    }

    @Override
    public double position() {
        return channel.currentPlayTimeMS();
    }

    @Override
    public double length() {
        return channel.getLengthS() * 1000;
    }

    @Override
    public void seekTo(double ms) {
        channel.seekTo(ms);
    }

    @Override
    public void setVolume(float volume) {
        channel.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return channel.getVolume();
    }

    @Override
    public void getFFT(float[] array, int type) {
        switch (type) {
            case FLAG_FFT1024:{
                channel.getFFT(array);
                break;
            }
            default:
        }
    }

    @Override
    public boolean isPlaying() {
        return channel.isActive();
    }

    public static class BassFactory extends IAudioEntry.AFactory {

        @Override
        public IAudioEntry create(File file) {
            return new BassEntry(BassChannel.createStreamFromFile(file.getAbsolutePath()));
        }

        @Override
        public IAudioEntry create(byte[] bytes) {
            return new BassEntry(BassChannel.createStreamFromBuffer(bytes));
        }

    }

}

