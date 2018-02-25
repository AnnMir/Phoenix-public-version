package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.view.TextureView;

public class CameraHandler {
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private String cameraId;
    private CameraStateListener listener;
    private TextureView textureView;

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice device) {
            cameraDevice = device;
            createCameraPreviewSession();
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
                listener.onCameraError(error);
            }
        }
    };

    public CameraHandler(@NonNull CameraManager cameraManager, @NonNull String cameraId,
                         CameraStateListener listener) {
        this.cameraManager = cameraManager;
        this.cameraId = cameraId;
        this.listener = listener;
    }

    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
    }

    public void openCamera() throws SecurityException, CameraAccessException {
        cameraManager.openCamera(cameraId, stateCallback, null); // FIXME threads
    }

    private void createCameraPreviewSession() {

    }
}
