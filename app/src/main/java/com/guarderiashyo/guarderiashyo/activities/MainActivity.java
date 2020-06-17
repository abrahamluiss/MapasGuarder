package com.guarderiashyo.guarderiashyo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.guarderiashyo.guarderiashyo.R;

public class MainActivity extends AppCompatActivity {

    Handler mHandler;
    Runnable mRunnable;
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imageView);
        img.animate().alpha(4000).setDuration(0);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, InicioActivity.class);
                startActivity(i);
                finish();


            }
        }, 4000);

    }
}
