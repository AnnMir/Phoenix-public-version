package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.utils.MediaCodecUtils;

public class AudioEncoder extends MediaEncoder {
    private final boolean VERBOSE = true;

    private static final String MIME_TYPE = "audio/mp4a-latm";
    // 44.1[KHz] is the only setting guaranteed to be available on all devices
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

    public AudioEncoder(MediaMuxerWrapper muxer, MediaEncoderListener listener)
            throws MediaMuxerException {
        super(listener);
        WeakReference<MediaMuxerWrapper> weakMuxer = new WeakReference<>(muxer);
        muxer.addEncoder(this);
        this.muxer = weakMuxer;
    }

    @Override
    public void prepare() throws CameraException, IOException {
        trackIndex = -1;
        muxerStarted = EOS = false;

        // Prepare MediaCodec for AAC encoding of audio data from internal mic

        MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, 1);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        if (VERBOSE) {
            Log.d(App.getTag(), "Expected audio format: " + format);
        }

        mediaCodec = MediaCodecUtils.getCodecByFormat(format);
        mediaCodec.start();

        if (VERBOSE) {
            Log.d(App.getTag(), "Configured video format: " + mediaCodec.getOutputFormat());
        }

        if (listener != null) {
            listener.onPrepared(this);
        }
    }
}
