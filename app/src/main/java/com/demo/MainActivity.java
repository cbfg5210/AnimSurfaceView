package com.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private AnimSurfaceView asvAnim;
    private int imgArrayRes = R.array.array_fr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        asvAnim = findViewById(R.id.asvAnim);

        findViewById(R.id.btnStart).setOnClickListener(v -> asvAnim.start());
        findViewById(R.id.btnPause).setOnClickListener(v -> asvAnim.pause());
        findViewById(R.id.btnResume).setOnClickListener(v -> asvAnim.resume());
        findViewById(R.id.btnSwitchAnim).setOnClickListener(v -> {
            imgArrayRes = imgArrayRes == R.array.array_fr ? R.array.array_wr : R.array.array_fr;
            asvAnim.setImgArrayRes(imgArrayRes);
        });
    }
}
