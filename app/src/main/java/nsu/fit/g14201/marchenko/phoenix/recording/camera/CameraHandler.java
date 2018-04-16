package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.hardware.camera2.CameraAccessException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import nsu.fit.g14201.marchenko.phoenix.App;

public class CameraHandler extends Handler {
    private static final boolean VERBOSE = true;
    private static final int MSG_PREVIEW_START = 1;
    private static final int MSG_PREVIEW_STOP = 2;

    private CameraThread thread; // FIXME: Shouldn't it be weak?

    CameraHandler(final CameraThread thread) {
        this.thread = thread;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_PREVIEW_START:
                try {
                    thread.startPreview(msg.arg1, msg.arg2);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_PREVIEW_STOP:
                thread.stopPreview();
                synchronized (this) {
                    notifyAll();
                }
                Looper.myLooper().quit();
                thread = null;
                break;
            default:
                throw new RuntimeException("Unknown message: what = " + msg.what);
        }
    }

    public void startPreview(int width, int height) {
        sendMessage(obtainMessage(MSG_PREVIEW_START, width, height));
    }

    /**
     * Request to stop camera preview
     * @param needWait need to wait for stopping camera preview
     */
    public void stopPreview(boolean needWait) {
        synchronized (this) {
            sendEmptyMessage(MSG_PREVIEW_STOP);
            if (needWait && thread.isRunning) {
                try {
                    if (VERBOSE) {
                        Log.d(App.getTag(), "Wait for termination of the camera thread");
                    }
                    wait();
                } catch (final InterruptedException e) {
                }
            }
        }
    }
}
