package com.edlplan.beatmapservice;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RadioGroup;

public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        RadioGroup group = findViewById(R.id.bar_code_type);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.wc) {
                ((ImageView) findViewById(R.id.img)).setImageResource(R.drawable.pay_weixin);
            } else {
                ((ImageView) findViewById(R.id.img)).setImageResource(R.drawable.pay_zhifubao);
            }
        });
    }

}
