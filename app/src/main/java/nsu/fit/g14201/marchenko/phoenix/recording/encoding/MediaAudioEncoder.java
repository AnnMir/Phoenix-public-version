package nsu.fit.g14201.marchenko.phoenix.recording.encoding;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;

public abstract class MediaAudioEncoder implements Runnable {
    private final boolean VERBOSE = true;

    protected static final int TIMEOUT_USEC = 10000;	// 10[msec]

    protected final Object sync = new Object();
    protected Handler handler;
    protected WeakReference<MediaMuxerWrapper> muxer = null;
    protected MediaCodec mediaCodec;
    protected Listener listener;

    protected int trackIndex;
    protected boolean EOS; // Flag that indicates that encoder received EOS
    protected boolean muxerStarted; // Flag that indicates that the muxer is running
    protected volatile boolean isCapturing = false;
    protected volatile boolean requestStop;
    /**
     * Flag that indicate the frame data will be available soon.
     */
    private int requestDrain;

    private MediaCodec.BufferInfo bufferInfo;

    MediaAudioEncoder(MediaMuxerWrapper muxer, Listener listener) throws MediaMuxerException {
        WeakReference<MediaMuxerWrapper> weakMuxer = new WeakReference<>(muxer);
        muxer.addEncoder((AudioEncoder) this);
        this.muxer = weakMuxer;

        this.listener = listener;

        synchronized (sync) {
            // Create BufferInfo here for effectiveness (to reduce GC)
            bufferInfo = new MediaCodec.BufferInfo();
            // Wait for a new thread to start
            new Thread(this, getClass().getSimpleName()).start();
            try {
                sync.wait();
            } catch (InterruptedException e) {}
        }
    }

    /**
     * encoding loop on private thread
     */
    @Override
    public void run() {
        synchronized (sync) {
            requestStop = false;
            requestDrain = 0;
            sync.notify();
        }
        final boolean isRunning = true;
        boolean localRequestStop;
        boolean localRequestDrain;
        while (isRunning) {
            synchronized (sync) {
                localRequestStop = requestStop;
                localRequestDrain = (requestDrain > 0);
                if (localRequestDrain) {
                    requestDrain--;
                }
            }
            if (localRequestStop) {
                drain();
                // request stop recording
                signalEndOfInputStream();
                // process output data again for EOS signal
                drain();
                // release all related objects
                release();
                break;
            }
            if (localRequestDrain) {
                drain();
            } else {
                synchronized (sync) {
                    try {
                        sync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        } // end of while
        if (VERBOSE) {
            Log.d(App.getTag2(), "Encoder thread exiting");
        }
        synchronized (sync) {
            requestStop = true;
            isCapturing = false;
        }
    }

    protected void release() {
        if (VERBOSE) {
            Log.d(App.getTag2(), "release:");
        }
        try {
            listener.onStopped(this);
        } catch (final Exception e) {
            Log.e(App.getTag2(), "failed onStopped", e);
        }
        isCapturing = false;
        if (mediaCodec != null) {
            try {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            } catch (final Exception e) {
                Log.e(App.getTag2(), "failed releasing MediaCodec", e);
            }
        }
        if (muxerStarted) {
            final MediaMuxerWrapper muxerWrapper = muxer != null ? muxer.get() : null;
            if (muxerWrapper != null) {
                try {
                    muxerWrapper.stop();
                } catch (final Exception e) {
                    Log.e(App.getTag2(), "failed stopping muxer", e);
                }
            }
        }
        bufferInfo = null;
    }

    public abstract void prepare() throws CameraException, IOException;

    void startRecording() {
        synchronized (sync) {
            isCapturing = true;
            requestStop = false;
            sync.notifyAll();
        }
    }

    void stopRecording() {
        if (VERBOSE) {
            Log.v(App.getTag2(), "stopRecording");
        }
        synchronized (sync) {
            if (!isCapturing || requestStop) {
                return;
            }
            requestStop = true;	// for rejecting newer frame
            sync.notifyAll();
            // We can not know when the encoding and writing finish.
            // so we return immediately after request to avoid delay of caller thread
        }


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

    /**
     * Method to indicate that frame data will be available soon or it is already available
     * @return return true if encoder is ready to encode.
     */
    public boolean frameAvailableSoon() {
        synchronized (sync) {
            if (!isCapturing || requestStop) {
                return false;
            }
            requestDrain++;
            sync.notifyAll();
        }
        return true;
    }

    /**
     * previous presentationTimeUs for writing
     */
    protected long prevOutputPTSUs = 0;
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

    protected void signalEndOfInputStream() {
        if (VERBOSE) {
            Log.d(App.getTag2(), "sending EOS to encoder");
        }
        encode(null, 0, getPTSUs());
    }

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

    /**
     * drain encoded data and write them to muxer
     */
    protected void drain() {
        if (mediaCodec == null) {
            return;
        }
        ByteBuffer[] encoderOutputBuffers = mediaCodec.getOutputBuffers();
        int encoderStatus, count = 0;
        final MediaMuxerWrapper muxerWrapper = muxer.get();
        if (muxerWrapper == null) {
//        	throw new NullPointerException("muxer is unexpectedly null");
            Log.w(App.getTag2(), "muxer is unexpectedly null");
            return;
        }
        LOOP:	while (isCapturing) {
            // get encoded data with maximum timeout duration of TIMEOUT_USEC(=10[msec])
            encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait 5 counts(=TIMEOUT_USEC x 5 = 50msec) until data/EOS come
                if (!EOS) {
                    if (++count > 5)
                        break LOOP;		// out of while
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (VERBOSE) {
                    Log.v(App.getTag2(), "INFO_OUTPUT_BUFFERS_CHANGED");
                }
                // this shoud not come when encoding
                encoderOutputBuffers = mediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (VERBOSE) {
                    Log.v(App.getTag2(), "INFO_OUTPUT_FORMAT_CHANGED");
                }
                // this status indicate the output format of codec is changed
                // this should come only once before actual encoded data
                // but this status never come on Android4.3 or less
                // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
                if (muxerStarted) {	// second time request is error
                    throw new RuntimeException("format changed twice");
                }
                // get output format from codec and pass them to muxer
                // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
                final MediaFormat format = mediaCodec.getOutputFormat(); // API >= 16
                try {
                    trackIndex = muxerWrapper.addTrack(format);
                } catch (MediaMuxerException e) {
                    e.printStackTrace();
                    throw new RuntimeException("audio#addTrack error");
                }
                muxerStarted = true;
                if (!muxerWrapper.start()) {
                    // we should wait until muxer is ready
                    synchronized (muxerWrapper) {
                        while (!muxerWrapper.hasStarted())
                            try {
                                muxerWrapper.wait(100);
                            } catch (final InterruptedException e) {
                                break LOOP;
                            }
                    }
                }
            } else if (encoderStatus < 0) {
                // unexpected status
                if (VERBOSE) {
                    Log.w(App.getTag2(), "drain:unexpected result from encoder#dequeueOutputBuffer: " + encoderStatus);
                }
            } else {
                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // You shoud set output format to muxer here when you target Android4.3 or less
                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                    // therefor we should expand and prepare output format from buffer data.
                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                    if (VERBOSE) {
                        Log.d(App.getTag2(), "drain:BUFFER_FLAG_CODEC_CONFIG");
                    }
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    // encoded data is ready, clear waiting counter
                    count = 0;
                    if (!muxerStarted) {
                        // muxer is not ready...this will prrograming failure.
                        throw new RuntimeException("drain:muxer hasn't started");
                    }
                    // write encoded data to muxer(need to adjust presentationTimeUs.
                    bufferInfo.presentationTimeUs = getPTSUs();
                    muxerWrapper.writeSampleData(trackIndex, encodedData, bufferInfo);
                    prevOutputPTSUs = bufferInfo.presentationTimeUs;
                }
                // return buffer to encoder
                mediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    isCapturing = false;
                    break;
                }
            }
        }
    }


    public interface Listener {
        void onPrepared(MediaAudioEncoder encoder);

        void onStopped(MediaAudioEncoder encoder);

        void onError(@NonNull MediaCodec.CodecException e);
    }
}

