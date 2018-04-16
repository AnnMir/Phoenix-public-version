package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaMuxerWrapper;

import static nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException.NO_CODEC_FOUND;

public class LowLevelVideoHandler {
    private static final String TAG = "SAFARI";
    private static final boolean VERBOSE = true;           // lots of logging

    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int BIT_RATE = 6000000;      // bits/sec // TODO: Increase
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames


    private MediaCodec encoder;
    private MediaCodec.BufferInfo bufferInfo;
    private CodecInputSurface inputSurface;
    private MediaMuxerWrapper muxerWrapper;
    private MediaCodec.Callback mediaCodecCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i,
                                            @NonNull MediaCodec.BufferInfo bufferInfo) {

        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec,
                                          @NonNull MediaFormat mediaFormat) {

        }
    };

    /**
     * Configures encoder and muxer state, and prepares the input Surface.  Initializes
     * encoder, muxerWrapper, inputSurface and bufferInfo.
     */
    public void prepareEncoder(int width, int height, String outputPath) throws IOException,
            CameraException, LowLevelRecordingException {
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        if (VERBOSE) Log.d(TAG, "Assumed video format: " + format);

        // Create a MediaCodec encoder and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.

        // If you want to have two EGL contexts - one for display, one for recording -
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument. // TODO: OpenGL
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String encoderName;
        if ((encoderName = mediaCodecList.findEncoderForFormat(format)) == null) {
            throw new CameraException(NO_CODEC_FOUND);
        }
        encoder = MediaCodec.createByCodecName(encoderName);
        encoder.setCallback(mediaCodecCallback);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = new CodecInputSurface(encoder.createInputSurface());

        if (VERBOSE) Log.d(TAG, "Configured video format: " + encoder.getOutputFormat());

        encoder.start();

        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.

        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        // FIXME: No, we're interesting!

        muxerWrapper = new MediaMuxerWrapper(outputPath, null);

        inputSurface.release();
        encoder.release();
    }
}
