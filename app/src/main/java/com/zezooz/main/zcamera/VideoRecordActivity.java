package com.zezooz.main.zcamera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Method;

public class VideoRecordActivity extends Activity implements View.OnClickListener {
    private View main;
    private CameraSurfaceView mView;
    private PowerManager.WakeLock mWL;
    private ImageButton recordButton;
    //    private ImageButton stopButton;
    private boolean buttonStatusRecording;
    private boolean showNavigationBar;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        main = getLayoutInflater().inflate(R.layout.activity_video_record, null);
//        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        main.setOnClickListener(this);
        setContentView(main);
//        setContentView(R.layout.activity_video_record);
        // full screen & full brightness
        setWindowMode();

        Utils.context = this;
        buttonStatusRecording = false;

        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();

        mView = (CameraSurfaceView) this.findViewById(R.id.surfaceview);
        recordButton = (ImageButton) this.findViewById(R.id.record);
        recordButton.setOnClickListener(this);
        if (checkDeviceHasNavigationBar(this)) {
            showNavigationBar = true;
            setNavHidenHandler();
        } else {
            showNavigationBar = false;
        }
        updateRecordButton();
        updateToolBarPosition();
        showControlPanelDelay();
    }

    private void showControlPanelDelay(){
       final LinearLayout toolbar = (LinearLayout) this.findViewById(R.id.toolbar);
        main.postDelayed(new Runnable(){
            public void run() {
                //execute the task
//
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    toolbar.setAlpha(0f);
                }

                toolbar.setVisibility(View.VISIBLE);
                // Animate the content view to 100% opacity, and clear any animation
                // listener set on the view.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    toolbar.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setListener(null);
                }
            }
        }, 1000);
    }



    private void setNavHidenHandler() {
//        IntentFilter intentfilter = new IntentFilter();
//        intentfilter.addAction(ACTION_DISPLAY_NAV_BAR);
//        intentfilter.addAction(ACTION_HIDE_NAV_BAR);
//        this.registerReceiver(new MyReceiver(),intentfilter);

    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }


    private void setWindowMode() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT > 18) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rootContainer);
//            relativeLayout.setPadding(0, getActionBarHeight()+getStatusBarHeight(), 0, 0);

//            window.getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }
    }

    private int getNavigationBarHeight() {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
//        Log.v("dbw", "Navi height:" + height);
        return height;
    }

    private void updateRecordButton() {
        if (buttonStatusRecording) {
            recordButton.setImageDrawable(getResources().getDrawable(R.drawable.player_stop));
        } else {
            recordButton.setImageDrawable(getResources().getDrawable(R.drawable.player_record));
        }
    }

    private void updateToolBarPosition() {

        int nav_h;
        if (showNavigationBar) {
            nav_h = getNavigationBarHeight();
        } else {
            nav_h = 20;
        }
        LinearLayout toolbar = (LinearLayout) this.findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        lp.bottomMargin = nav_h;
        toolbar.setLayoutParams(lp);
    }

    public void onClick(View source) {
        switch (source.getId()) {
            case R.id.record:
                if (!buttonStatusRecording) {
                    mView.getCameraRender().startMediaRecorder();
                } else {
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