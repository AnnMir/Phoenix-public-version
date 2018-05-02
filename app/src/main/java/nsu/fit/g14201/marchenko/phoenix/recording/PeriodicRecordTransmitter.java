package nsu.fit.g14201.marchenko.phoenix.recording;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;

public class PeriodicRecordTransmitter implements VideoFragmentListener {
    private RecordRepositoriesController recordRepositoriesController;
    private VideoFragmentPath videoFragmentPath;

    public PeriodicRecordTransmitter(RecordRepositoriesController recordRepositoriesController) {
        this.recordRepositoriesController = recordRepositoriesController;
    }

    @Override
    public void onRecordStarted(VideoFragmentPath videoFragmentPath) {
        this.videoFragmentPath = videoFragmentPath;
        recordRepositoriesController.createVideoRepository(videoFragmentPath.getShortDirectoryPath());
    }
}
