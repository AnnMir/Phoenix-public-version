package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.lang.ref.WeakReference;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.glutils.RenderHandler;
import nsu.fit.g14201.marchenko.phoenix.recording.utils.MediaCodecUtils;

public class VideoEncoder extends MediaEncoder {
    private static final String TAG = "MediaVideoEncoder";
    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 30;
    private static final int IFRAME_INTERVAL = 5;
    private static final float BPP = 0.25f;

    private final boolean VERBOSE = true;
    private int videoWidth;
    private int videoHeight;
    private Surface inputSurface;
    private RenderHandler renderHandler;

    public VideoEncoder(MediaMuxerWrapper muxer, int videoWidth, int videoHeight,
                        MediaEncoderListener listener)
            throws MediaMuxerException {
        super(listener);
        WeakReference<MediaMuxerWrapper> weakMuxer = new WeakReference<>(muxer);
        muxer.addEncoder(this);
        this.muxer = weakMuxer;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        renderHandler = RenderHandler.createHandler(TAG);
    }

    public boolean frameAvailableSoon(final float[] tex_matrix) {
        boolean result;
        if (result = super.frameAvailableSoon())
            renderHandler.draw(tex_matrix);
        return result;
    }

    public boolean frameAvailableSoon(final float[] tex_matrix, final float[] mvp_matrix) {
        boolean result;
        if (result = super.frameAvailableSoon())
            renderHandler.draw(tex_matrix, mvp_matrix);
        return result;
    }

    @Override
    public boolean frameAvailableSoon() {
        boolean result;
        if (result = super.frameAvailableSoon())
            renderHandler.draw(null);
        return result;
    }

    public void setEglContext(final EGLContext shared_context, final int tex_id) {
        renderHandler.setEglContext(shared_context, tex_id, inputSurface, true);
    }

    @Override
    public void prepare() throws CameraException, IOException {
        trackIndex = -1;
        muxerStarted = EOS = false;

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, videoWidth, videoHeight);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, calculateBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        if (VERBOSE) {
            Log.d(App.getTag(), "Expected video format: " + format);
        }

        mediaCodec = MediaCodecUtils.getCodecByFormat(format);
//        encoder.setCallback(mediaCodecCallback);
        inputSurface = mediaCodec.createInputSurface();

        if (VERBOSE) {
            Log.d(App.getTag(), "Configured video format: " + mediaCodec.getOutputFormat());
        }

        mediaCodec.start();

        if (listener != null) {
            listener.onPrepared(this);
        }
    }

    @Override
    protected void signalEndOfInputStream() {
        if (VERBOSE) {
            Log.d(App.getTag(), "Sending EOS to encoder");
        }
		mediaCodec.signalEndOfInputStream();
    }

    private int calculateBitRate() {
        final int bitrate = (int) (BPP * FRAME_RATE * videoWidth * videoHeight);
        Log.d(App.getTag(), String.format("Bitrate = %5.2f[Mbps]", bitrate / 1024f / 1024f));
        return bitrate;
    }
}
