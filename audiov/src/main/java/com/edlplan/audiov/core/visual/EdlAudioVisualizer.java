package com.edlplan.audiov.core.visual;

import com.edlplan.audiov.core.graphics.ACanvas;
import com.edlplan.audiov.core.graphics.ATexture;
import com.edlplan.audiov.core.option.OptionEntry;

import java.io.IOException;
import java.util.Map;

public class EdlAudioVisualizer extends BaseVisualizer {

    public static final String DRAW_CURSOR = "@draw cursor";
    private static final int range = 2;

    private float ang = 0;
    private float lk;
    private float[] lastBytes;

    private long drawTime = -1;
    private long deltaTime = -1;
    private float[] rf;
    private float maxBarLength = 150;
    private float keepLength = 100;
    private float clearRate = 180f / 255;
    private float scale = 1;
    private boolean drawCursor = true;
    private ATexture osu_icon_white, lighting;
    private float[] mBytes;
    private float[] mPoints;
    private ATexture backBuffer;
    private ATexture nowView;
    private double beatAngle = 0;
    private float preBeatX = 0;
    private float preBeatY = 0;
    private float minDeltaDis = 10;

    @Override
    public Map<String, OptionEntry<?>> dumpOptions() {
        Map<String, OptionEntry<?>> tmp = super.dumpOptions();
        OptionEntry<Boolean> drawCursor = new OptionEntry<>(DRAW_CURSOR);
        drawCursor.setData(this.drawCursor);
        tmp.put(drawCursor.getName(), drawCursor);
        return tmp;
    }

    @Override
    public void applyOptions(Map<String, OptionEntry<?>> options) {
        super.applyOptions(options);
        if (options.containsKey(DRAW_CURSOR)) {
            OptionEntry entry = options.get(DRAW_CURSOR);
            if (entry.getData() instanceof Boolean) {
                drawCursor = (Boolean) entry.getData();
            }
        }
    }

    @Override
    protected void onPrepare() {
        super.onPrepare();
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        osu_icon_white = ATexture.getFactory().createFromAssets("logo_white.png");
        lighting = ATexture.getFactory().createFromAssets("cursor.png");
        mBytes = null;
    }

    @Override
    protected void onUpdateFFT() {
        super.onUpdateFFT();
        updateVisualizer(fftData);
    }

    private void updateVisualizer(float[] fft) {
        float[] model = new float[fft.length / 2 + 1];
        model[0] = (byte) Math.abs(fft[0] * 127);
        for (int i = 2, j = 1; j < model.length && (i + 1) < fft.length; ) {
            model[j] = (float) Math.sqrt((fft[i] * fft[i] + fft[i + 1] * fft[i + 1]) * 127 * 127 / 2);
            i += 2;
            j++;
        }
        mBytes = model;
    }

    private float limitBarLength(float raw) {
        if (raw < keepLength) {
            return raw;
        } else {
            return (float) (keepLength + (maxBarLength - keepLength) * Math.pow(Math.atan((raw - keepLength) / 50) * 2 / Math.PI, 2));
        }
    }

    private void updateData() {

        /*--------------------------------------------------------------------
         *                      处理帧时间差
         *-------------------------------------------------------------------*/
        if (drawTime != -1) {
            deltaTime = System.currentTimeMillis() - drawTime;
        }
        drawTime = System.currentTimeMillis();


        /*--------------------------------------------------------------------
         *                      处理音乐频谱数据
         *-------------------------------------------------------------------*/
        if (mBytes == null) {
            return;
        }

        int barCount = mBytes.length - 20;

        float mul = 0;
        for (int i = 0; i < mBytes.length; i++) {
            if (mBytes[i] < 0) {
                mBytes[i] = 127;
            }
            mul += mBytes[i];
        }
        mul = (float) Math.sqrt(0.15f * mul / (mBytes.length));

        float k = 0;
        for (int i = 0; i < mBytes.length; i++) {
            k += (i + 4) * mBytes[i];
        }
        k = k / mBytes.length;
        k = (float) Math.sqrt(k) / 7;
        k = (float) Math.sqrt(k);

        if (rf == null || rf.length != barCount) rf = new float[barCount];

        int btimes = 1;
        if (barCount % 5 != 0) {
            btimes *= 5;
        }

        for (int i = 0; i < rf.length; i++) {
            rf[i] = (mBytes[i]) /* *k */ * (2 + i * 0.11f) / 2;
        }
        rf = calculateVolume(rf);

        float change = 0;
        if (lastBytes == null || lastBytes.length != rf.length) lastBytes = new float[rf.length];

        for (int i = 0; i < rf.length; i++) {
            change += ((lastBytes[i] < rf[i]) ? (rf[i] - lastBytes[i]) : (0));
            lastBytes[i] = rf[i];
        }
        change = (float) Math.pow(change, 1.5) / 1500;

        float[] ra;
        for (int i = 0; i < rf.length; i++) {
            rf[i] = rf[((btimes * i) % barCount)] * k;
        }
        ra = rf;

        //smoth(rf,0,barCount);

        float deltaBeat = Math.max(k - lk, 0);
        if (deltaTime > 0 && deltaTime < 200)
            ang += (Math.abs(k - lk) + 0.08 * k) * 0.006 * deltaTime;

        float ryAdd = 0;

        boolean isBeat = false;
        float cPan = 210f / 255;
        if (k > lk * 1.14 + 0.02) {
            ryAdd = Math.min((float) (Math.pow(k - lk, 0.3) * 20), 25);
            cPan = 150f / 255;
            isBeat = true;
        }

        lk = k;



        /*--------------------------------------------------------------------
         *                      进行绘制
         *-------------------------------------------------------------------*/
        ACanvas nowCanvas = ACanvas.of(nowView);
        nowCanvas.start();
        nowCanvas.clear(0, 0, 0, 0);
        nowCanvas.drawTexture(backBuffer, 0, 0, clearRate);
        nowCanvas.end();

        ACanvas bufferCanvas = ACanvas.of(backBuffer);
        bufferCanvas.start();
        bufferCanvas.clear(0, 0, 0, 0);
        bufferCanvas.end();

        if (mPoints == null || mPoints.length < (barCount) * 4) {
            mPoints = new float[(barCount) * 4];
        }

        float rawrng = k * 60 + 137;
        float rng = rawrng + ryAdd;

        final float deltaAngle = (float) ((2 * Math.PI / (barCount - 1)));
        float angi = ((int) (ang / deltaAngle)) * deltaAngle;
        int r = (int) (165 + k * 25);
        for (int i = 0; i < barCount; i++) {
            float bl = limitBarLength(ra[i]);
            mPoints[i * 4] = (float) (nowView.getWidth() / 2 + Math.cos(angi + i * deltaAngle) * r * scale);
            mPoints[i * 4 + 1] = (float) (nowView.getHeight() / 2 - Math.sin(angi + i * deltaAngle) * r * scale);
            mPoints[i * 4 + 2] = (float) (nowView.getWidth() / 2 + Math.cos(angi + i * deltaAngle) * (bl + r) * scale);
            mPoints[i * 4 + 3] = (float) (nowView.getHeight() / 2 - Math.sin(angi + i * deltaAngle) * (bl + r) * scale);
        }

        nowCanvas.start();
        nowCanvas.drawLines(mPoints, (float) (3.2f * (scale > 1 ? scale : Math.sqrt(scale))), 0, 128f / 255, 1, (float) Math.sin(k));
        rng *= scale;
        nowCanvas.drawTexture(
                osu_icon_white,
                0, 0, osu_icon_white.getWidth(), osu_icon_white.getHeight(),
                nowView.getWidth() / 2 - rng, nowView.getHeight() / 2 - rng, rng * 2, rng * 2,
                cPan
        );

        if (drawCursor) {//绘制光标
            float cr = rawrng * scale;
            float sr = cr * 0.2f;
            double dta = Math.pow(k, 5) * (1 + deltaBeat * 20) * 0.04;
            if (dta > 2 * Math.PI) {
                dta = Math.PI * 3 / 2;
            }
            beatAngle -= dta;
            if (isBeat) {
                sr *= 1.2f;
            }

            float beatX = (float) (cr * 0.5 * Math.cos(beatAngle));
            float beatY = (float) (cr * 0.5 * Math.sin(beatAngle));

            double l = Math.hypot(beatX - preBeatX, beatY - preBeatY);

            if (preBeatY != 0) {
                double ll = minDeltaDis;
                while (ll < l) {
                    double itp = ll / l;
                    float ir = (float) (sr * (itp + 1) / 2);
                    nowCanvas.drawTexture(
                            lighting,
                            0, 0, lighting.getWidth(), lighting.getHeight(),
                            (float) (beatX * itp + preBeatX * (1 - itp) + nowView.getWidth() / 2 - ir),
                            (float) (beatY * itp + preBeatY * (1 - itp) + nowView.getHeight() / 2 - ir),
                            ir * 2, ir * 2,
                            (float) (1 * itp + clearRate * (1 - itp))
                    );
                    ll += minDeltaDis;
                }
            }

            preBeatX = beatX;
            preBeatY = beatY;

            nowCanvas.drawTexture(
                    lighting,
                    0, 0, lighting.getWidth(), lighting.getHeight(),
                    beatX + nowView.getWidth() / 2 - sr,
                    beatY + nowView.getHeight() / 2 - sr,
                    sr * 2, sr * 2,
                    1
            );
        }


        nowCanvas.end();

        bufferCanvas.start();
        bufferCanvas.drawTexture(nowView, 0, 0, 1);
        bufferCanvas.end();
    }

    private float[] calculateVolume(float[] f) {
        float[] r = new float[f.length];
        int start = 0;
        int end = 0;
        int count = 0;
        float rv;
        for (int i = 0; i < r.length; i++) {
            rv = 0;
            start = (i - range < 0) ? 0 : (i - range);
            end = (i + range >= r.length) ? (r.length - 1) : (i + range);
            count = end - start + 1;
            for (int j = start; j <= end; j++) {
                rv += f[j];
            }
            rv = rv / (count * 255);
            rv = (float) Math.pow(rv, 0.15);
            r[i] = rv * f[i] * 10;
        }
        return r;
    }

    @Override
    protected void onBaseSizeChanged(int newSize) {
        super.onBaseSizeChanged(newSize);
        scale = newSize / 720f;
        backBuffer = ATexture.getFactory().create(newSize, newSize);
        nowView = ATexture.getFactory().create(newSize, newSize);
    }

    @Override
    public void draw() {
        if (entry != null && entry.isPlaying()) {
            updateData();
        }
    }

    @Override
    public ATexture getResult() {
        return nowView;
    }
}
