package nsu.fit.g14201.marchenko.phoenix.recording.utils;


import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import java.io.IOException;

import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;

import static nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException.NO_CODEC_FOUND;

public class MediaCodecUtils {
    private MediaCodecUtils() {}

    public static MediaCodec getCodecByFormat(MediaFormat format) throws CameraException, IOException {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String encoderName;
        if ((encoderName = mediaCodecList.findEncoderForFormat(format)) == null) {
            throw new CameraException(NO_CODEC_FOUND);
        }
        MediaCodec mediaCodec = MediaCodec.createByCodecName(encoderName);
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        return mediaCodec;
    }
}
