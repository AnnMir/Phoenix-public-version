package nsu.fit.g14201.marchenko.phoenix.recording.camera;

public interface CameraStateListener {
    void onCameraDisconnected();
    void onCameraStandardError(int error);
    void onCameraError(int error);

    int CAMERA_ACCESS_ERROR = 0;
    int CAPTURE_SESSION_ERROR = 1;
}
