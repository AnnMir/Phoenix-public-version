package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

import nsu.fit.g14201.marchenko.phoenix.App;

/**
 * Thread for asynchronous operation on the camera preview
 */
class CameraThread extends Thread {
    private static final boolean VERBOSE = true;
    private final WeakReference<CameraGLView> surface;
    private final Object threadSyncObject = new Object();
    private CameraHandler handler;
    private CameraWrapper cameraWrapper;
    volatile boolean isRunning = false;

    CameraThread(@NonNull CameraGLView surface, @NonNull CameraWrapper cameraWrapper) {
        super("Camera thread");
        this.surface = new WeakReference<>(surface);
        this.cameraWrapper = cameraWrapper;
    }

    CameraHandler getHandler() {
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
    void startPreview(int width, int height) throws CameraAccessException {
        if (VERBOSE) {
            Log.d(App.getTag(), "StartPreview");
        }
        CameraGLView surface = this.surface.get();
        if (surface == null || cameraWrapper != null) {
            return;
        }

        cameraWrapper.configureCamera(width, height, surface);
        // adjust view size with keeping the aspect ration of camera preview
        surface.post(surface::setVideoSizeAccordingToPreviewSize);
        cameraWrapper.setPreviewTexture(surface.getUpdatedSurfaceTexture());
//        cameraWrapper.openCamera();
//            try {
//                final SurfaceTexture st = surface.getSurfaceTexture();
//                st.setDefaultBufferSize(previewSize.width, previewSize.height);
//                mCamera.setPreviewTexture(st);
//            } catch (final IOException e) {
//                Log.e(TAG, "startPreview:", e);
//                if (mCamera != null) {
//                    mCamera.release();
//                    mCamera = null;
//                }
//            } catch (final RuntimeException e) {
//                Log.e(TAG, "startPreview:", e);
//                if (mCamera != null) {
//                    mCamera.release();
//                    mCamera = null;
//                }
//            }
//            if (mCamera != null) {
//                // start camera preview display
//                mCamera.startPreview();
//            }
    }

    void stopPreview() {
        if (VERBOSE) {
            Log.d(App.getTag(), "stopPreview:");
        }
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//        }
        CameraGLView cameraGLView = surface.get();
        if (cameraGLView != null) {
            cameraGLView.cameraHandler = null;
        }
    }
}
