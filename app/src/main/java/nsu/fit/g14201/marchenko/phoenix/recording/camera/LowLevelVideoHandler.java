package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import nsu.fit.g14201.marchenko.phoenix.App;

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
     * encoder, mMuxer, inputSurface, mBufferInfo, mTrackIndex, and mMuxerStarted.
     */
    public void prepareEncoder(int width, int height) throws IOException, CameraException,
            OpenGLException {
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setString(MediaFormat.KEY_FRAME_RATE, null);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        if (VERBOSE) Log.d(TAG, "Format: " + format);

        // Create a MediaCodec encoder and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.

        // If you want to have two EGL contexts - one for display, one for recording -
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
//        encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String encoderName;
        if ((encoderName = mediaCodecList.findEncoderForFormat(format)) == null) {
            throw new CameraException(NO_CODEC_FOUND);
        }
        encoder = MediaCodec.createByCodecName(encoderName);
        encoder.setCallback(mediaCodecCallback);
//        Log.d(App.getTag(), "Null: " + format == null ? "null" : "not null");
//        Map<String, Object> formatMap = format.getMap();
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = new CodecInputSurface(encoder.createInputSurface());

        inputSurface.release();
        encoder.release();

//        mEncoder.start();
//
//        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
//        // hard-coded output directory.
//        String outputPath = new File(OUTPUT_DIR,
//                "test." + width + "x" + height + ".mp4").toString();
//        Log.i(TAG, "Output file is " + outputPath);
//
//
//        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
//        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
//        // obtained from the encoder after it has started processing data.
//        //
//        // We're not actually interested in multiplexing audio.  We just want to convert
//        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
//        try {
//            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        } catch (IOException ioe) {
//            throw new RuntimeException("MediaMuxer creation failed", ioe);
//        }
//
//        mTrackIndex = -1;
//        mMuxerStarted = false;
    }
}
