package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.VideoEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.glutils.GLDrawer2D;

import static nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView.SCALE_CROP_CENTER;
import static nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView.SCALE_KEEP_ASPECT;
import static nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView.SCALE_KEEP_ASPECT_VIEWPORT;
import static nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView.SCALE_STRETCH_FIT;

final class CameraSurfaceRenderer implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener{
    private final WeakReference<CameraGLView> surface;
    private GLDrawer2D drawer; // Object for preview display
    private final float[] stMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    private int textureId;
    private VideoEncoder videoEncoder;

    SurfaceTexture surfaceTexture;

    private static final boolean VERBOSE = true;

    CameraSurfaceRenderer(CameraGLView surface) {
        this.surface = new WeakReference<>(surface);
        Matrix.setIdentityM(mvpMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // This renderer required OES_EGL_image_external extension
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (!extensions.contains("OES_EGL_image_external"))
            throw new RuntimeException("This system does not support OES_EGL_image_external.");
        // Create texture ID
        textureId = GLDrawer2D.initTex();
        // Create SurfaceTexture with texture ID
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);
        // Clear screen with yellow color so that you can see rendering rectangle
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        CameraGLView parent = surface.get();
        if (parent != null) {
            parent.hasSurface = true;
        }
        drawer = new GLDrawer2D();
        drawer.setMatrix(mvpMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // If width or height is zero, initialization of this view is still in progress
        if (width == 0 || height == 0) { return; }
        updateViewport();
        CameraGLView parent = surface.get();
        if (parent != null) {
            parent.startPreview(width, height);
        }
    }

    private volatile boolean requestUpdateTex = false;
    private boolean flip = true;

    /**
     * drawing to GLSurface
     * we set renderMode to GLSurfaceView.RENDERMODE_WHEN_DIRTY,
     * this method is only called when #requestRender is called(= when texture is required to update)
     * if you don't set RENDERMODE_WHEN_DIRTY, this method is called at maximum 60fps
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (requestUpdateTex) {
            requestUpdateTex = false;
            // Update texture (that came from camera)
            surfaceTexture.updateTexImage();
            // Get texture matrix
            surfaceTexture.getTransformMatrix(stMatrix);
        }
        // Draw to preview screen
        drawer.draw(textureId, stMatrix);
        flip = !flip;
        if (flip) {	// ~30fps // FIXME: А точно 30 fps?
            synchronized (this) {
                if (videoEncoder != null) {
                    // Notify to capturing thread that the camera frame is available.
                    videoEncoder.frameAvailableSoon(stMatrix, mvpMatrix);
                }
            }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestUpdateTex = true;
    }

    void updateViewport() {
        CameraGLView parent = surface.get();
        if (parent == null) {
            return;
        }
        final int viewWidth = parent.getWidth();
        final int viewHeight = parent.getHeight();
        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        final double videoWidth = parent.videoWidth;
        final double videoHeight = parent.videoHeight;
        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }
        Matrix.setIdentityM(mvpMatrix, 0);
        final double viewAspect = viewWidth / (double) viewHeight;
        Log.i(App.getTag(), String.format("view(%d,%d)%f,video(%1.0f,%1.0f)", viewWidth,
                viewHeight, viewAspect, videoWidth, videoHeight));
        switch (parent.scaleMode) {
            case SCALE_STRETCH_FIT:
                break;
            case SCALE_KEEP_ASPECT_VIEWPORT: {
                final double req = videoWidth / videoHeight;
                int x, y;
                int width, height;
                if (viewAspect > req) {
                    // If view is wider than camera image, calculate width of drawing area
                    // based on view height
                    y = 0;
                    height = viewHeight;
                    width = (int) (req * viewHeight);
                    x = (viewWidth - width) / 2;
                } else {
                    // If view is higher than camera image, calculate height of drawing area
                    // based on view width
                    x = 0;
                    width = viewWidth;
                    height = (int) (viewWidth / req);
                    y = (viewHeight - height) / 2;
                }
                // Set viewport to draw keeping aspect ration of camera image
                if (VERBOSE) Log.v(App.getTag(), String.format("xy(%d,%d),size(%d,%d)", x, y,
                        width, height));
                GLES20.glViewport(x, y, width, height);
                break;
            }
            case SCALE_KEEP_ASPECT:
            case SCALE_CROP_CENTER:
                final double scaleX = viewWidth / videoWidth;
                final double scaleY = viewHeight / videoHeight;
                final double scale = (parent.scaleMode == SCALE_CROP_CENTER
                        ? Math.max(scaleX, scaleY) : Math.min(scaleX, scaleY));
                final double width = scale * videoWidth;
                final double height = scale * videoHeight;
                Log.v(App.getTag(), String.format("size(%1.0f,%1.0f),scale(%f,%f),mat(%f,%f)",
                        width, height, scaleX, scaleY, width / viewWidth, height / viewHeight));
                Matrix.scaleM(mvpMatrix, 0, (float) (width / viewWidth),
                        (float) (height / viewHeight), 1.0f);
                break;
        }
        if (drawer != null) {
            drawer.setMatrix(mvpMatrix, 0);
        }
    }
}