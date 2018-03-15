package nsu.fit.g14201.marchenko.phoenix.recording;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import nsu.fit.g14201.marchenko.phoenix.recording.utils.SizeManager;
import nsu.fit.g14201.marchenko.phoenix.ui.views.AutoFitTextureView;


public class VideoTextureView extends AutoFitTextureView {
    private Size previewSize;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SurfaceTexture configureSurfaceTexture() {
        SurfaceTexture texture = getSurfaceTexture();
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        return texture;
    }

    public void setPreviewSize(Size previewSize) {
        this.previewSize = previewSize;
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to this TextureView.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of this TextureView is fixed.
     *
     * @param viewWidth  The width of this TextureView
     * @param viewHeight The height of this TextureView
     */
    public void configureTransform(int viewWidth, int viewHeight) {
        WindowManager windowManager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null || previewSize == null) {
            return;
        }

        int rotation = windowManager.getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        setTransform(matrix);
    }
}
