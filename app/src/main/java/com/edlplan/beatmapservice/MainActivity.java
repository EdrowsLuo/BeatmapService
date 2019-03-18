package com.edlplan.beatmapservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.edlplan.downloader.Downloader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    static Downloader sdownloader;

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
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        if (intent.getData() != null) {
            String id = intent.getData().getQueryParameter("id");
            if (id != null) {
                download(id);
            }
        }

        findViewById(R.id.button).setOnClickListener(v -> {
            download(((EditText) findViewById(R.id.editText)).getText().toString());
        });

        findViewById(R.id.beatmapBrowser).setOnClickListener(v -> {
            Intent i = new Intent(this, BeatmapBrowserActivity.class);
            startActivity(i);
        });
    }



    protected void download(String id) {
        if (sdownloader != null) {
            Toast.makeText(MainActivity.this, "上一个未下载完成", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(()->{
            Downloader downloader = new Downloader();
            sdownloader = downloader;
            downloader.setTargetDirectory(new File(Environment.getExternalStorageDirectory(), "osu!droid/Songs"));
            try {
                URL startURL;
                if (((RadioGroup) findViewById(R.id.downloadSite)).getCheckedRadioButtonId() == R.id.radioButtonSayo) {
                    startURL = new URL("https://txy1.sayobot.cn/download/osz/" + URLEncoder.encode("" + Integer.parseInt(id), "UTF-8"));
                } else {
                    startURL = new URL("https://bloodcat.com/osu/_data/beatmaps/" + Integer.parseInt(id) + ".osz");
                }


                /*HttpURLConnection connection = (HttpURLConnection) startURL.openConnection();
                if (connection.getResponseCode() != 302) {
                    System.out.println("code::" + connection.getResponseCode());
                    System.out.println(startURL);
                    System.out.println(connection.getHeaderFields());
                }*/
                downloader.setUrl(startURL);
                downloader.setCallback(new Downloader.Callback() {
                    @Override
                    public void onProgress(int down, int total) {
                        setMsg(
                                String.format("%dkb/%dkb %.2f", down / 1024, total / 1024, (down / (float) total) * 100)
                        );
                    }

                    @Override
                    public void onError(Throwable e) {
                        sdownloader = null;
                        setMsg(
                                e.getMessage()
                        );
                    }

                    @Override
                    public void onComplete() {
                        sdownloader = null;
                        setMsg(
                                "complete"
                        );
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show());
                    }
                });
                downloader.start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setMsg(final String s) {
        runOnUiThread(()->{
            ((TextView) findViewById(R.id.text)).setText(
                    s
            );
        });
    }
}
