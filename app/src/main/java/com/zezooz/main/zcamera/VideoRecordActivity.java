package com.zezooz.main.zcamera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

public class VideoRecordActivity extends Activity {
    private CameraSurfaceView mView;
    private PowerManager.WakeLock mWL;
    private ImageButton recordButton;
//    private ImageButton stopButton;
    private boolean buttonStatusRecording;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_video_record);
        // full screen & full brightness
        setWindowMode();

        Utils.context = this;
        buttonStatusRecording = false;

        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();

        mView = (CameraSurfaceView) this.findViewById(R.id.surfaceview);
//      mView = new CameraSurfaceView(this);
//      setContentView(mView);
        recordButton = (ImageButton)this.findViewById(R.id.record);
//        stopButton = (ImageButton)this.findViewById(R.id.stop);
//        stopButton.setVisibility(View.VISIBLE);
        updateRecordButton();
    }

    public int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))// 如果资源是存在的、有效的
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }


    private void setWindowMode(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT > 18) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
////            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rootContainer);
//            relativeLayout.setPadding(0, getActionBarHeight()+getStatusBarHeight(), 0, 0);

//            window.getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        else
        {


        }
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