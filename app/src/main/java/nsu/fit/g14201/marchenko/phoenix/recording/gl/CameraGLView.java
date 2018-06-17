package nsu.fit.g14201.marchenko.phoenix.recording.gl;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraHandler;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraThread;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraWrapper;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.VideoEncoder;

/**
 * Sub class of GLSurfaceView to display camera preview and write video frame to capturing surface
 */
public class CameraGLView extends GLSurfaceView {
    private static final boolean VERBOSE = true;

    static final int SCALE_STRETCH_FIT = 0;
    static final int SCALE_KEEP_ASPECT_VIEWPORT = 1;
    static final int SCALE_KEEP_ASPECT = 2;
    static final int SCALE_CROP_CENTER = 3;

    public CameraHandler cameraHandler = null;
    public Size previewSize;
    public int rotation;

    boolean hasSurface;
    int videoWidth, videoHeight;
    int scaleMode = SCALE_STRETCH_FIT;

    private final CameraSurfaceRenderer renderer;
    private CameraWrapper cameraWrapper;

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
        if (cameraHandler != null) {
            cameraHandler.stopPreview(false);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasSurface && cameraHandler == null) {
            if (VERBOSE) {
                Log.v(App.getTag(), "Surface already exists");
            }
            startPreview(getWidth(), getHeight());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (VERBOSE) {
            Log.d(App.getTag(), "surfaceDestroyed:");
        }
        if (cameraHandler != null) {
            cameraHandler.stopPreview(true);
        }
        cameraHandler = null;
        hasSurface = false;
        renderer.onSurfaceDestroyed();
        super.surfaceDestroyed(holder);
    }

    public void setVideoSize(int width, int height) {
        if ((rotation % 180) == 0) {
            Log.d(App.getTag(), "Orientation: Horizontal");
            videoWidth = width;
            videoHeight = height;
        } else {
            Log.d(App.getTag(), "Orientation: Vertical");
            videoWidth = height;
            videoHeight = width;
        }
        Log.d(App.getTag(), "Width = " + videoWidth + ", height: " + videoHeight);

        queueEvent(renderer::updateViewport);
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setCameraWrapper(CameraWrapper cameraWrapper) {
        this.cameraWrapper = cameraWrapper;
    }

    public void setVideoEncoder(VideoEncoder encoder) {
        queueEvent(() -> {
            synchronized (renderer) {
                if (encoder != null) {
                    encoder.setEglContext(EGL14.eglGetCurrentContext(), renderer.textureId);
                }
                renderer.videoEncoder = encoder;
            }
        });
    }

    public void setVideoSizeAccordingToPreviewSize() {
        setVideoSize(previewSize.getWidth(), previewSize.getHeight());
    }

    public SurfaceTexture getUpdatedSurfaceTexture() {
        SurfaceTexture surfaceTexture = getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        return surfaceTexture;
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
