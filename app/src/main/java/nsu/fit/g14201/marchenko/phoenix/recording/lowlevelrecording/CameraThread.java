package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;

import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;

import nsu.fit.g14201.marchenko.phoenix.App;

/**
 * Thread for asynchronous operation of camera preview
 */
class CameraThread extends Thread {
    private final WeakReference<CameraGLView> surface;
    private CameraHandler handler;
    private final Object threadSyncObject = new Object();
    private volatile boolean isRunning = false;

    private static final boolean VERBOSE = true;

    CameraThread(CameraGLView surface) {
        super("Camera thread");
        this.surface = new WeakReference<>(surface);
    }

    CameraHandler getHandler() {
        synchronized (threadSyncObject) {
            try {
                threadSyncObject.wait();
            } catch (InterruptedException e) {}
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
}
