package com.edlplan.beatmapservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.platform.android.AndroidPlugin;
import com.edlplan.audiov.platform.android.AudioView;
import com.edlplan.audiov.platform.bass.BassPlugin;
import com.edlplan.beatmapservice.download.DownloadCenter;
import com.edlplan.beatmapservice.download.DownloadHolder;
import com.edlplan.beatmapservice.download.Downloader;
import com.edlplan.beatmapservice.site.BeatmapFilterInfo;
import com.edlplan.beatmapservice.site.BeatmapListType;
import com.edlplan.beatmapservice.site.BeatmapSiteManager;
import com.edlplan.beatmapservice.site.GameModes;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;
import com.edlplan.beatmapservice.site.RankedState;
import com.edlplan.beatmapservice.site.sayo.SayoServerSelector;
import com.edlplan.beatmapservice.CacheManager;
import com.edlplan.framework.utils.functionality.SmartIterator;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class BSMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickedDir= initPermission(this);
        cacheManager.loadCache(this);

        //com.tencent.bugly.proguard.an.c = BuildConfig.DEBUG;
        Beta.autoCheckUpgrade = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("auto_update", true);
        Beta.initDelay = 1000;
        Bugly.init(getApplicationContext(), BuildConfig.DEBUG ? "8fc13af6cd" : "e6e37ac737", false);

        AndroidPlugin.initial(this);
        AudioVCore.initial(AndroidPlugin.INSTANCE, BassPlugin.INSTANCE);

        setContentView(R.layout.activity_bsmain);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        BeatmapSiteManager.get().getInfoSite().reset();

        recyclerView = findViewById(R.id.mainRecycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setAdapter(adapter = new BeatmapCardAdapter());

        findViewById(R.id.beatmapFilterButton).setOnClickListener(v -> {
            if (findViewById(R.id.beatmapFilterLayout).getVisibility() == View.GONE) {
                findViewById(R.id.beatmapFilterLayout).setVisibility(View.VISIBLE);
            } else {

                findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);
            }
        });

        (searchView = findViewById(R.id.search_beatmap)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    int sid = Integer.parseInt(query);

                    Intent intent = new Intent(BSMainActivity.this, BeatmapDetailActivity.class);
                    intent.putExtra("beatmapSetID", sid);
                    startActivity(intent);

                } catch (NumberFormatException e) {

                }
                search();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        searchView.setOnSearchClickListener(v -> {
            System.out.println("on search clicked");
        });

        searchView.setOnCloseListener(() -> {
            System.out.println("on search closed");
            return false;
        });

        (hot = findViewById(R.id.hot)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                latest.setChecked(false);
                BeatmapFilterInfo filterInfo = new BeatmapFilterInfo();
                filterInfo.setBeatmapListType(BeatmapListType.HOT);
                BeatmapSiteManager.get().getInfoSite().applyFilterInfo(filterInfo);
                adapter.notifyDataSetChanged();
                findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);
                loadMore(true);
                Toast.makeText(this, "切换到热门铺面", Toast.LENGTH_SHORT).show();
            }
        });

        (latest = findViewById(R.id.latest)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                hot.setChecked(false);
                BeatmapFilterInfo filterInfo = new BeatmapFilterInfo();
                filterInfo.setBeatmapListType(BeatmapListType.LATEST);
                BeatmapSiteManager.get().getInfoSite().applyFilterInfo(filterInfo);
                adapter.notifyDataSetChanged();
                findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);
                loadMore(true);
                Toast.makeText(this, "切换到最新铺面", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.refresh).setOnClickListener(v -> {
            if (!(std.isChecked() && taiko.isChecked() && ctb.isChecked() && mania.isChecked()
                    && ranked.isChecked() && qualified.isChecked() && loved.isChecked() && pending.isChecked()
                    && graveyard.isChecked() && !enableValueLimit.isChecked())) {
                search();
                return;
            }
            if (latest.isChecked()) {
                BeatmapFilterInfo filterInfo = new BeatmapFilterInfo();
                filterInfo.setBeatmapListType(BeatmapListType.LATEST);
                BeatmapSiteManager.get().getInfoSite().applyFilterInfo(filterInfo);
                adapter.notifyDataSetChanged();
                findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);
                loadMore(true);
                return;
            }
            if (hot.isChecked()) {
                BeatmapFilterInfo filterInfo = new BeatmapFilterInfo();
                filterInfo.setBeatmapListType(BeatmapListType.HOT);
                BeatmapSiteManager.get().getInfoSite().applyFilterInfo(filterInfo);
                adapter.notifyDataSetChanged();
                findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);
                loadMore(true);
                return;
            }
            search();
        });

        limitText = findViewById(R.id.valueLimitText);
        (enableValueLimit = findViewById(R.id.enableValueLimit)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                limitText.setVisibility(View.VISIBLE);
                limitText.setText(new BeatmapFilterInfo.ValueLimit().toSayoString());
            } else {
                limitText.setVisibility(View.GONE);
            }
        });

        std = findViewById(R.id.std);
        taiko = findViewById(R.id.taiko);
        ctb = findViewById(R.id.ctb);
        mania = findViewById(R.id.mania);

        ranked = findViewById(R.id.ranked);
        qualified = findViewById(R.id.qualified);
        loved = findViewById(R.id.loved);
        pending = findViewById(R.id.pending);
        graveyard = findViewById(R.id.graveyard);

        selectedServer = findViewById(R.id.downloadServerSpinner);

        SayoServerSelector.getInstance().asyncInitial(done -> {
            runOnUiThread(() -> {
                if (!done) {
                    Util.toast(this, "获取服务器列表失败，使用默认路线");
                }
                selectedServer.setAdapter(new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        SmartIterator
                                .wrap(SayoServerSelector.getInstance()
                                        .getServerInfoList()
                                        .iterator())
                                .applyFunction(info -> info.server_nameU)
                                .collectAllAsList()
                ));
                selectedServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SayoServerSelector.getInstance().switchInfo(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        SayoServerSelector.getInstance().switchInfo(0);
                        parent.setSelection(0);
                    }
                });
            });
        });



        loadMore(true);


        // 现在 Sayobot cdn 直接提供国外支持了
        /*if (!Locale.getDefault().getCountry().equals("CN")) {
            if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ignoreRecommendServerSelect", false)) {
                MyDialog dialog = new MyDialog(this);
                dialog.setTitle("Server");
                dialog.setDescription("We recommend you to change to new server Saybot(HK)!");
                dialog.setOnCancelListener(dialog1 -> {
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("ignoreRecommendServerSelect", true).apply();
                    dialog1.dismiss();
                });
                dialog.setOnSure(dialog1 -> {
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putBoolean("ignoreRecommendServerSelect", true)
                            .putString("beatmap_origin", "1")
                            .apply();
                    Util.toast(this,"Switch to Saybot(HK)");
                    dialog1.dismiss();
                });
                dialog.show();
            }
        }*/

        //List<BeatmapInfo> cache = DroidShared.loadDroidLibrary(DownloadCenter.getDroidSongsDirectory(this));
        //System.out.println(cache.size());
    }


    public void previewAudio(IAudioEntry preview) {
        AudioView audioView = findViewById(R.id.visualCircle);
        if (audioView.getAudioEntry() != preview) {
            if (audioView.getAudioEntry() != null) {
                audioView.getAudioEntry().stop();
                audioView.getAudioEntry().release();
            }
            audioView.setAudioEntry(preview);
            audioView.setVisibility(View.VISIBLE);
            preview.play();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bsmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_check_update) {
            Beta.checkUpgrade(true, false);
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(this, SupportActivity.class));
        } else if (id == R.id.nav_ops_dgsrz) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://ops.dgsrz.com")));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        Util.debug("onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Util.debug("onResume");
        super.onResume();
        checkPermissions();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    RecyclerView recyclerView;

    BeatmapCardAdapter adapter;

    boolean loading = false;

    private SearchView searchView;

    private Spinner selectedServer;

    private RadioButton hot,latest;

    private CheckBox std, taiko, ctb, mania;

    private CheckBox ranked, qualified, loved, pending, graveyard;

    private CheckBox enableValueLimit;

    private TextView limitText;

    public void search() {
        Log.i("beatmap-search", ((SearchView) findViewById(R.id.search_beatmap)).getQuery().toString());
        BeatmapFilterInfo info = createSearchFilterInfo();
        if (info == null) {
            return;
        }



        findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);
        searchView.clearFocus();

        hot.setChecked(false);
        latest.setChecked(false);

        BeatmapSiteManager.get().getInfoSite().applyFilterInfo(info);
        adapter.notifyDataSetChanged();
        loadMore(true);

    }

    public BeatmapFilterInfo createSearchFilterInfo() {
        BeatmapFilterInfo info = new BeatmapFilterInfo();
        info.setBeatmapListType(BeatmapListType.SEARCH);
        info.setKeyWords(searchView.getQuery().toString());

        info.setModes(
                (std.isChecked() ? GameModes.STD : 0) | (taiko.isChecked() ? GameModes.TAIKO : 0)
                        | (ctb.isChecked() ? GameModes.CTB : 0) | (mania.isChecked() ? GameModes.MANIA : 0)
        );
        if (info.getModes() == 0) {
            Toast.makeText(this, "至少要选择一个模式", Toast.LENGTH_SHORT).show();
            return null;
        }

        info.setRankedState(
                (ranked.isChecked() ? RankedState.BinaryCode.RANKED : 0) | (qualified.isChecked() ? RankedState.BinaryCode.QUALIFIED : 0)
                        | (loved.isChecked() ? RankedState.BinaryCode.LOVED : 0) | (pending.isChecked() ? RankedState.BinaryCode.PENDING : 0)
                        | (graveyard.isChecked() ? RankedState.BinaryCode.GRAVEYARD : 0)
        );
        if (info.getRankedState() == 0) {
            Toast.makeText(this, "至少要选择一个铺面状态", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (enableValueLimit.isChecked()) {
            BeatmapFilterInfo.ValueLimit limit = new BeatmapFilterInfo.ValueLimit();
            if (BeatmapFilterInfo.ValueLimit.parseInto(limitText.getText().toString(), limit) != 0) {
                MyDialog.showForTask(
                        this,
                        "错误的过滤条件",
                        "不能随便删减，请只改变数字（而且暂时不支持小数，请使用整数）",
                        Dialog::dismiss
                );
                return null;
            }
            info.setValueLimit(limit);
        }

        return info;
    }

    public void loadMore() {
        loadMore(false);
    }

    public void loadMore(boolean force) {
        if (loading & !force) {
            Toast.makeText(this, "加载中", Toast.LENGTH_SHORT).show();
            return;
        }
        loading = true;
        if (BeatmapSiteManager.get().getInfoSite().hasMoreBeatmapSet()) {
            (new Thread(() -> {
                BeatmapSiteManager.get().getInfoSite().tryToLoadMoreBeatmapSet();
                loading = false;
                runOnUiThread(adapter::notifyDataSetChanged);
            })).start();
        } else {
            Toast.makeText(this, "没有更多的铺面了", Toast.LENGTH_SHORT).show();
            loading = false;
        }
    }

    public class BeatmapCardViewHolder extends RecyclerView.ViewHolder {

        public Downloader.Callback updateCallback;

        public IBeatmapSetInfo info;

        public ImageView imageView;

        public TextView title, beatmapInfo;

        public CardView body;

        public ImageView std, taiko, ctb, mania;

        public ImageButton downloadButton, previewButton;

        public TextView downloadText;

        public ProgressBar downloadProgress;

        public TextView rankedStateView;

        public BeatmapCardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.title);
            beatmapInfo = itemView.findViewById(R.id.beatmapInfo);
            body = itemView.findViewById(R.id.body);
            std = itemView.findViewById(R.id.imageViewStd);
            taiko = itemView.findViewById(R.id.imageViewTaiko);
            ctb = itemView.findViewById(R.id.imageViewCatch);
            mania = itemView.findViewById(R.id.imageViewMania);
            downloadButton = itemView.findViewById(R.id.downloadButton);
            downloadText = itemView.findViewById(R.id.progressText);
            downloadProgress = itemView.findViewById(R.id.progressBar);
            rankedStateView = itemView.findViewById(R.id.rankStateView);
            previewButton = itemView.findViewById(R.id.musicPreview);
        }

    }


    public class BeatmapCardAdapter extends RecyclerView.Adapter<BeatmapCardViewHolder> {

        @NonNull
        @Override
        public BeatmapCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new BeatmapCardViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_beatmap_general, viewGroup, false));
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull BeatmapCardViewHolder beatmapCardViewHolder, int i) {

            if (beatmapCardViewHolder.info != null) {
                if (DownloadHolder.get().getCallbackContainer(beatmapCardViewHolder.info.getBeatmapSetID()) != null) {
                    DownloadHolder.get().getCallbackContainer(beatmapCardViewHolder.info.getBeatmapSetID()).deleteCallback(beatmapCardViewHolder.updateCallback);
                }
            }

            IBeatmapSetInfo info = BeatmapSiteManager.get().getInfoSite().getInfoAt(i);
            final int sid = info.getBeatmapSetID();
            String downloadedState = "";

            if (cacheManager.downloadedSongs.containsKey(String.valueOf(sid))) {
                int ts = cacheManager.downloadedSongs.get(String.valueOf(sid));
                if (ts < info.getLastUpdate()) {
                    downloadedState = "(U)";
                } else {
                    downloadedState = "(D)";
                }
            }
            final Activity context = (Activity) beatmapCardViewHolder.body.getContext();
            beatmapCardViewHolder.info = info;
            beatmapCardViewHolder.title.setText(downloadedState + info.getTitle());
            beatmapCardViewHolder.beatmapInfo.setText(String.format("Artist: %s\nCreator: %s", info.getArtist(), info.getCreator()));
            beatmapCardViewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            beatmapCardViewHolder.imageView.setImageAlpha(200);
            beatmapCardViewHolder.imageView.setImageResource(R.drawable.cover);
            int modes = info.getModes();
            beatmapCardViewHolder.std.setVisibility(((modes & GameModes.STD) != 0) ? View.VISIBLE : View.GONE);
            beatmapCardViewHolder.taiko.setVisibility(((modes & GameModes.TAIKO) != 0) ? View.VISIBLE : View.GONE);
            beatmapCardViewHolder.ctb.setVisibility(((modes & GameModes.CTB) != 0) ? View.VISIBLE : View.GONE);
            beatmapCardViewHolder.mania.setVisibility(((modes & GameModes.MANIA) != 0) ? View.VISIBLE : View.GONE);
            beatmapCardViewHolder.rankedStateView.setText(RankedState.stateIntToString(info.getRankedState()));

            Bitmap cover = CoverPool.getCoverBitmap(sid);
            if (cover == null) {
                beatmapCardViewHolder.imageView.setImageAlpha(0);
                CoverPool.loadCoverBitmap(
                        beatmapCardViewHolder.beatmapInfo.getContext(),
                        sid,
                        () -> beatmapCardViewHolder.imageView.post(() -> {
                            if (beatmapCardViewHolder.info.getBeatmapSetID() == sid) {
                                beatmapCardViewHolder.imageView.setImageAlpha(200);
                                beatmapCardViewHolder.imageView.setImageBitmap(CoverPool.getCoverBitmap(sid));
                            }
                        }));
            } else {
                beatmapCardViewHolder.imageView.setImageBitmap(cover);
            }

            beatmapCardViewHolder.body.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), BeatmapDetailActivity.class);
                intent.putExtra("beatmapSetInfo", info);
                v.getContext().startActivity(intent);
            });

            beatmapCardViewHolder.previewButton.setOnClickListener(v -> {
                AudioView audioView = findViewById(R.id.visualCircle);
                if (audioView.getAudioEntry() != null) {
                    audioView.getAudioEntry().pause();
                }
                Util.asyncCall(() -> {
                    try {
                        IAudioEntry preview = AudioVCore.createAudio(Util.readFullByteArray(
                                Util.openUrl("https://cdnx.sayobot.cn:25225/preview/" + info.getBeatmapSetID() + ".mp3")));
                        runOnUiThread(() -> {
                            previewAudio(preview);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });

            beatmapCardViewHolder.body.setOnLongClickListener(v -> {
                AudioView audioView = findViewById(R.id.visualCircle);
                if (audioView.getAudioEntry() != null) {
                    audioView.getAudioEntry().pause();
                }
                Util.toast(BSMainActivity.this, "完整音频加载中...");
                Util.asyncCall(() -> {
                    try {
                        IAudioEntry preview = AudioVCore.createAudio(Util.readFullByteArrayWithRetry(
                                BeatmapSiteManager.get().getDetailSite()
                                        .getBeatmapInfoV2(info)
                                        .getBidData()
                                        .get(0)
                                        .getAudioUrl(), 5, 100));
                        runOnUiThread(() -> {
                            previewAudio(preview);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return true;
            });

            if (DownloadHolder.get().getCallbackContainer(sid) != null) {
                //正在下载
                Downloader.CallbackContainer container = DownloadHolder.get().getCallbackContainer(sid);
                beatmapCardViewHolder.downloadProgress.setVisibility(View.VISIBLE);
                beatmapCardViewHolder.downloadProgress.setProgress(container.isCompleted() ? 1000 : (int) (1000 * container.getProgress()));
                beatmapCardViewHolder.downloadText.setVisibility(View.VISIBLE);
                beatmapCardViewHolder.downloadText.setText(String.format("%.1f%%", container.isCompleted() ? 100 : container.getProgress() * 100));

                beatmapCardViewHolder.downloadButton.setOnClickListener(null);

                if (container.isErr()) {
                    beatmapCardViewHolder.downloadText.setText("错误！");
                    beatmapCardViewHolder.downloadButton.setOnClickListener(v -> {
                        DownloadHolder.get().initialCallback(sid, null);
                        notifyDataSetChanged();
                    });
                } else {
                    container.setCallback(
                            beatmapCardViewHolder.updateCallback = new Downloader.Callback() {

                                @Override
                                public void onProgress(int down, int total) {
                                    if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                        return;
                                    }
                                    beatmapCardViewHolder.downloadProgress.post(() -> {
                                        if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                            return;
                                        }
                                        beatmapCardViewHolder.downloadProgress.setProgress((int) (1000 * container.getProgress()));
                                        beatmapCardViewHolder.downloadText.setText(String.format("%.1f%%", container.getProgress() * 100));
                                    });
                                }

                                @Override
                                public void onError(Throwable e) {
                                    context.runOnUiThread(() ->{
                                        Toast.makeText(context, "err: " + e, Toast.LENGTH_LONG).show();
                                        notifyDataSetChanged();
                                    });
                                }

                                @Override
                                public void onComplete() {
                                    context.runOnUiThread(() -> Toast.makeText(context, info.getArtist() + " - " + info.getTitle() + "下载完成", Toast.LENGTH_LONG).show());

                                    if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                        return;
                                    }
                                    beatmapCardViewHolder.downloadProgress.post(() -> {
                                        if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                            return;
                                        }
                                        beatmapCardViewHolder.downloadProgress.setProgress(1000);
                                        beatmapCardViewHolder.downloadText.setText("完成！");
                                    });
                                }
                            }
                    );
                }

            } else {
                //没有在下载
                beatmapCardViewHolder.downloadProgress.setVisibility(View.GONE);
                beatmapCardViewHolder.downloadText.setVisibility(View.GONE);

                beatmapCardViewHolder.downloadButton.setOnClickListener(v -> {
                    Downloader.CallbackContainer container = new Downloader.CallbackContainer();
                    beatmapCardViewHolder.downloadProgress.setVisibility(View.VISIBLE);
                    beatmapCardViewHolder.downloadProgress.setProgress((int) (1000 * container.getProgress()));
                    beatmapCardViewHolder.downloadText.setVisibility(View.VISIBLE);
                    beatmapCardViewHolder.downloadText.setText(container.isCompleted() ? "完成！" : String.format("%.1f%%", container.getProgress() * 100));
                    container.setCallback(beatmapCardViewHolder.updateCallback = new Downloader.Callback() {
                        @Override
                        public void onProgress(int down, int total) {
                            if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                return;
                            }
                            beatmapCardViewHolder.downloadProgress.post(() -> {
                                if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                    return;
                                }
                                beatmapCardViewHolder.downloadProgress.setProgress((int) (1000 * container.getProgress()));
                                beatmapCardViewHolder.downloadText.setText(String.format("%.1f%%", container.getProgress() * 100));
                            });
                        }

                        @Override
                        public void onError(Throwable e) {
                            context.runOnUiThread(() -> {
                                Toast.makeText(context, "err: " + e, Toast.LENGTH_LONG).show();
                                notifyDataSetChanged();
                            });
                        }

                        @Override
                        public void onComplete() {
                            context.runOnUiThread(() -> Toast.makeText(context, info.getArtist() + " - " + info.getTitle() + "下载完成", Toast.LENGTH_LONG).show());

                            if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                return;
                            }
                            beatmapCardViewHolder.downloadProgress.post(() -> {
                                if (beatmapCardViewHolder.info.getBeatmapSetID() != sid) {
                                    return;
                                }
                                beatmapCardViewHolder.downloadProgress.setProgress(1000);
                                beatmapCardViewHolder.downloadText.setText("完成！");
                            });
                        }
                    });

                    DownloadCenter.download(BSMainActivity.this, info, container,pickedDir);
                    v.setOnClickListener(null);
                });
            }

            if (i == getItemCount() - 1) {
                loadMore();
            }
        }

        @Override
        public int getItemCount() {
            return BeatmapSiteManager.get().getInfoSite().getLoadedBeatmapSetCount();
        }
    }


    private boolean checkPermissions() {
        if (PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2333);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (checkPermissions()) {
            Toast.makeText(this, R.string.storage_permission_granted, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED
                && EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
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
    CacheManager cacheManager = CacheManager.get();

}
