package com.edlplan.beatmapservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.edlplan.beatmapservice.download.DownloadCenter;
import com.edlplan.beatmapservice.download.DownloadHolder;
import com.edlplan.beatmapservice.site.BeatmapFilterInfo;
import com.edlplan.beatmapservice.site.BeatmapListType;
import com.edlplan.beatmapservice.site.BeatmapSiteManager;
import com.edlplan.beatmapservice.site.GameModes;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;
import com.edlplan.beatmapservice.site.RankedState;
import com.edlplan.downloader.Downloader;

import java.util.HashMap;

public class BSMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        UpdateChecker.startCheckUpdate(this);

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
        layoutManager.setOrientation(OrientationHelper.VERTICAL);

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
                    && ranked.isChecked() && qualified.isChecked() && loved.isChecked() && pending.isChecked() && graveyard.isChecked())) {
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

        std = findViewById(R.id.std);
        taiko = findViewById(R.id.taiko);
        ctb = findViewById(R.id.ctb);
        mania = findViewById(R.id.mania);

        ranked = findViewById(R.id.ranked);
        qualified = findViewById(R.id.qualified);
        loved = findViewById(R.id.loved);
        pending = findViewById(R.id.pending);
        graveyard = findViewById(R.id.graveyard);

        loadMore(true);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    RecyclerView recyclerView;

    BeatmapCardAdapter adapter;

    boolean loading = false;

    private SearchView searchView;

    private RadioButton hot,latest;

    private CheckBox std, taiko, ctb, mania;

    private CheckBox ranked, qualified, loved, pending, graveyard;

    public void search() {
        Log.i("beatmap-search", ((SearchView) findViewById(R.id.search_beatmap)).getQuery().toString());
        BeatmapFilterInfo info = createSearchFilterInfo();
        if (info == null) {
            return;
        }

        findViewById(R.id.beatmapFilterLayout).setVisibility(View.GONE);

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

        System.out.println("mode : " + info.getModes());
        System.out.println("rank : " + info.getRankedState());

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

        public ImageButton downloadButton;

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
            final Activity context = (Activity) beatmapCardViewHolder.body.getContext();
            beatmapCardViewHolder.info = info;
            beatmapCardViewHolder.title.setText(info.getTitle());
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
                                        beatmapCardViewHolder.downloadText.setText("100.0%");
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
                    beatmapCardViewHolder.downloadText.setText(String.format("%.1f%%", container.getProgress() * 100));
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
                                beatmapCardViewHolder.downloadText.setText("100%");
                            });
                        }
                    });
                    DownloadHolder.get().initialCallback(sid, container);
                    DownloadCenter.download(BSMainActivity.this, info, container);
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
}
