package com.MoP.os_pdf;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class LoadingActivity extends Activity {
    ProgressBar progressBar;
    public int time_check = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        startLoading();
        progressBar = findViewById(R.id.progressBar);
    }

    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
        time_check = 0;

    }

    public void step(View v) {
        progressBar.setIndeterminate(true);
    }
}