package com.zezooz.main.zcamera;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Nick on 2016/8/21.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{

    private final String vss =
            "attribute vec2 vPosition;\n" +
                    "attribute vec2 vTexCoord;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  texCoord = vTexCoord;\n" +
                    "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
                    "}";

    private final String fss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
                    "}";

    private int[] hTex;
    private FloatBuffer pVertex;
    private FloatBuffer pTexCoord;
    private int hProgram;
private boolean mIsRecording;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceTexture mSTexture;

    private SurfaceHolder mSurfaceHolder;
    private boolean mUpdateST = false;

    private CameraSurfaceView mView;



    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    CameraRender ( CameraSurfaceView view ) {
        mView = view;
        float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
        float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
        pVertex = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put ( vtmp );
        pVertex.position(0);
        pTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put ( ttmp );
        pTexCoord.position(0);

        mSurfaceHolder = view.getHolder();
    }



    public void startMediaRecorder() {
        mCamera.unlock();
        mIsRecording = true;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile mCamcorderProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK,
                CamcorderProfile.QUALITY_480P);
        mMediaRecorder.setProfile(mCamcorderProfile);
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
//        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            mIsRecording = false;
//            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            mCamera.lock();
        }
        mMediaRecorder.start();
    }

    public void stopMediaRecorder() {
        if (mMediaRecorder != null) {
            if (mIsRecording) {
                mMediaRecorder.stop();
                mCamera.lock();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mIsRecording = false;
                try {
                    mCamera.reconnect();
                } catch (IOException e) {
//                    Toast.makeText(this, "reconect fail", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }


    private File getOutputMediaFile(int type) {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "Camera App");
        File mediaStorageDir = null;
        try {
            mediaStorageDir = new File(Environment.getExternalStorageDirectory().getCanonicalFile(),"/nick.mp4");
            return mediaStorageDir;
        } catch (IOException e) {
            e.printStackTrace();
        }
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("linc", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }




    public void close()
    {
        mUpdateST = false;
//        mSTexture.release();
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        deleteTex();
    }


    public void onSurfaceCreated ( GL10 unused, EGLConfig config ) {
        //String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        //Log.i("mr", "Gl extensions: " + extensions);
        //Assert.assertTrue(extensions.contains("OES_EGL_image_external"));
//       String vss = Utils.getShaderString(R.raw.vertex);
//       String fss = Utils.getShaderString(R.raw.fragment);
        initTex();
        mSTexture = new SurfaceTexture ( hTex[0] );
        mSTexture.setOnFrameAvailableListener(this);

        mCamera = Camera.open();
        try {
            mCamera.setPreviewTexture(mSTexture);
        } catch ( IOException ioe ) {
        }

        GLES20.glClearColor ( 1.0f, 1.0f, 0.0f, 1.0f );

        hProgram = loadShader ( vss, fss );
    }

    public void onDrawFrame ( GL10 unused ) {
        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );

        synchronized(this) {
            if ( mUpdateST ) {
                mSTexture.updateTexImage();
                mUpdateST = false;
            }
        }

        GLES20.glUseProgram(hProgram);

        int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
        int tch = GLES20.glGetAttribLocation ( hProgram, "vTexCoord" );
        int th = GLES20.glGetUniformLocation ( hProgram, "sTexture" );

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        GLES20.glUniform1i(th, 0);

        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4*2, pVertex);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4*2, pTexCoord );
        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFlush();
    }

    public void onSurfaceChanged ( GL10 unused, int width, int height ) {
        GLES20.glViewport( 0, 0, width, height );
        Camera.Parameters param = mCamera.getParameters();
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        if ( psize.size() > 0 ) {
            int i;
            for ( i = 0; i < psize.size(); i++ ) {
                if ( psize.get(i).width < width || psize.get(i).height < height )
                    break;
            }
            if ( i > 0 )
                i--;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);
            //Log.i("mr","ssize: "+psize.get(i).width+", "+psize.get(i).height);
        }

        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        param.set("orientation", "landscape");
        mCamera.setParameters ( param );
        mCamera.startPreview();
    }

    private void initTex() {
        hTex = new int[1];
        GLES20.glGenTextures ( 1, hTex, 0 );
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }

    private void deleteTex() {
        GLES20.glDeleteTextures ( 1, hTex, 0 );
    }

    public synchronized void onFrameAvailable ( SurfaceTexture st ) {
        mUpdateST = true;
        mView.requestRender();
    }

    private static int loadShader ( String vss, String fss ) {
        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vshader, vss);
        GLES20.glCompileShader(vshader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile vshader");
            Log.v("Shader", "Could not compile vshader:"+GLES20.glGetShaderInfoLog(vshader));
            GLES20.glDeleteShader(vshader);
            vshader = 0;
        }

        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fshader, fss);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile fshader");
            Log.v("Shader", "Could not compile fshader:"+GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        return program;
    }


}