package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.VideoEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.glutils.GLDrawer2D;

/**
 * Sub class of GLSurfaceView to display camera preview and write video frame to capturing surface
 */
public class CameraGLView extends GLSurfaceView {
    private final CameraSurfaceRenderer renderer;
    private CameraHandler cameraHandler = null;

    boolean hasSurface;
    int videoWidth, videoHeight;
    int scaleMode = SCALE_STRETCH_FIT;

    static final int SCALE_STRETCH_FIT = 0;
    static final int SCALE_KEEP_ASPECT_VIEWPORT = 1;
    static final int SCALE_KEEP_ASPECT = 2;
    static final int SCALE_CROP_CENTER = 3;

    private static final boolean VERBOSE = true;

    public CameraGLView(Context context) {
        this(context, null, 0);
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraGLView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        renderer = new CameraSurfaceRenderer(this);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
		// the frequency of refreshing of camera preview is at most 15 fps
		// and RENDERMODE_WHEN_DIRTY is better to reduce power consumption
//		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasSurface && cameraHandler == null) {
            if (VERBOSE) {
                Log.v(App.getTag(), "Surface already exists");
            }
            startPreview(getWidth(),  getHeight());
        }
    }

    synchronized void startPreview(final int width, final int height) {
        if (cameraHandler == null) {
            CameraThread thread = new CameraThread(this);
            thread.start();
            cameraHandler = thread.getHandler();
        }
        cameraHandler.startPreview(width, height);
    }


}
