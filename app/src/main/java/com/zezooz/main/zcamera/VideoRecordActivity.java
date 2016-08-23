package com.zezooz.main.zcamera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class VideoRecordActivity extends AppCompatActivity {
    private CameraSurfaceView mView;
    private PowerManager.WakeLock mWL;
    private ImageButton recordButton;
//    private ImageButton stopButton;
    private boolean buttonStatusRecording;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.context = this;
        buttonStatusRecording = false;
        // full screen & full brightness
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();
        setContentView(R.layout.activity_video_record);
        mView = (CameraSurfaceView) this.findViewById(R.id.surfaceview);
//      mView = new CameraSurfaceView(this);
//      setContentView(mView);
        recordButton = (ImageButton)this.findViewById(R.id.record);
//        stopButton = (ImageButton)this.findViewById(R.id.stop);
//        stopButton.setVisibility(View.VISIBLE);
        updateRecordButton();
    }

    private void updateRecordButton() {
        if (buttonStatusRecording) {
//            recordButton.setBackgroundResource(R.drawable.player_record);
            recordButton.setImageDrawable(getResources().getDrawable(R.drawable.player_stop));
//            recordButton.setImageDrawable( ContextCompat.getDrawable(this, R.drawable.player_record));

        } else {
//            recordButton.setBackgroundResource(R.drawable.player_stop);
            recordButton.setImageDrawable(getResources().getDrawable(R.drawable.player_record));
        }
    }

    public void onClick(View source) {
        switch (source.getId()) {
            // 单击录制按钮
            case R.id.record:
                if(!buttonStatusRecording) {
                    mView.getCameraRender().startMediaRecorder();
                }else{
                    mView.getCameraRender().stopMediaRecorder();
                }
                buttonStatusRecording = !buttonStatusRecording;
                updateRecordButton();
                break;
        }
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


    //check if there is camera
    private boolean checkCamera() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}