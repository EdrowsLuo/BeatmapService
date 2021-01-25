package com.edlplan.beatmapservice;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.edlplan.beatmapservice.ui.TriangleEffectView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

}
