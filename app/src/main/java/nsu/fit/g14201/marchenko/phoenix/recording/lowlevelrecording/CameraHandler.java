package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.os.Handler;

class CameraHandler extends Handler {
    private CameraThread thread; // FIXME: Doesn't it need to be weak?

    CameraHandler(final CameraThread thread) {
        this.thread = thread;
    }

    void startPreview(final int width, final int height) {
        // TODO NEXT
//        sendMessage(obtainMessage(MSG_PREVIEW_START, width, height));
    }
}
