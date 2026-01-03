package com.crate.crateam.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.crate.crateam.R;
import com.crate.crateam.utility.SessionManager;

public class SplashActivity extends Activity {
    Handler handler;
    Runnable runnable;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        sessionManager = new SessionManager(getApplicationContext());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
                public void run() {
                if (!sessionManager.checkLogin()){
                    Intent toDashBoard = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(toDashBoard);
                }else {
                    Intent toLogIn = new Intent(SplashActivity.this, LogInActivity.class);
                    startActivity(toLogIn);
                }
            }
        };
        int SPLASH_SCREEN_TIME_OUT = 3000;
        handler.postDelayed(runnable, SPLASH_SCREEN_TIME_OUT);
    }
}
