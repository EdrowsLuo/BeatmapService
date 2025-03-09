package com.edlplan.beatmapservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.platform.android.AudioView;
import com.edlplan.beatmapservice.download.DownloadCenter;
import com.edlplan.beatmapservice.download.DownloadHolder;
import com.edlplan.beatmapservice.site.BeatmapInfo;
import com.edlplan.beatmapservice.site.BeatmapSetInfo;
import com.edlplan.beatmapservice.site.BeatmapSiteManager;
import com.edlplan.beatmapservice.site.GameModes;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;
import com.edlplan.beatmapservice.site.RankedState;
import com.edlplan.beatmapservice.download.Downloader;
import com.edlplan.beatmapservice.ui.ValueBar;
import com.edlplan.beatmapservice.ui.ValueListView;
import com.edlplan.beatmapservice.util.Collector;
import com.edlplan.beatmapservice.util.ListOp;
import com.edlplan.beatmapservice.util.Updatable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BeatmapDetailActivity extends AppCompatActivity {

    public ImageView bigCover;

    public String displayingCover;

    public ImageView std, taiko, ctb, mania;

    public TextView rankedStateView, titleView, artistView, creatorView, dataView, sidView, likeCountView;

    public RecyclerView recyclerView;

    private IBeatmapSetInfo info;

    public ProgressBar progress;

    private boolean loaded = false;

    private Downloader.Callback updateCallback;

    private boolean loadingPreview = false;

    private IAudioEntry preview;

    private List<BeatmapInfo> infos;

    private BeatmapInfo selectedInfo;

    private DifficultyListAdapter adapter;

    private ValueBar starBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beatmap_details);
        setHalfTransparent();
        pickedDir = initPermission(this);

        std = findViewById(R.id.imageViewStd);
        taiko = findViewById(R.id.imageViewTaiko);
        ctb = findViewById(R.id.imageViewCatch);
        mania = findViewById(R.id.imageViewMania);
        rankedStateView = findViewById(R.id.rankStateView);
        titleView = findViewById(R.id.titleTextView);
        artistView = findViewById(R.id.artistText);
        creatorView = findViewById(R.id.creatorText);
        dataView = findViewById(R.id.dataText);
        sidView = findViewById(R.id.sidTextView);
        likeCountView = findViewById(R.id.likeCount);
        bigCover = findViewById(R.id.bigCover);
        recyclerView = findViewById(R.id.difficultyList);

        starBar = findViewById(R.id.starRate);

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
                Util.asyncLoadString(
                        "https://api.sayobot.cn/v2/beatmapinfo?K=" + sid,
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
                                Util.toast(this, "加载失败");
                            }
                        }),
                        throwable -> {
                            throwable.printStackTrace();
                            Util.toast(this, throwable.toString());
                        }
                );
            }
        }

        Util.RunnableWithParam<ImageView> updateMusicButton = v -> {
            if (preview != null) {
                v.setImageResource(preview.isPlaying() ? R.drawable.pause : R.drawable.music);
                AudioView audioView = findViewById(R.id.visualCircle);
                if (audioView.getAudioEntry() != preview) {
                    audioView.setAudioEntry(preview);
                    audioView.setVisibility(View.VISIBLE);
                }
            }
        };

        findViewById(R.id.music).setOnClickListener(v -> {
            if (loaded) {
                if (preview == null) {
                    if (loadingPreview) {
                        Util.toast(this, "预览加载中");
                    } else {
                        loadingPreview = true;
                        Util.toast(this, "开始加载预览");
                        Util.asyncCall(() -> {
                            try {
                                preview = AudioVCore.createAudio(Util.readFullByteArray(
                                        Util.openUrl("https://cdnx.sayobot.cn:25225/preview/" + info.getBeatmapSetID() + ".mp3")));
                                loadingPreview = false;
                                v.post(() -> {
                                    preview.play();
                                    updateMusicButton.run((ImageView) v);
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                                Util.toast(this, "音频加载失败: " + e);
                                loadingPreview = false;
                            }
                        });
                    }
                } else {
                    if (preview.isPlaying()) {
                        preview.pause();
                    } else {
                        preview.play();
                    }
                    updateMusicButton.run((ImageView) v);
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            Log.i("LifeCycle", "onDestroy");
        }
        if (info != null) {
            if (DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID()) != null) {
                DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID()).deleteCallback(updateCallback);
            }
        }
        if (preview != null) {
            preview.release();
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
        artistView.setText("Artist: " + info.getArtist());
        creatorView.setText("Creator: " + info.getCreator());
        sidView.setText(String.format(Locale.getDefault(), "sid : %d", info.getBeatmapSetID()));
        likeCountView.setText(String.valueOf(info.getFavCount()));

        if (infos != null) {
            Collector<BeatmapInfo, Double> length = Collector.avg(BeatmapInfo::getLength);
            Collector<BeatmapInfo, Double> starMax = Collector.max(0, BeatmapInfo::getStar);
            Collector<BeatmapInfo, Double> starMin = Collector.min(Double.MAX_VALUE, BeatmapInfo::getStar);
            Collector<BeatmapInfo, Double> bpmMax = Collector.max(0, BeatmapInfo::getBpm);
            Collector<BeatmapInfo, Double> bpmMin = Collector.min(Double.MAX_VALUE, BeatmapInfo::getBpm);
            Updatable<BeatmapInfo> update = Collector.bind(length, starMax, starMin, bpmMax, bpmMin);
            ListOp.copyOf(infos).forEach(update::update);
            StringBuilder sb = new StringBuilder();
            sb.append("Length: " + Util.timeToString(Util.round(length.getValue())));
            sb.append("  Star: ");
            if (starMax.getValue() - starMin.getValue() < 0.01) {
                sb.append(String.format(Locale.getDefault(), "%.1f", starMax.getValue()));
            } else {
                sb.append(String.format(Locale.getDefault(), "%.1f~%.1f", starMin.getValue(), starMax.getValue()));
            }
            sb.append("  Bpm: ");
            if (bpmMax.getValue() - bpmMin.getValue() < 0.01) {
                sb.append(String.format(Locale.getDefault(), "%.1f", bpmMax.getValue()));
            } else {
                sb.append(String.format(Locale.getDefault(), "%.1f~%.1f", bpmMin.getValue(), bpmMax.getValue()));
            }
            dataView.setText(sb.toString());

            if (adapter == null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(layoutManager);
                layoutManager.setOrientation(RecyclerView.HORIZONTAL);
                recyclerView.setAdapter(adapter = new DifficultyListAdapter());
            }

            findViewById(R.id.shareButton).setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://osu.ppy.sh/beatmapsets/" + info.getBeatmapSetID())));
            });
        }

        if (DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID()) == null) {
            progress.setVisibility(View.GONE);
            findViewById(R.id.download).setOnClickListener(v -> {
                if (!loaded) return;
                Downloader.CallbackContainer container = new Downloader.CallbackContainer();
                DownloadCenter.download(BeatmapDetailActivity.this, info, container,pickedDir );
                updateInfoBind();
            });
        } else {
            //正在下载
            findViewById(R.id.download).setOnClickListener(null);
            Downloader.CallbackContainer container = DownloadHolder.get().getCallbackContainer(info.getBeatmapSetID());
            if (container.isErr()) {
                DownloadHolder.get().initialCallback(info.getBeatmapSetID(), null);
                updateInfoBind();
                Util.toast(this, "已清除失败的下载任务");
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
        (new Thread(() -> {
            List<BeatmapInfo> list = BeatmapSiteManager.get().getDetailSite().getBeatmapInfo(info);
            if (list == null) {
                Util.toast(this, "加载详细信息失败");
                return;
            }
            runOnUiThread(() -> onLoadDetails(list));
        })).start();
    }

    public void changeCover(BeatmapInfo info) {
        if (info == null) {
            return;
        }
        String s = info.getBackgroundUrl();
        if (s == null) {
            return;
        }
        String smd5 = Util.md5(s);
        if (BuildConfig.DEBUG) {
            Log.i("Load", String.format("%s %s", s, smd5));
        }
        if (!s.equals(displayingCover)) {
            displayingCover = s;
            File cache = new File(getCacheDir(), "bigCover/" + smd5 + ".png");
            if (cache.exists()) {
                bigCover.setImageURI(Uri.fromFile(cache));
                bigCover.setOnLongClickListener(v -> {
                    MyDialog.showForTask(this, "保存图片", "将保存在" + Util.getCoverOutputDir().getAbsolutePath(), dialog -> {
                        try {
                            if (Util.fileCopyTo(cache, Util.getCoverOutputDir())) {
                                Util.toast(this, "图片保存成功");
                            } else {
                                Util.toast(this, "图片保存失败");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Util.toast(this, "图片保存失败:" + e);
                        }
                        dialog.dismiss();
                    });
                    return true;
                });
            } else {
                //加载图片
                String taskKey = "loadBigCover::" + s;
                if (!Util.isTaskRunning(taskKey)) {
                    Util.asyncCall(taskKey, () -> {
                        try {
                            byte[] bs = Util.readFullByteArrayWithRetry(s, 5, 100);
                            Util.checkFile(cache);
                            OutputStream o = new FileOutputStream(cache);
                            o.write(bs);
                            o.close();

                            bigCover.post(() -> {
                                if (!s.equals(displayingCover)) {
                                    return;
                                }
                                bigCover.setImageURI(Uri.fromFile(cache));
                                bigCover.setOnLongClickListener(v -> {
                                    MyDialog.showForTask(this, "保存图片", "将保存在" + Util.getCoverOutputDir().getAbsolutePath(), dialog -> {
                                        try {
                                            if (Util.fileCopyTo(cache, Util.getCoverOutputDir())) {
                                                Util.toast(this, "图片保存成功");
                                            } else {
                                                Util.toast(this, "图片保存失败");
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Util.toast(this, "图片保存失败:" + e);
                                        }
                                        dialog.dismiss();
                                    });
                                    return true;
                                });
                            });
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }

            }
        }
    }

    private void onLoadDetails(List<BeatmapInfo> list) {
        if (list.size() > 0) {
            infos = list;
            Collections.sort(infos, (a, b) -> -Double.compare(a.getStar(), b.getStar()));
            BeatmapInfo i = null;
            for (BeatmapInfo info : infos) {
                if (info.getBackgroundUrl() != null) {
                    i = info;
                    break;
                }
            }
            changeCover(i);
        }
        loaded = true;
        updateInfoBind();
        if (list.size() > 0) {
            changeSelected(infos.get(0));
        }
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

    private void changeSelected(BeatmapInfo info) {
        if (selectedInfo == info) {
            return;
        }
        selectedInfo = info;


        setValue(findViewById(R.id.arBar), findViewById(R.id.arText), info.getApproachRate());
        setValue(findViewById(R.id.odBar), findViewById(R.id.odText), info.getOverallDifficulty());
        setValue(findViewById(R.id.hpBar), findViewById(R.id.hpText), info.getHP());
        setValue(findViewById(R.id.starRate), findViewById(R.id.starText), info.getStar());
        setValue(findViewById(R.id.aimStarRate), findViewById(R.id.aimStarText), info.getAim());
        setValue(findViewById(R.id.speedStarRate), findViewById(R.id.speedStarText), info.getSpeed());

        TextView csLabel = findViewById(R.id.csLabel);
        if (info.getMode() == GameModes.Single.MANIA || info.getMode() == GameModes.Single.TAIKO) {
            csLabel.setText("KEY");
            setIntValue(findViewById(R.id.csBar), findViewById(R.id.csText), info.getCircleSize());
        } else {
            csLabel.setText("CS");
            setValue(findViewById(R.id.csBar), findViewById(R.id.csText), info.getCircleSize());
        }

        StringBuilder details = new StringBuilder();
        if (info.getMode() == GameModes.Single.STD) {
            details.append("MaxCombo: ").append(info.getMaxCombo()).append('\n');
            details.append("PP: ").append(String.format(Locale.getDefault(), "%.2f", info.getPP())).append('\n');
            details.append("BPM: ").append(String.format(Locale.getDefault(), "%.2f", info.getBpm())).append('\n');
            details.append("Objects: ").append(info.getCircleCount()).append('/').append(info.getSliderCount()).append('/').append(info.getSpinnerCount());
            findViewById(R.id.strainLayout).setVisibility(View.VISIBLE);
            ((ValueListView) findViewById(R.id.aimList)).setValue(
                    ListOp.copyOf(info.getStrainAim()).reflect(integer -> integer + 1).asIntArray());
            ((ValueListView) findViewById(R.id.speedList)).setValue(
                    ListOp.copyOf(info.getStrainSpeed()).reflect(integer -> integer + 1).asIntArray());
        } else {
            details.append("BPM: ").append(String.format(Locale.getDefault(), "%.2f", info.getBpm())).append('\n');
            findViewById(R.id.strainLayout).setVisibility(View.INVISIBLE);
        }


        ((TextView) findViewById(R.id.detailText)).setText(details.toString());
        changeCover(selectedInfo);
    }

    private void setValue(ValueBar valueBar, TextView textView, double v) {
        valueBar.setValue((int) (v * 10));
        if (v > 10) {
            textView.setText(">10");
        } else {
            textView.setText(String.format(Locale.getDefault(), "%.1f", v));
        }
    }

    private void setIntValue(ValueBar valueBar, TextView textView, double v) {
        valueBar.setValue((int) (v * 10));
        if (v > 10) {
            textView.setText(">10");
        } else {
            textView.setText(String.format(Locale.getDefault(), "%d", Math.round(v)));
        }
    }

    private class DifficultyViewHolder extends RecyclerView.ViewHolder {

        private TextView diffName;
        private TextView star;
        private ImageView modeIcon;
        private View body, mainBody;
        private BeatmapInfo bindInfo;

        public DifficultyViewHolder(@NonNull View itemView) {
            super(itemView);
            diffName = itemView.findViewById(R.id.difName);
            star = itemView.findViewById(R.id.star);
            modeIcon = itemView.findViewById(R.id.modeIcon);
            body = itemView.findViewById(R.id.diffBody);
            mainBody = itemView;
        }

        public void bind(BeatmapInfo info) {
            bindInfo = info;
            if (info == selectedInfo) {
                body.setBackgroundResource(R.drawable.rounded_rect_dark);
                //mainBody.setBackgroundResource(R.drawable.rounded_rect);
            } else {
                body.setBackgroundResource(R.drawable.rounded_rect);
                //mainBody.setBackgroundColor(Color.argb(0, 0, 0, 0));
            }
            diffName.setText(info.getVersion());
            int starCount = Math.min((int) info.getStar(), 10);
            boolean halfStar = starCount < 10 && info.getStar() % 1 >= 0.5;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < starCount; i++) {
                stringBuilder.append("★");
            }
            if (halfStar) {
                stringBuilder.append("☆");
            }
            star.setText(String.format(Locale.getDefault(), "%s %.2f", stringBuilder.toString(), info.getStar()));

            int icon;
            switch (info.getMode()) {
                case GameModes.Single.STD:
                    icon = R.drawable.mode_std;
                    break;
                case GameModes.Single.TAIKO:
                    icon = R.drawable.mode_taiko;
                    break;
                case GameModes.Single.CTB:
                    icon = R.drawable.mode_catch;
                    break;
                case GameModes.Single.MANIA:
                    icon = R.drawable.mode_mania;
                    break;
                default:
                    icon = R.drawable.mode_std;
                    break;
            }

            modeIcon.setImageResource(icon);

            body.setOnClickListener(v -> {
                if (bindInfo != null && bindInfo != selectedInfo) {
                    changeSelected(bindInfo);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class DifficultyListAdapter extends RecyclerView.Adapter<DifficultyViewHolder> {
        @NonNull
        @Override
        public DifficultyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new DifficultyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diffculty, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DifficultyViewHolder difficultyViewHolder, int i) {
            difficultyViewHolder.bind(infos.get(i));
        }

        @Override
        public int getItemCount() {
            return infos.size();
        }
    }

    DocumentFile pickedDir;

    public DocumentFile initPermission(Activity activity) {
        String path = PreferenceManager.getDefaultSharedPreferences(activity).getString("default_download_path", "default");
        if (path.startsWith(Environment.getExternalStorageDirectory().toString())
                || path.equals("default")
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        ) {
        } else {
            SharedPreferences sp = activity.getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
            String uriTree = sp.getString("uriTree", "");
            if (TextUtils.isEmpty(uriTree)) {
                Util.toast(this, "请点击右下角的\"选择\"");
                activity.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 3);
                // 重新授权
            } else {
                try {
                    Uri uri = Uri.parse(uriTree);
                    final int takeFlags = activity.getIntent().getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    return DocumentFile.fromTreeUri(activity, uri);
                } catch (SecurityException e) {
                    activity.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 3);
                }
            }
        }
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode != RESULT_OK)
            return;
        else {
            // 获取权限
            Uri treeUri = resultData.getData();

            final int takeFlags = resultData.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            }
            // 保存获取的目录权限
            SharedPreferences sp = getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("uriTree", treeUri.toString());
            editor.apply();
            pickedDir = DocumentFile.fromTreeUri(this, treeUri);
        }
    }
}
