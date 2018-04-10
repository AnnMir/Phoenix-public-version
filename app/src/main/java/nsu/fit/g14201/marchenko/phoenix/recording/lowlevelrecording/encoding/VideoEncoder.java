package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


public class VideoEncoder extends MediaEncoder {
    public boolean frameAvailableSoon(final float[] texMatrix, final float[] mvpMatrix) {
//        boolean result;
//        if (result = super.frameAvailableSoon())
//            mRenderHandler.draw(texMatrix, mvpMatrix);
//        return result;
        return true;
    }
}
