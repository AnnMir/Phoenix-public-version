package nsu.fit.g14201.marchenko.phoenix.recording.camera;


public interface CameraStateListener {
    void onCameraDisconnected();
    void onCameraError(int error);
}
