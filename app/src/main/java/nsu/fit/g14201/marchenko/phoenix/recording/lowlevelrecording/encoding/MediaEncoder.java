package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


import android.media.MediaCodec;
import android.media.MediaFormat;
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
    protected WeakReference<MediaMuxerWrapper> muxer;
    protected MediaCodec mediaCodec;
    protected MediaEncoderListener listener;

    protected int trackIndex;
    protected boolean EOS; // Flag that indicates that encoder received EOS
    protected boolean muxerStarted; // Flag that indicates that the muxer is running
    protected boolean isCapturing = false;
    protected boolean requestStop = false;

    private int requestDrain; // Flag that indicates that the frame data will be available soon
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
        synchronized (sync) {
            requestStop = false; // FIXME: Should it be removed?
            requestDrain = 0;
            sync.notify();
        }

        boolean isRunning = true;
        boolean localRequestStop;
        boolean localRequestDrain;

        try {
            while (isRunning) {
                synchronized (sync) {
                    localRequestStop = requestStop;
                    localRequestDrain = requestDrain > 0;
                    if (localRequestDrain) {
                        requestDrain--;
                    }
                }
                if (localRequestStop) {
                    drain();
                    signalEndOfInputStream();
                    // Process output data again for EOS signal
                    drain();
                    release();
                    break;
                }
                if (localRequestDrain) {
                    drain();
                } else {
                    synchronized (sync) {
                        try {
                            sync.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        } catch (MediaMuxerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (VERBOSE) {
            Log.d(App.getTag(), "Exited encoding thread");
        }
        synchronized (sync) {
            isCapturing = false;
            requestStop = true;
        }
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
        synchronized (sync) {
            if (!isCapturing || requestStop) {
                return;
            }
            requestStop = true;	// for rejecting newer frame
            sync.notifyAll();
            // We can not know when the encoding and writing finish.
            // so we return immediately after request to avoid delay of caller thread
        }
    }

    protected void release() {
        listener.onStopped(this);
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

    // Drains encoded data and writes it to muxer
    // FIXME IMPORTANT: Replace with newer version
    private void drain() throws MediaMuxerException {
        ByteBuffer[] encoderOutputBuffers = mediaCodec.getOutputBuffers();
        int encoderStatus, count = 0;
        MediaMuxerWrapper muxer = this.muxer.get();
        if (muxer == null) {
            Log.d(App.getTag(), "Muxer is unexpectedly null");
            return;
        }
        LOOP:	while (isCapturing) {
            // Get encoded data with maximum timeout duration of TIMEOUT_USEC (=10[msec])
            encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait 5 counts(=TIMEOUT_USEC x 5 = 50msec) until data/EOS come
                if (!EOS) {
                    if (++count > 5)
                        break LOOP;		// out of while
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (EXTREMELY_VERBOSE) {
                    Log.d(App.getTag(), "INFO_OUTPUT_BUFFERS_CHANGED");
                }
                // this should not come when encoding
                encoderOutputBuffers = mediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (EXTREMELY_VERBOSE) {
                    Log.d(App.getTag(), "INFO_OUTPUT_FORMAT_CHANGED");
                }
                // this status indicate the output format of codec is changed
                // this should come only once before actual encoded data
                // but this status never come on Android 4.3 or less
                // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
                if (muxerStarted) {	// second time request is error
                    throw new RuntimeException("Format changed twice");
                }
                // get output format from codec and pass them to muxer
                // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash
                MediaFormat format = mediaCodec.getOutputFormat();
                trackIndex = muxer.addTrack(format);
                muxerStarted = true;
                if (!muxer.start()) {
                    // we should wait until muxer is ready
                    synchronized (muxer) {
                        while (!muxer.hasStarted()) {
                            try {
                                muxer.wait(100);
                            } catch (InterruptedException e) {
                                break LOOP;
                            }
                        }
                    }
                }
            } else if (encoderStatus < 0) {
                // unexpected status
                if (VERBOSE) {
                    Log.d(App.getTag(), "Drain: unexpected result from encoder#dequeueOutputBuffer: "
                            + encoderStatus);
                }
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    // this never should come...may be a MediaCodec internal error
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // You should set output format to muxer here when you target Android4.3 or less
                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                    // therefore we should expand and prepare output format from buffer data.
                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                    if (VERBOSE) {
                        Log.d(App.getTag(), "drain:BUFFER_FLAG_CODEC_CONFIG");
                    }
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    // encoded data is ready, clear waiting counter
                    count = 0;
                    if (!muxerStarted) {
                        throw new RuntimeException("Drain: muxer hasn't started");
                    }
                    // write encoded data to muxer(need to adjust presentationTimeUs.
                    bufferInfo.presentationTimeUs = getPTSUs();
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    prevOutputPTSUs = bufferInfo.presentationTimeUs;
                }
                // return buffer to encoder
                mediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    // when EOS comes
                    isCapturing = false;
                    break;      // out of while
                }
            }
        }
    }

    /**
     * previous presentationTimeUs for writing
     */
    private long prevOutputPTSUs = 0;
    /**
     * get next encoding presentationTimeUs
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

    public interface MediaEncoderListener {
        void onPrepared(MediaEncoder encoder);

        void onStopped(MediaEncoder encoder);
    }
}
