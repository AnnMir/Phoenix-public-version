package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nsu.fit.g14201.marchenko.phoenix.recording.VideoTextureView;
import nsu.fit.g14201.marchenko.phoenix.recording.utils.SizeManager;

public class StandardCameraHandler {
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


    public StandardCameraHandler(@NonNull CameraManager cameraManager, @NonNull String cameraId,
                                 CameraStateListener listener) {
        this.cameraManager = cameraManager;
        this.cameraId = cameraId;
        this.listener = listener;
    }

    public void setTextureView(VideoTextureView textureView) {
        this.textureView = textureView;
    }

    public void openCamera(int width, int height) throws SecurityException, CameraAccessException {
        chooseSizes(width, height);
        textureView.configureTransform(width, height);
        cameraManager.openCamera(cameraId, stateCallback, null); // TODO threads
    }

    public void startRecording(String videoPath) {
        if (cameraDevice == null || !textureView.isAvailable()) {
            return;
        }

        try {
            closePreview();
            SurfaceTexture texture = textureView.configureSurfaceTexture();

            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            previewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
//            Surface recorderSurface = videoHandler.getSurface();
//            surfaces.add(recorderSurface);
//            previewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    previewSession = cameraCaptureSession;
                    updatePreview();
                    listener.onRecordingStarted();
//                    videoHandler.startRecording();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    listener.onCameraError(CameraStateListener.CAPTURE_SESSION_ERROR);
                }
            }, null); // TODO: Threads
        } catch (CameraAccessException e) {
            e.printStackTrace();
            listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
        }
    }

    public void stopRecording(String videoPath) {
//        videoHandler.stopRecording();
//        videoHandler.resetRecorder();
        listener.onRecordingFinished(videoPath);
        startPreview();
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

//        if (videoHandler != null) {
//            videoHandler.closeRecorder();
//            videoHandler = null;
//        }
    }

    private void startPreview() {
        if (cameraDevice == null || !textureView.isAvailable()) {
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
        if (cameraDevice == null) {
            return;
        }

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

    private void chooseSizes(int width, int height) throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            throw new RuntimeException("Cannot get available preview/video sizes");
        }

        Size videoSize = SizeManager.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
        Size previewSize = SizeManager.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                width, height, videoSize);

//        videoHandler.setVideoSize(videoSize);
//        videoHandler.setSensorOrientation(characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
        textureView.setPreviewSize(previewSize);

        int orientation = textureView.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
        } else {
            textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }
    }
}
