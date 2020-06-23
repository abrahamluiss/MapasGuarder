package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.guarderiashyo.guarderiashyo.R;

public class RequestGuarderiaActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTxtViewLookingFor;
    private Button mBtnCancelRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_guarderia);
        mAnimation = findViewById(R.id.animation);
        mTxtViewLookingFor = findViewById(R.id.txtViewLookingFor);
        mBtnCancelRequest = findViewById(R.id.cancelRequest);

        mAnimation.playAnimation();
    }
}
