package nsu.fit.g14201.marchenko.phoenix.transmission;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.VideoFragmentListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRemoteRepoStateListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class PeriodicRecordRemoteTransmitter implements RecordRemoteRepoStateListener,
        VideoFragmentListener {
    private static final int THREAD_NUM = 4;

    private LocalStorage localStorage;
    private RemoteReposControllerProviding recordRepositoriesController;
    private Scheduler scheduler;
    private VideoFragmentPath videoFragmentPath;
    private TransmissionListener transmissionListener;

//    private List<Instant> before = new ArrayList<>();
//    private List<Instant> after = new ArrayList<>();

    PeriodicRecordRemoteTransmitter(@NonNull LocalStorage localStorage,
                                    @NonNull RemoteReposControllerProviding recordRepositoriesController,
                                    @NonNull VideoFragmentPath videoFragmentPath) {
        this.localStorage = localStorage;
        this.recordRepositoriesController = recordRepositoriesController;
        this.videoFragmentPath = videoFragmentPath;

        recordRepositoriesController.setRemoteRepoStateListener(this);
        scheduler = Schedulers.from(Executors.newFixedThreadPool(THREAD_NUM));
    }

    @Override
    public void onFragmentSavedLocally(int fragmentNum) {
        final Disposable subscribe = localStorage.getRecord(
                videoFragmentPath.getRelativeNameByFragmentNumber(fragmentNum))
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .flatMapCompletable(record ->
                        recordRepositoriesController.transmitVideo(
                                record, videoFragmentPath.getFragmentFileNameByNumber(fragmentNum)
                        ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(App.getTag(), "Fragment " + fragmentNum + " upload started");
                }, throwable -> {
                    throwable.printStackTrace();
                    int problem = TransmissionProblem.UNKNOWN;
                    if (throwable instanceof RecordRepositoryException) {
                        switch (((RecordRepositoryException) throwable).getReason()) {
                            case RecordRepositoryException.RECORD_NOT_FOUND:
                                problem = TransmissionProblem.RECORD_NOT_FOUND_LOCALLY;
                        }
                    }
                    transmissionListener.onUnableToContinueTransmission(new TransmissionProblem(problem));
                    Log.e(App.getTag(), "Didn't manage to send fragment " + fragmentNum);
                });
    }

    @Override
    public void onLastFragmentSaved(int fragmentNum) {
        onFragmentSavedLocally(fragmentNum);
        stop();
        transmissionListener.onTransmissionFinished();
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
        recordRepositoriesController.createVideoRepository(
                videoFragmentPath.getDirectoryName()
        );
    }

    void setTransmissionListener(@NonNull TransmissionListener listener) {
        transmissionListener = listener;
    }

    void removeTransmissionListener() {
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
}
