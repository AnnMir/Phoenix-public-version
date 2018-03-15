package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.TextureView;

import java.util.Collections;

import nsu.fit.g14201.marchenko.phoenix.recording.VideoTextureView;

public class CameraHandler {
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private String cameraId;
    private CameraStateListener listener;
    private VideoTextureView textureView;
    private CameraCaptureSession previewSession;
    private CaptureRequest.Builder previewBuilder;

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice device) {
            cameraDevice = device;
            startPreview();
            if (textureView != null) {
                textureView.configureTransform(textureView.getWidth(), textureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice device) {
            device.close();
            cameraDevice = null;
            if (listener != null) {
                listener.onCameraDisconnected();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice device, int error) {
            device.close();
            cameraDevice = null;
            if (listener != null) {
                listener.onCameraStandardError(error);
            }
        }
    };
    private final TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            try {
                openCamera(width, height);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            textureView.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };


    public CameraHandler(@NonNull CameraManager cameraManager, @NonNull String cameraId,
                         CameraStateListener listener) {
        this.cameraManager = cameraManager;
        this.cameraId = cameraId;
        this.listener = listener;
    }

    public void setTextureView(VideoTextureView textureView) {
        this.textureView = textureView;
    }

    public void openCamera(int width, int height) throws SecurityException, CameraAccessException {
        textureView.chooseSizes(width, height, cameraManager.getCameraCharacteristics(cameraId));
        textureView.configureTransform(width, height);
        cameraManager.openCamera(cameraId, stateCallback, null); // TODO threads
    }

    public void resumeCameraWork() throws CameraAccessException {
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    public void closeCamera() {
        closePreview();

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void startPreview() {
        if (!textureView.isAvailable()) {
            return;
        }

        SurfaceTexture texture = textureView.configureSurfaceTexture();
        assert texture != null;
        Surface previewSurface = new Surface(texture);

        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(
                    Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }
                            previewSession = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            listener.onCameraError(CameraStateListener.CAPTURE_SESSION_ERROR);
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
            listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        try {
            previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            previewSession.setRepeatingRequest(previewBuilder.build(), null, null);
            // TODO: Threads
        } catch (CameraAccessException e) {
            e.printStackTrace();
            listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
        }
    }

    private void closePreview() {
        if (previewSession != null) {
            previewSession.close();
            previewSession = null;
        }
    }
}
