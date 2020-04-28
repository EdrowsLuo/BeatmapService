package com.edlplan.beatmapservice;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.edlplan.beatmapservice.ui.TriangleDrawable;
import com.edlplan.beatmapservice.ui.TriangleEffectView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (BuildConfig.DEBUG) {
            findViewById(R.id.save_pic).setOnClickListener(v -> {
                Bitmap bmp = ((TriangleEffectView) findViewById(R.id.triangle)).getTriangleDrawable().drawPreview();
                File file = new File(Util.getCoverOutputDir(), String.format("pic_%d.png", System.currentTimeMillis()));
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                        Util.toast(this, String.format("输出到 %s", file.getAbsolutePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
