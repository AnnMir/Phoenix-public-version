package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraStateListener;


public class CameraWrapper {
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private String cameraId;
    private CameraStateListener listener;
    private SurfaceTexture surfaceTexture;
    private CameraCaptureSession previewSession;
    private CaptureRequest.Builder previewBuilder;

    private Semaphore startPreviewSemaphore = new Semaphore(1);

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice device) {
            cameraDevice = device;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice device) {
            device.close();
            cameraDevice = null;
            startPreviewSemaphore.release();

            if (listener != null) {
                listener.onCameraDisconnected();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice device, int error) {
            device.close();
            cameraDevice = null;
            startPreviewSemaphore.release();

            if (listener != null) {
                listener.onCameraStandardError(error);
            }
        }
    };
//    private final TextureView.SurfaceTextureListener surfaceTextureListener
//            = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
//            try {
//                openCamera(width, height);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//                listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
//            }
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
//            textureView.configureTransform(width, height);
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
//        }
//    };


    public CameraWrapper(@NonNull CameraManager cameraManager, @NonNull String cameraId,
                         CameraStateListener listener) {
        this.cameraManager = cameraManager;
        this.cameraId = cameraId;
        this.listener = listener;
    }

    void configureCamera(int width, int height, CameraGLView cameraGLView)
            throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            throw new RuntimeException("Cannot get available preview/video sizes");
        }

        cameraGLView.previewSize = getClosestSupportedSize(map.getOutputSizes(SurfaceTexture.class),
                width, height);
        setRotation(cameraGLView, characteristics);

        // TODO: fps
//        Range<Integer>[] supportedFpsRange =
//                characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
//        previewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, bestFPSRange);
    }

    void setPreviewTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    void openCamera()
            throws SecurityException,
            CameraAccessException {
        if (!startPreviewSemaphore.tryAcquire()) {
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, null);
    }

    public void startRecording(String videoPath) {
        if (cameraDevice == null /*|| !textureView.isAvailable()*/) {
            return;
        }

//        try {
//            stopPreview();
//            SurfaceTexture texture = textureView.configureSurfaceTexture();
//
//            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            List<Surface> surfaces = new ArrayList<>();
//
//            // Set up Surface for the camera preview
//            Surface previewSurface = new Surface(texture);
//            surfaces.add(previewSurface);
//            previewBuilder.addTarget(previewSurface);
//
//            // Set up Surface for the MediaRecorder
////            Surface recorderSurface = videoHandler.getSurface();
////            surfaces.add(recorderSurface);
////            previewBuilder.addTarget(recorderSurface);
//
//            // Start a capture session
//            // Once the session starts, we can update the UI and start recording
//            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
//
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    previewSession = cameraCaptureSession;
//                    updatePreview();
//                    listener.onRecordingStarted();
////                    videoHandler.startRecording();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    listener.onCameraError(CameraStateListener.CAPTURE_SESSION_ERROR);
//                }
//            }, null); // TODO: Threads
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//            listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
//        }
    }

    public void stopRecording(String videoPath) {
        listener.onRecordingFinished(videoPath);
        startPreview();
    }

    public void resumeCameraWork() throws CameraAccessException {
//        if (textureView.isAvailable()) {
//            openCamera(textureView.getWidth(), textureView.getHeight());
//        } else {
//            textureView.setSurfaceTextureListener(surfaceTextureListener);
//        }
    }

    void closeCamera() {
        stopPreview();

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        Log.d(App.getTag(), "Camera closed");
    }

    private void startPreview() {
        if (cameraDevice == null) {
            startPreviewSemaphore.release();
            return;
        }

        Surface previewSurface = new Surface(surfaceTexture);

        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            previewBuilder.addTarget(previewSurface);
            cameraDevice.createCaptureSession(
                    Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            startPreviewSemaphore.release();

                            if (cameraDevice == null) {
                                return;
                            }
                            previewSession = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            startPreviewSemaphore.release();
                            listener.onCameraError(CameraStateListener.CAPTURE_SESSION_ERROR);
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
            startPreviewSemaphore.release();
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
            previewSession.setRepeatingRequest(previewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            listener.onCameraError(CameraStateListener.CAMERA_ACCESS_ERROR);
        }
    }

    private void stopPreview() {
        if (previewSession != null) {
            previewSession.close();
            previewSession = null;
        }
    }

    /**
     * Rotate preview screen according to the device orientation
     */
    private void setRotation(CameraGLView cameraGLView, CameraCharacteristics characteristics) {
        if (cameraGLView == null) return;

        Display display = ((WindowManager)cameraGLView.getContext()
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        // Get whether the camera is front or back camera
        int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                degrees = (orientation + degrees) % 360;
                degrees = (360 - degrees) % 360;  // reverse
                break;
            case CameraCharacteristics.LENS_FACING_BACK:
                degrees = (orientation - degrees + 360) % 360;

        }
        cameraGLView.rotation = degrees;
    }

    private static Size getClosestSupportedSize(Size[] supportedSizes, int requestedWidth,
                                                int requestedHeight) {
        return Collections.min(Arrays.asList(supportedSizes), new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return diff(lhs) - diff(rhs);
            }

            private int diff(Size size) {
                return Math.abs(requestedWidth - size.getWidth())
                        + Math.abs(requestedHeight - size.getHeight());
            }
        });
    }
}
