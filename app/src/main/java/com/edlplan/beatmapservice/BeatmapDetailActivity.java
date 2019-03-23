package com.edlplan.beatmapservice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edlplan.beatmapservice.download.DownloadCenter;
import com.edlplan.beatmapservice.download.DownloadHolder;
import com.edlplan.beatmapservice.site.BeatmapInfo;
import com.edlplan.beatmapservice.site.BeatmapSetInfo;
import com.edlplan.beatmapservice.site.BeatmapSiteManager;
import com.edlplan.beatmapservice.site.GameModes;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;
import com.edlplan.beatmapservice.site.RankedState;
import com.edlplan.beatmapservice.download.Downloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class BeatmapDetailActivity extends AppCompatActivity {

    public ImageView bigCover;

    public ImageView std, taiko, ctb, mania;

    public TextView rankedStateView, titleView, detailView, sidView, likeCountView;

    private IBeatmapSetInfo info;

    public ProgressBar progress;

    private boolean loaded = false;

    private Downloader.Callback updateCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beatmap_details);
        setHalfTransparent();

        std = findViewById(R.id.imageViewStd);
        taiko = findViewById(R.id.imageViewTaiko);
        ctb = findViewById(R.id.imageViewCatch);
        mania = findViewById(R.id.imageViewMania);
        rankedStateView = findViewById(R.id.rankStateView);
        titleView = findViewById(R.id.titleTextView);
        detailView = findViewById(R.id.detailText);
        sidView = findViewById(R.id.sidTextView);
        likeCountView = findViewById(R.id.likeCount);
        bigCover = findViewById(R.id.bigCover);

        progress = findViewById(R.id.downloadProgress);

        Intent intent = getIntent();
        Object object = intent.getSerializableExtra("beatmapSetInfo");
        if (object != null) {
            info = (IBeatmapSetInfo) object;

            updateInfoBind();

            Bitmap cover = CoverPool.getCoverBitmap(info.getBeatmapSetID());
            if (cover != null) {
                bigCover.setImageBitmap(cover);
            } else {
                bigCover.setImageResource(R.drawable.default_bg);
            }

            loadDetails();

        } else {
            int sid = intent.getIntExtra("beatmapSetID", -1);
            BeatmapSetInfo info = new BeatmapSetInfo();
            info.setBeatmapSetID(sid);
            this.info = info;
            updateInfoBind();
            if (sid != -1) {

                Bitmap cover = CoverPool.getCoverBitmap(info.getBeatmapSetID());
                if (cover != null) {
                    bigCover.setImageBitmap(cover);
                } else {
                    bigCover.setImageResource(R.drawable.default_bg);
                }

                //加载信息
                Util.asynLoadString(
                        "https://api.sayobot.cn/v2/beatmapinfo?0=" + sid,
                        s -> runOnUiThread(() -> {
                            try {
                                JSONObject obj = new JSONObject(s);
                                if (obj.getInt("status") != 0) {
                                    Util.toast(this, "加载失败，status = " + obj.getInt("status"));
                                } else {
                                    obj = obj.getJSONObject("data");

                                    info.setBeatmapSetID(obj.getInt("sid"));
                                    info.setFavCount(obj.getInt("favourite_count"));
                                    info.setRankedState(obj.getInt("approved"));

                                    info.setTitle(obj.getString("title"));
                                    info.setArtist(obj.getString("artist"));
                                    info.setCreator(obj.getString("creator"));

                                    int mode = 0;

                                    JSONArray ja = obj.getJSONArray("bid_data");
                                    for (int i = 0; i < ja.length(); i++) {
                                        mode |= (1 << (ja.getJSONObject(i).getInt("mode")));
                                    }

                                    info.setModes(mode);

                                    updateInfoBind();

                                    loadDetails();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Util.toast(this,"加载失败");
                            }
                        }),
                        throwable -> {
                            throwable.printStackTrace();
                            Util.toast(this, throwable.toString());
                        }
                );
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (info != null) {
            if (DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID()) != null) {
                DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID()).deleteCallback(updateCallback);
            }
        }
    }

    private void updateInfoBind() {
        int modes = info.getModes();
        std.setVisibility(((modes & GameModes.STD) != 0) ? View.VISIBLE : View.GONE);
        taiko.setVisibility(((modes & GameModes.TAIKO) != 0) ? View.VISIBLE : View.GONE);
        ctb.setVisibility(((modes & GameModes.CTB) != 0) ? View.VISIBLE : View.GONE);
        mania.setVisibility(((modes & GameModes.MANIA) != 0) ? View.VISIBLE : View.GONE);

        rankedStateView.setText(RankedState.stateIntToString(info.getRankedState()));
        titleView.setText(info.getTitle());
        detailView.setText(String.format("Artist: %s   Creator: %s", info.getArtist(), info.getCreator()));
        sidView.setText(String.format(Locale.getDefault(), "sid : %d", info.getBeatmapSetID()));
        likeCountView.setText(String.valueOf(info.getFavCount()));

        if (DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID()) == null) {
            progress.setVisibility(View.GONE);
            findViewById(R.id.download).setOnClickListener(v -> {
                if (!loaded) return;
                Downloader.CallbackContainer container = new Downloader.CallbackContainer();
                DownloadHolder.get().initialCallback(info.getBeatmapSetID(), container);
                DownloadCenter.download(BeatmapDetailActivity.this, info, container);
                updateInfoBind();
            });
        } else {
            //正在下载
            findViewById(R.id.download).setOnClickListener(null);
            Downloader.CallbackContainer container = DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID());
            if (container.isErr()) {
                DownloadHolder.get().initialCallback(info.getBeatmapSetID(), null);
                updateInfoBind();
                Util.toast(this,"已清除失败的下载任务");
                return;
            }
            progress.setVisibility(View.VISIBLE);
            progress.setProgress((int) Math.round(container.getProgress() * 1000));

            container.setCallback(updateCallback = new Downloader.Callback() {
                @Override
                public void onProgress(int down, int total) {
                    runOnUiThread(() -> {
                        progress.setProgress((int) Math.round(container.getProgress() * 1000));
                    });
                }

                @Override
                public void onError(Throwable e) {
                    Util.toast(BeatmapDetailActivity.this, "err: " + e);
                    runOnUiThread(BeatmapDetailActivity.this::updateInfoBind);
                }

                @Override
                public void onComplete() {
                    Util.toast(BeatmapDetailActivity.this, info.getArtist() + " - " + info.getTitle() + " 下载完成");
                }
            });

        }


    }

    private void loadDetails() {
        (new Thread(() ->{
            List<BeatmapInfo> list = BeatmapSiteManager.get().getDetailSite().getBeatmapInfo(info);
            if (list == null) {
                Util.toast(this, "加载详细信息失败");
                return;
            }
            runOnUiThread(()->onLoadDetails(list));
        })).start();
    }

    private void onLoadDetails(List<BeatmapInfo> list) {
        if (list.size() > 0) {
            String img = list.get(0).getImgMD5();
            if (!img.equals("00000000000000000000000000000000")) {
                File cache = new File(getCacheDir(), "bigCover/" + img + ".png");
                if (cache.exists()) {
                    bigCover.setImageURI(Uri.fromFile(cache));
                } else {
                    Util.asynCall(() -> {
                        try {
                            URL url = new URL("https://txy1.sayobot.cn/bg/md5/" + img);
                            byte[] bs = Util.readFullByteArray(url.openConnection().getInputStream());
                            Util.checkFile(cache);
                            OutputStream o = new FileOutputStream(cache);
                            o.write(bs);
                            o.close();
                            runOnUiThread(() -> bigCover.setImageURI(Uri.fromFile(cache)));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
        loaded = true;
        updateInfoBind();
    }

    protected void setHalfTransparent() {

        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
