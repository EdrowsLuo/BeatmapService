package com.edlplan.beatmapservice.ui;

import com.edlplan.beatmapservice.util.Setter;
import com.edlplan.beatmapservice.util.Updatable;

public class Animation {

    private long startTime;

    private double startValue;

    private double endValue;

    private int duration;

    private Easing easing = Easing.None;

    private Setter<Double> setter;

    private Updatable<Integer> onUpdate;

    private boolean running = false;

    private void onUpdate(int i) {
        if (onUpdate != null) {
            onUpdate.update(i);
        }
    }

    public void update() {
        if (!running) return;
        int p = (int) (System.currentTimeMillis() - startTime);
        if (p < 0) {
            setter.set(startValue);
            onUpdate(0);
            return;
        } else if (p <= duration) {
            double pp = duration == 0 ? 1 : (p / (double) duration);
            pp = EasingManager.apply(easing, pp);
            setter.set(startValue * (1 - pp) + endValue * pp);
            onUpdate(duration);
            return;
        } else if (p > duration) {
            setter.set(endValue);
            onUpdate(duration);
            running = false;
            return;
        }
    }

    public void start() {
        running = true;
        startTime = System.currentTimeMillis();
    }

    public void setSetter(Setter<Double> setter) {
        this.setter = setter;
    }

    public void setOnUpdateListener(Updatable<Integer> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setValue(double startValue, double endValue, int duration, Easing easing) {
        this.endValue = endValue;
        this.startValue = startValue;
        this.easing = easing;
        this.duration = duration;
    }
}
