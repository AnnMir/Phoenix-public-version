package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


import android.media.MediaCodec;

import java.io.IOException;
import java.lang.ref.WeakReference;

import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;

public abstract class MediaEncoder implements Runnable {
    protected final Object sync = new Object();
    protected WeakReference<MediaMuxerWrapper> muxer;
    protected MediaCodec mediaCodec;
    protected MediaEncoderListener listener;

    protected int trackIndex;
    protected boolean EOS; // Flag that indicates that encoder received EOS
    protected boolean muxerStarted; // Flag that indicates that the muxer is running

    private MediaCodec.BufferInfo bufferInfo;

    MediaEncoder(MediaEncoderListener listener) {
        this.listener = listener;

        synchronized (sync) {
            // Create BufferInfo here for effectiveness (to reduce GC)
            bufferInfo = new MediaCodec.BufferInfo();
            // Wait for a new thread to start
            new Thread(this, getClass().getSimpleName()).start();
            try {
                sync.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Encoding loop in the private thread
     */
    @Override
    public void run() {
        // TODO NEXT
        synchronized (sync) {
            sync.notify();
        }
    }

    public abstract void prepare() throws CameraException, IOException;

    protected void release() {
        listener.onStopped(this);
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (muxerStarted) {
//            final MediaMuxerWrapper muxer = mWeakMuxer != null ? mWeakMuxer.get() : null;
//            if (muxer != null) {
//                try {
//                    muxer.stop();
//                } catch (final Exception e) {
//                    Log.e(TAG, "failed stopping muxer", e);
//                }
//            }
        }
        bufferInfo = null;
    }

    public interface MediaEncoderListener {
        void onPrepared(MediaEncoder encoder);

        void onStopped(MediaEncoder encoder);
    }
}
