package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;

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
    private volatile boolean isRunning = false;

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

        cameraWrapper.openCamera(width, height, surface.getSurfaceTexture());
//            try {
//                mCamera = Camera.open(CAMERA_ID);
//                final Camera.Parameters params = mCamera.getParameters();
//                final List<String> focusModes = params.getSupportedFocusModes();
//                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//                } else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                } else {
//                    if (DEBUG) Log.i(TAG, "Camera does not support autofocus");
//                }
//                // let's try fastest frame rate. You will get near 60fps, but your device become hot.
//                final List<int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
////					final int n = supportedFpsRange != null ? supportedFpsRange.size() : 0;
////					int[] range;
////					for (int i = 0; i < n; i++) {
////						range = supportedFpsRange.get(i);
////						Log.i(TAG, String.format("supportedFpsRange(%d)=(%d,%d)", i, range[0], range[1]));
////					}
//                final int[] max_fps = supportedFpsRange.get(supportedFpsRange.size() - 1);
//                Log.i(TAG, String.format("fps:%d-%d", max_fps[0], max_fps[1]));
//                params.setPreviewFpsRange(max_fps[0], max_fps[1]);
//                params.setRecordingHint(true);
//                // request closest supported preview size
//                final Camera.Size closestSize = getClosestSupportedSize(
//                        params.getSupportedPreviewSizes(), width, height);
//                params.setPreviewSize(closestSize.width, closestSize.height);
//                // request closest picture size for an aspect ratio issue on Nexus7
//                final Camera.Size pictureSize = getClosestSupportedSize(
//                        params.getSupportedPictureSizes(), width, height);
//                params.setPictureSize(pictureSize.width, pictureSize.height);
//                // rotate camera preview according to the device orientation
//                setRotation(params);
//                mCamera.setParameters(params);
//                // get the actual preview size
//                final Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
//                Log.i(TAG, String.format("previewSize(%d, %d)", previewSize.width, previewSize.height));
//                // adjust view size with keeping the aspect ration of camera preview.
//                // here is not a UI thread and we should request surface view to execute.
//                surface.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        surface.setVideoSize(previewSize.width, previewSize.height);
//                    }
//                });
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
}
