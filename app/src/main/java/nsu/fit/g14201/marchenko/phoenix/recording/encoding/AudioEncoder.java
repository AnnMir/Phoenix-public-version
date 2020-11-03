package nsu.fit.g14201.marchenko.phoenix.recording.encoding;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.utils.MediaCodecUtils;

public class AudioEncoder extends MediaAudioEncoder {
    private final boolean VERBOSE = true;

    private static final String MIME_TYPE = "audio/mp4a-latm";
    // 44.1[KHz] is the only setting guaranteed to be available on all devices
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

    private AudioThread audioThread = null;

    public AudioEncoder(MediaMuxerWrapper muxer, Listener listener) throws MediaMuxerException {
        super(muxer, listener);
    }

    @Override
    public void prepare() throws CameraException, IOException {
        trackIndex = -1;
        muxerStarted = EOS = false;

        MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, 1);

        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        if (VERBOSE) {
            Log.d(App.getTag(), "Expected audio format: " + format);
        }

        Log.d(App.getTag2(), "THREAD " + Thread.currentThread().getName());

        mediaCodec = MediaCodecUtils.getCodecByFormat(format);

//        MediaCodec.Callback callback = new MediaCodec.Callback() {
//            @Override
//            public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferId) {
//                if (!muxerStarted) {
//                    Log.d(App.getTag2(), "INPUT buffer available, but RETURNED");
//                    return;
//                }
//
//                Log.d(App.getTag2(), "INPUT buffer available");
//
//                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
//
//                // read audio data from internal mic
//                int size = inputBuffer.capacity() < SAMPLES_PER_FRAME ?
//                        inputBuffer.capacity()
//                        : SAMPLES_PER_FRAME;
//                int bytesRead = audioRecord.read(inputBuffer, size);
//
//                codec.queueInputBuffer(inputBufferId, 0, bytesRead, getPTSUs(), 0);
//            }
//
//            @Override
//            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
//                                                @NonNull MediaCodec.BufferInfo info) {
////                Log.d(App.getTag(), "name = " + Thread.currentThread().getName());
//
//                Log.d(App.getTag2(), "OUTPUT buffer available");
//
//                ByteBuffer encodedData = codec.getOutputBuffer(index);
//                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    info.size = 0;
//                }
//
//                if (info.size != 0) {
//                    if (!muxerStarted) {
//                        throw new RuntimeException("onOutputBufferAvailable: muxer hasn't started");
//                    }
//                    prevOutputPTSUs = info.presentationTimeUs;
//                    muxer.get().writeSampleData(trackIndex, encodedData, info);
//                }
//                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    prevOutputPTSUs = info.presentationTimeUs;
//                    muxer.get().writeSampleData(trackIndex, encodedData, info);
//                }
//                codec.releaseOutputBuffer(index, false);
//
//                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    release();
//                }
//            }
//
//            @Override
//            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
//                listener.onError(e);
//                release();
//            }
//
//            @Override
//            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
//                if (muxerStarted) {	// Second time request is an error
//                    throw new RuntimeException("Format changed twice");
//                }
//                MediaMuxerWrapper muxer = AudioEncoder.super.muxer.get();
//                try {
//                    trackIndex = muxer.addTrack(format);
//                } catch (MediaMuxerException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
//                }
//                if (!muxer.start()) {
//                    // we should wait until muxer is ready
//                    synchronized (muxerSync) {
//                        while (!muxer.hasStarted()) {
//                            try {
//                                muxerSync.wait(100);
//                            } catch (InterruptedException e) {}
//                        }
//                    }
//                }
//                muxerStarted = true;
//                Log.d(App.getTag2(), "MUXER STARTED");
//                Log.d(App.getTag2(), Thread.currentThread().getName());
//            }
//        };
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mediaCodec.setCallback(callback, handler);
//        } else {
//            mediaCodec.setCallback(callback); // FIXME: Callback is on UI thread
//        }

        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();

        if (VERBOSE) {
            Log.d(App.getTag(), "Configured video format: " + mediaCodec.getOutputFormat());
        }

        if (listener != null) {
            listener.onPrepared(this);
        }
    }

    @Override
    protected void startRecording() {
        super.startRecording();
//        // create and execute audio capturing thread using internal mic
        if (audioThread == null) {
            audioThread = new AudioThread();
            audioThread.start();
        }
    }

    @Override
    protected void release() {
        audioThread = null;
        super.release();
    }

    private static final int[] AUDIO_SOURCES = new int[] {
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };
//
    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write it to the MediaCodec encoder
     */
    private class AudioThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                final int minBufferSize = AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
                if (bufferSize < minBufferSize)
                    bufferSize = ((minBufferSize / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

                AudioRecord audioRecord = null;
                for (final int source : AUDIO_SOURCES) {
                    try {
                        audioRecord = new AudioRecord(
                                source, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                            audioRecord = null;
                        }
                    } catch (final Exception e) {
                        audioRecord = null;
                    }
                    if (audioRecord != null) {
                        break;
                    }
                }
                if (audioRecord != null) {
                    Log.d(App.getTag(), "AudioRecord CREATED!");
                    try {
                        if (isCapturing) {
                            if (VERBOSE) {
                                Log.d(App.getTag(), "AudioThread: start audio recording");
                            }
                            final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                            int readBytes;
                            audioRecord.startRecording();
                            try {
                                for (; isCapturing && !requestStop && !EOS ;) {
                                    // read audio data from internal mic
                                    buf.clear();
                                    readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                                    if (readBytes > 0) {
                                        // set audio data to encoder
                                        buf.position(readBytes);
                                        buf.flip();
                                        encode(buf, readBytes, getPTSUs());
                                        frameAvailableSoon();
                                    }
                                }
                                frameAvailableSoon();
                            } finally {
                                audioRecord.stop();
                            }
                        }
                    } finally {
                        audioRecord.release();
                    }
                } else {
                    Log.e(App.getTag(), "failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                Log.e(App.getTag(), "AudioThread#run", e);
            }
            if (VERBOSE) {
                Log.v(App.getTag(), "AudioThread:finished");
            }
        }
    }
}
