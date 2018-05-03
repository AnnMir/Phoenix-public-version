package nsu.fit.g14201.marchenko.phoenix.recording;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public class PeriodicRecordTransmitter implements VideoFragmentListener {
    private RecordRepositoriesController recordRepositoriesController;
    private VideoFragmentPath videoFragmentPath;

    public PeriodicRecordTransmitter(RecordRepositoriesController recordRepositoriesController) {
        this.recordRepositoriesController = recordRepositoriesController;
    }

    @Override
    public void recordWillStart(VideoFragmentPath videoFragmentPath) throws RecordRepositoryException {
        this.videoFragmentPath = videoFragmentPath;
        recordRepositoriesController.createVideoRepositoryLocally(videoFragmentPath.getDirectoryName());
    }

    @Override
    public void recordDidStart() {
        recordRepositoriesController.createVideoRepositoryRemotely(videoFragmentPath.getDirectoryName());
    }

    @Override
    public void onFragmentSavedLocally() {

    }
}
