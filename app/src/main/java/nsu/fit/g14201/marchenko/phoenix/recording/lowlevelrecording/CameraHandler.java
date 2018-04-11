package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.os.Handler;
import android.os.Message;

class CameraHandler extends Handler {
    private static final int MSG_PREVIEW_START = 1;
    private CameraThread thread; // FIXME: Doesn't it need to be weak?

    CameraHandler(final CameraThread thread) {
        this.thread = thread;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_PREVIEW_START:
                thread.startPreview(msg.arg1, msg.arg2);
                break;
//            case MSG_PREVIEW_STOP:
//                mThread.stopPreview();
//                synchronized (this) {
//                    notifyAll();
//                }
//                Looper.myLooper().quit();
//                mThread = null;
//                break;
            default:
                throw new RuntimeException("Unknown message: what = " + msg.what);
        }
    }

    void startPreview(int width, int height) {
        sendMessage(obtainMessage(MSG_PREVIEW_START, width, height));
    }
}
