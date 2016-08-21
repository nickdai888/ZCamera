package com.zezooz.main.zcamera;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class VideoRecordActivity extends AppCompatActivity {
    private CameraSurfaceView mView;
    private PowerManager.WakeLock mWL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.context = this;
        // full screen & full brightness
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();
        mView = new CameraSurfaceView(this);
        setContentView(mView);
    }

    @Override
    protected void onPause() {
        if (mWL.isHeld())
            mWL.release();
        mView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
        if (!mWL.isHeld()) mWL.acquire();
    }
}