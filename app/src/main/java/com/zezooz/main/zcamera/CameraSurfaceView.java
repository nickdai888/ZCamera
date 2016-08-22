package com.zezooz.main.zcamera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Nick on 2016/8/21.
 */

public class CameraSurfaceView extends GLSurfaceView {
    private CameraRender mRenderer;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }


    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init () {
        mRenderer = new CameraRender(this);
        setEGLContextClientVersion ( 2 );
        setRenderer ( mRenderer );
        setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
    }

    public CameraRender getCameraRender(){
        return mRenderer;
    }

    public void surfaceCreated ( SurfaceHolder holder ) {
        super.surfaceCreated ( holder );
    }

    public void surfaceDestroyed ( SurfaceHolder holder ) {
        mRenderer.close();
        super.surfaceDestroyed ( holder );
    }

    public void surfaceChanged ( SurfaceHolder holder, int format, int w, int h ) {
        super.surfaceChanged ( holder, format, w, h );
    }

//    public void setEGLContextClientVersion(int EGLContextClientVersion) {
//        this.EGLContextClientVersion = EGLContextClientVersion;
//    }



}
