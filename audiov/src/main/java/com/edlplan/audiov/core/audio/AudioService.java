package com.edlplan.audiov.core.audio;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.utils.Consumer;
import com.edlplan.audiov.core.utils.ListenerGroup;

import java.io.File;
import java.util.LinkedList;

/**
 * 通过单独的一条线程来管理音频的服务
 */
public class AudioService implements Runnable {

    private final LinkedList<Consumer<AudioService>> postEvents = new LinkedList<>();
    private LoopFlag loopFlag = LoopFlag.Pause;
    private boolean serviceStarted = false;
    private Thread holdingThread;
    private int loopDeltaTimeMS = 50;
    private IAudioEntry preAudioEntry;
    private IAudioEntry audioEntry;
    private ListenerGroup<OnAudioChangeListener> onAudioChangeListener
            = ListenerGroup.create(l -> l.onAudioChange(preAudioEntry, audioEntry));

    private ListenerGroup<OnAudioProgressListener> onAudioProgressListener
            = ListenerGroup.create(l -> l.onProgress(audioEntry, audioEntry.position()));

    private boolean hasCallComplete = false;

    private ListenerGroup<OnAudioCompleteListener> onAudioCompleteListener
            = ListenerGroup.create(l -> l.onAudioComplete(audioEntry));

    private float volume = 1;

    @Override
    public void run() {
        //System.out.println("test::serviceStart");
        while (true) {
            handleEvents();
            if (loopFlag == LoopFlag.Pause) {

            } else if (loopFlag == LoopFlag.Stop) {
                break;
            } else if (loopFlag == LoopFlag.Running) {
                if (audioEntry != null) {
                    onAudioProgressListener.handle();
                    if (!hasCallComplete) {
                        if (audioEntry.position() >= audioEntry.length()) {
                            hasCallComplete = true;
                            onAudioCompleteListener.handle();
                        }
                    }
                }
            }

            try {
                Thread.sleep(loopDeltaTimeMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("test::serviceEnd");
    }

    private void handleEvents() {
        synchronized (postEvents) {
            while (!postEvents.isEmpty()) {
                postEvents.getFirst().consume(this);
                postEvents.removeFirst();
            }
        }
    }

    public void setVolume(float volume) {
        post(audioService -> {
            this.volume = volume;
            if (audioEntry != null) {
                audioEntry.setVolume(volume);
            }
        });
    }

    public void setPosition(double position) {
        post(audioService -> {
            if (audioEntry != null) {
                audioEntry.seekTo(position);
            }
        });
    }

    public void changeAudio(File file, boolean playWhenChanged) {
        changeAudio(AudioVCore.getInstance().audio().getAudioFactory().create(file), playWhenChanged);
    }

    public void changeAudio(IAudioEntry nextEntry, boolean playWhenChanged) {
        if (loopFlag == LoopFlag.Stop) {
            return;
        }
        if (!serviceStarted) {
            play();
        }
        post(audioService -> {
            preAudioEntry = audioEntry;
            if (preAudioEntry != null) {
                preAudioEntry.stop();
            }
            audioEntry = nextEntry;
            if (playWhenChanged) {
                playAudio();
            }
            onAudioChangeListener.handle();
            preAudioEntry = null;
        });
    }

    private void playAudio() {
        if (loopFlag == LoopFlag.Stop) {
            return;
        }
        loopFlag = LoopFlag.Running;
        if (audioEntry != null) {
            hasCallComplete = false;
            audioEntry.play();
            audioEntry.setVolume(volume);
        }
    }

    public void post(Consumer<AudioService> consumer) {
        synchronized (postEvents) {
            postEvents.addLast(consumer);
        }
    }

    public IAudioEntry getAudioEntry() {
        return audioEntry;
    }

    public void play() {
        if (loopFlag == LoopFlag.Stop) {
            return;
        }
        if (serviceStarted) {
            loopFlag = LoopFlag.Running;
            post(audioService -> playAudio());
        } else {
            loopFlag = LoopFlag.Running;
            serviceStarted = true;
            holdingThread = new Thread(this);
            holdingThread.start();
            playAudio();
        }
    }

    public void stop() {
        if (serviceStarted) {
            loopFlag = LoopFlag.Stop;
        }
    }

    public void pause() {
        if (serviceStarted && loopFlag != LoopFlag.Stop) {
            loopFlag = LoopFlag.Pause;
            if (audioEntry != null) {
                audioEntry.pause();
            }
        }
    }

    public void registerOnAudioChangeListener(OnAudioChangeListener listener) {
        post(a -> onAudioChangeListener.register(listener));
    }

    public void unregisterOnAudioChangeListener(OnAudioChangeListener listener) {
        post(a -> onAudioChangeListener.unregiser(listener));
    }

    public void registerOnAudioCompleteListener(OnAudioCompleteListener listener) {
        post(a -> onAudioCompleteListener.register(listener));
    }

    public void unregiserAudioCompleteListener(OnAudioCompleteListener listener) {
        post(a -> onAudioCompleteListener.unregiser(listener));
    }

    public void registerOnAudioProgressListener(OnAudioProgressListener listener) {
        post(a -> onAudioProgressListener.register(listener));
    }

    public void unregiserAudioProgressListener(OnAudioProgressListener listener) {
        post(a -> onAudioProgressListener.unregiser(listener));
    }

    enum LoopFlag {
        Pause, //暂停
        Running, //循环中
        Stop //停止，会终止服务
    }
}
