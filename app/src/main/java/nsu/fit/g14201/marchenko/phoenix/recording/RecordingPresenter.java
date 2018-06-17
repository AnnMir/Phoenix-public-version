package nsu.fit.g14201.marchenko.phoenix.recording;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaCodec;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.context.Contextual;
import nsu.fit.g14201.marchenko.phoenix.model.RecordPath;
import nsu.fit.g14201.marchenko.phoenix.model.VideoFragmentPath;
import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandlerProviding;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraStateListener;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraWrapper;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.VideoEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.CameraGLView;

public class RecordingPresenter implements RecordingContract.Presenter,
        CameraStateListener,
        MediaEncoder.MediaEncoderListener,
        Contextual {
    private final boolean VERBOSE = true;

    private final RecordingContract.View recordingView;
    private final Context context;
    private nsu.fit.g14201.marchenko.phoenix.context.Context appContext;

    private CameraWrapper backCamera;
    private CameraWrapper frontCamera;
    private CameraWrapper selectedCamera;
    private PeriodicFragmentRecorder fragmentRecorder;
    private CameraGLView cameraGLView;
    private boolean isWaitingForCameraConfiguration = false;
    private boolean isVideoRecording = false;
    private RecordingListener recordingListener;

    public RecordingPresenter(Context applicationContext, RecordingContract.View recordingView) {
        context = applicationContext;
        this.recordingView = recordingView;
        recordingView.setPresenter(this);
    }

    @Override
    public void start() {
        try {
            checkCameraHardware();
            selectedCamera = backCamera;
        } catch (CameraAccessException | CameraException e) {
            e.printStackTrace();
            recordingView.showIncorrigibleErrorDialog(
                    context.getString(R.string.camera_access_error)
                            + "\n" + context.getString(R.string.no_camera_no_app)
            );
        } catch (SecurityException e) {
            // TODO PERMISSIONS Request camera permission
        }
    }

    @Override
    public void setOutputForVideo(CameraGLView view) {
        cameraGLView = view;
        cameraGLView.setCameraWrapper(selectedCamera);
        fragmentRecorder = new PeriodicFragmentRecorder(cameraGLView, this);
        fragmentRecorder.setContext(appContext);
    }

    @Override
    public void changeRecordingState() {
        if (isVideoRecording) {
            fragmentRecorder.stop();
        } else {
            startRecording();
        }
    }

    @Override
    public void doOnResumeActions() {
        fragmentRecorder.resume();
    }

    @Override
    public void doOnPauseActions() {
        fragmentRecorder.pause();
    }

    @Override
    public void setRecordingListener(RecordingListener listener) {
        recordingListener = listener;
    }

    @Override
    public void removeRecordingListener() {
        recordingListener = null;
    }

    @Override
    public void setVideoFragmentListener(VideoFragmentListener listener) {
        fragmentRecorder.setVideoFragmentListener(listener);
    }

    @Override
    public void removeVideoFragmentListener() {
        fragmentRecorder.removeVideoFragmentListener();
    }

    @Override
    public void stop() {
        if (isVideoRecording) {
            fragmentRecorder.stop();
        }
        fragmentRecorder.removeCameraStateListener();
    }

    @Override
    public synchronized void onCameraConfigured() {
        if (isWaitingForCameraConfiguration) {
            isWaitingForCameraConfiguration = false;
            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(() -> startRecording());
        }
    }

    @Override
    public void onCameraDisconnected() {
        recordingView.showFatalErrorDialog(context.getString(R.string.disconnected_camera) +
                "\n" + context.getString(R.string.no_camera_no_app));
    }

    @Override
    public void onCameraStandardError(int error) {
        //TODO error handling

        switch (error) {
            case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                recordingView.showFatalErrorDialog(context.getString(R.string.camera_already_in_use) +
                        "\n" + context.getString(R.string.no_camera_no_app));
                break;
            case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                recordingView.showFatalErrorDialog("ERROR_CAMERA_DISABLED");
                break;
            case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                recordingView.showCorrigibleErrorDialog(context.getString(R.string.camera_error));
                break;
            case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                recordingView.showFatalErrorDialog("ERROR_CAMERA_SERVICE");
                break;
            case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                recordingView.showFatalErrorDialog("ERROR_MAX_CAMERAS_IN_USE");

        }
    }

    @Override
    public void onCameraError(int error) {
        processCameraError(error);
    }

    @Override
    public void onRecordingStarted() {
        isVideoRecording = true;

        recordingListener.recordDidStart();
        recordingView.onRecordingStarted();
    }

    @Override
    public void onRecordingFinished(String path) {
        isVideoRecording = false;

        fragmentRecorder.removeVideoFragmentListener();
        recordingView.onRecordingFinished(path);
    }

    @Override
    public void onPrepared(MediaEncoder encoder) {
        if (VERBOSE) {
            Log.d(App.getTag(), "on Prepared: encoder = " + encoder);
        }
        if (encoder instanceof VideoEncoder) {
            cameraGLView.setVideoEncoder((VideoEncoder) encoder);
        }
    }

    @Override
    public void onStopped(MediaEncoder encoder) {
        if (VERBOSE) {
            Log.d(App.getTag(), "on Stopped: encoder = " + encoder);
        }
        if (encoder instanceof VideoEncoder)
            cameraGLView.setVideoEncoder(null);
    }

    @Override
    public void onError(@NonNull MediaCodec.CodecException e) {
        e.printStackTrace();

        Log.d(App.getTag(), e.getDiagnosticInfo());
        recordingView.showIncorrigibleErrorDialog(e.getMessage());
    }

    @Override
    public void setContext(nsu.fit.g14201.marchenko.phoenix.context.Context context) {
        appContext = context;
    }

    private void checkCameraHardware() throws CameraAccessException, CameraException {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        for (String cameraId : cameraManager.getCameraIdList()) {
            Integer facing = cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.LENS_FACING);
            if (facing != null) {
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCamera = new CameraWrapper(cameraManager, cameraId, this);
                    continue;
                }
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCamera = new CameraWrapper(cameraManager, cameraId, this);
                }
            }
        }
        if (backCamera == null && frontCamera == null) {
            throw new CameraException(CameraException.NO_CAMERAS_FOUND);
        }
    }

    private void processCameraError(int error) {
        // TODO Error handling
        switch (error) {
            case CAPTURE_SESSION_ERROR:
                recordingView.showFatalErrorDialog("ON CAPTURE SESSION ERROR");
                break;
            case CAMERA_ACCESS_ERROR:
                recordingView.showFatalErrorDialog("ON CAMERA ACCESS ERROR");
        }
    }

    private void createLocalVideoRepository(@NonNull VideoFragmentPath videoFragmentPath) {
        appContext.getLocalStorage().createVideoRepository(videoFragmentPath.getDirectoryName());
    }

    private synchronized void startRecording() {
        if (cameraGLView.getVideoWidth() == 0) {
            isWaitingForCameraConfiguration = true;
            return;
        }

        try {
            VideoTitleHandlerProviding videoTitleHandler = appContext.getVideoTitleHandler();
            VideoFragmentPath videoFragmentPath = new VideoFragmentPath(
                    new RecordPath(videoTitleHandler.getNewVideoTitle()),
                    videoTitleHandler.getExtension()
            );
            createLocalVideoRepository(videoFragmentPath);
            recordingListener.recordWillStart(videoFragmentPath);
            fragmentRecorder.start(this, videoFragmentPath);
        } catch (Throwable e) {
            e.printStackTrace();
            recordingView.showIncorrigibleErrorDialog(e.getMessage());
        }
    }
}
