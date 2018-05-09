package nsu.fit.g14201.marchenko.phoenix.transmission;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.VideoFragmentListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRemoteRepoStateListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public class PeriodicRecordRemoteTransmitter implements RecordRemoteRepoStateListener,
        VideoFragmentListener {
    private RecordReposControllerProviding recordRepositoriesController;
    private VideoFragmentPath videoFragmentPath;
    private TransmissionListener transmissionListener;

//    private List<Instant> before = new ArrayList<>();
//    private List<Instant> after = new ArrayList<>();

    PeriodicRecordRemoteTransmitter(@NonNull RecordReposControllerProviding recordRepositoriesController,
                                    @NonNull VideoFragmentPath videoFragmentPath) {
        this.recordRepositoriesController = recordRepositoriesController;
        this.videoFragmentPath = videoFragmentPath;

        recordRepositoriesController.setRemoteRepoStateListener(this);
    }

    @Override
    public void onFragmentSavedLocally(int fragmentNum) {
        new Thread() {
            @Override
            public void run() {
//                before.add(Instant.now());
                recordRepositoriesController.getRecord(
                        videoFragmentPath.getRelativeNameByFragmentNumber(fragmentNum),
                        new RecordRepositoriesController.RecordGetter() {
                    @Override
                    public void onRecordGot(FileInputStream record) {
//                        after.add(Instant.now());
                        transmitVideoFragment(record, fragmentNum);
                    }

                    @Override
                    public void onRecordNotFound() {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        Runnable reaction = () -> {
                            transmissionListener.onUnableToContinueTransmission(
                                    new TransmissionProblem(TransmissionProblem.RECORD_NOT_FOUND_LOCALLY)
                            );
                        };
                        mainHandler.post(reaction);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onFailedToCreateVideoRepository(@NonNull Exception e, @NonNull String name) {
        // TODO: Handle no internet access case
        e.printStackTrace();
        Log.e(App.getTag(), e.getLocalizedMessage());
        transmissionListener.onUnableToContinueTransmission(
                new TransmissionDetailedProblem(
                        TransmissionProblem.FAILED_TO_CREATE_VIDEO_FOLDER, name
                )
        );
    }

    void createVideoRepositories() {
        recordRepositoriesController.createVideoRepositoryRemotely(videoFragmentPath.getDirectoryName());
    }

    void setTransmissionListener(@NonNull TransmissionListener listener) {
        transmissionListener = listener;
    }

    void removeTransmissionListener() { // TODO: Use
        transmissionListener = null;
    }

    void stop() {
//        for (int i = 0; i < before.size(); i++) {
//            if (i < after.size()) {
//                Log.d(App.getTag(), "Fragment # " + i + ": " + Long.toString(
//                        after.get(i).toEpochMilli() - before.get(i).toEpochMilli())
//                );
//            } else {
//                Log.d(App.getTag(), "Fragment # " + i + ": never got");
//            }
//        }
    }

    private void transmitVideoFragment(@NonNull FileInputStream inputStream, int fragmentNum) {
        recordRepositoriesController.transmitVideo(
                inputStream,
                videoFragmentPath.getFragmentFileNameByNumber(fragmentNum)
        );
    }
}
