package nsu.fit.g14201.marchenko.phoenix.recording.encoding;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;

public abstract class MediaEncoder implements Runnable {
    private final boolean VERBOSE = true;
    private final boolean EXTREMELY_VERBOSE = false;

    protected static final int TIMEOUT_USEC = 10000;	// 10[msec]

    protected final Object sync = new Object();
    protected final Object muxerSync = new Object();
    protected Handler handler;
    protected WeakReference<MediaMuxerWrapper> muxer;
    protected MediaCodec mediaCodec;
    protected Listener listener;

    protected int trackIndex;
    protected boolean EOS; // Flag that indicates that encoder received EOS
    protected boolean muxerStarted; // Flag that indicates that the muxer is running
    protected volatile boolean isCapturing = false;

    private MediaCodec.BufferInfo bufferInfo;

    MediaEncoder(Listener listener) {
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

    @Override
    public void run() {
        synchronized (sync) {
            sync.notifyAll();
        }

        Looper.prepare();
        handler = new Handler();
        Looper.loop();
    }

    public abstract void prepare() throws CameraException, IOException;

    void startRecording() {
        isCapturing = true;
    }

    void stopRecording() {
        if (!isCapturing) {
            return;
        }
        signalEndOfInputStream();
    }

    MediaFormat getOutputFormat() {
        return mediaCodec.getOutputFormat();
    }

    /**
     * @return old track index
     */
    int renewTrackIndex(int trackIndex) {
        int oldTrackIndex = trackIndex;
        this.trackIndex = trackIndex;

        return oldTrackIndex;
    }

    protected void release() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (muxerStarted) {
            MediaMuxerWrapper muxer = this.muxer != null ? this.muxer.get() : null;
            if (muxer != null) {
                muxer.stop();
            }
        }
        bufferInfo = null;
        isCapturing = false;
        Looper.myLooper().quit();

        listener.onStopped(this);
    }

    /**
     * Method to indicate that frame data will be available soon or it is already available
     * @return return true if encoder is ready to encode.
     */
    public boolean frameAvailableSoon() {
        return isCapturing;
    }

    /**
     * previous presentationTimeUs for writing
     */
    private long prevOutputPTSUs = 0;
    /**
     * get nextFragment encoding presentationTimeUs
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fails to write
        if (result < prevOutputPTSUs) {
            result = (prevOutputPTSUs - result) + result;
        }
        return result;
    }

    protected abstract void signalEndOfInputStream();

    /**
     * Method to set byte array to the MediaCodec encoder
     * @param buffer
     * @param length　length of byte array, zero means EOS.
     * @param presentationTimeUs
     */
    protected void encode(ByteBuffer buffer, int length, long presentationTimeUs) {
        if (!isCapturing) {
            return;
        }
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        while (isCapturing) {
            final int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    // send EOS
                    EOS = true;
                    if (VERBOSE) {
                        Log.d(App.getTag(), "send BUFFER_FLAG_END_OF_STREAM");
                    }
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeUs, 0);
                }
                break;
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait for MediaCodec encoder is ready to encode
                // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                // will wait for maximum TIMEOUT_USEC(10msec) on each call
            }
        }
    }

    public interface Listener {
        void onPrepared(MediaEncoder encoder);

        void onStopped(MediaEncoder encoder);

        void onError(@NonNull MediaCodec.CodecException e);
    }
}

// FIXME: Обработать ошибки записи в правильных местах, проверить, правильные ли слушатели
