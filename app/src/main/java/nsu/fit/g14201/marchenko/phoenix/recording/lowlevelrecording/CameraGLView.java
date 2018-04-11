package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;

import nsu.fit.g14201.marchenko.phoenix.App;

/**
 * Sub class of GLSurfaceView to display camera preview and write video frame to capturing surface
 */
public class CameraGLView extends GLSurfaceView {
    private final CameraSurfaceRenderer renderer;
    private CameraHandler cameraHandler = null;
    private CameraWrapper cameraWrapper;

    boolean hasSurface;
    int videoWidth, videoHeight;
    Size previewSize;
    int rotation;
    int scaleMode = SCALE_STRETCH_FIT;

    static final int SCALE_STRETCH_FIT = 0;
    static final int SCALE_KEEP_ASPECT_VIEWPORT = 1;
    static final int SCALE_KEEP_ASPECT = 2;
    static final int SCALE_CROP_CENTER = 3;

    private static final boolean VERBOSE = true;

    public CameraGLView(Context context, @NonNull CameraWrapper cameraWrapper) {
        this(context, null, 0, cameraWrapper);
    }

    public CameraGLView(Context context, AttributeSet attrs, @NonNull CameraWrapper cameraWrapper) {
        this(context, attrs, 0, cameraWrapper);
    }

    public CameraGLView(final Context context, AttributeSet attrs, int defStyle,
                        @NonNull CameraWrapper cameraWrapper) {
        super(context, attrs);
        renderer = new CameraSurfaceRenderer(this);
        setEGLContextClientVersion(2);
        setRenderer(renderer);

        this.cameraWrapper = cameraWrapper;

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

    synchronized void startPreview(int width, int height) {
        if (cameraHandler == null) {
            CameraThread thread = new CameraThread(this, cameraWrapper);
            thread.start();
            cameraHandler = thread.getHandler();
        }
        cameraHandler.startPreview(width, height);
    }

    SurfaceTexture getSurfaceTexture() {
        return renderer != null ? renderer.surfaceTexture : null;
    }
}
