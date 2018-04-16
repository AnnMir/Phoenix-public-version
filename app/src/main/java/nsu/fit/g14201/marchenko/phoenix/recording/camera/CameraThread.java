package nsu.fit.g14201.marchenko.phoenix.recording.camera;

import android.hardware.camera2.CameraAccessException;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.CameraGLView;

/**
 * Thread for asynchronous operation on the camera preview
 */
public class CameraThread extends Thread {
    private static final boolean VERBOSE = true;

    private final WeakReference<CameraGLView> surface;
    private final Object threadSyncObject = new Object();
    private CameraWrapper cameraWrapper;

    CameraHandler handler;
    volatile boolean isRunning = false;

    public CameraThread(@NonNull CameraGLView surface, @NonNull CameraWrapper cameraWrapper) {
        super("Camera thread");
        this.surface = new WeakReference<>(surface);
        this.cameraWrapper = cameraWrapper;
    }

    public CameraHandler getHandler() {
        synchronized (threadSyncObject) {
            try {
                threadSyncObject.wait();
            } catch (InterruptedException e) {
            }
        }
        return handler;
    }

    /**
     * prepare Looper and create Handler for this thread
     */
    @Override
    public void run() {
        if (VERBOSE) {
            Log.d(App.getTag(), "Camera thread start");
        }
        Looper.prepare();
        synchronized (threadSyncObject) {
            handler = new CameraHandler(this);
            isRunning = true;
            threadSyncObject.notify();
        }
        Looper.loop();
        if (VERBOSE) {
            Log.d(App.getTag(), "Camera thread finish");
        }
        synchronized (threadSyncObject) {
            handler = null;
            isRunning = false;
        }
    }

    /**
     * Starts camera preview
     *
     * @param width
     * @param height
     */
    synchronized void startPreview(int width, int height) throws CameraAccessException {
        if (VERBOSE) {
            Log.d(App.getTag(), "StartPreview");
        }
        CameraGLView surface = this.surface.get();
        if (surface == null) {
            return;
        }

        cameraWrapper.configureCamera(width, height, surface);
        // adjust view size with keeping the aspect ration of camera preview
        surface.post(surface::setVideoSizeAccordingToPreviewSize);
        Log.d(App.getTag(), "Setting preview texture");
        cameraWrapper.setPreviewTexture(surface.getUpdatedSurfaceTexture());
        cameraWrapper.openCamera();
    }

    void stopPreview() {
        if (VERBOSE) {
            Log.d(App.getTag(), "stopPreview:");
        }
        cameraWrapper.closeCamera();

        CameraGLView cameraGLView = surface.get();
        if (cameraGLView != null) {
            cameraGLView.cameraHandler = null;
        }
    }
}
