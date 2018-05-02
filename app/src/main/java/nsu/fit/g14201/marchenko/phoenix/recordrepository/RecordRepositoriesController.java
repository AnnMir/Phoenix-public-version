package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.cloud.CloudErrorListener;

public class RecordRepositoriesController implements CloudErrorListener {
    private Set<RecordRepository> repositories;
    private RecordStorageErrorListener errorListener;

    public RecordRepositoriesController() {
        repositories = new HashSet<>();
    }

    public void addRepository(RecordRepository recordRepository) {
        repositories.add(recordRepository);
    }

    public void setErrorListener(RecordStorageErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public void createVideoRepository(@NonNull String repositoryName) {
        for (RecordRepository repository : repositories) {
            repository.createVideoRepository(repositoryName);
        }
    }

    @Override
    public void onFailedToCreateVideoFolder(Exception e) {
        Log.e(App.getTag(), e.getLocalizedMessage());
        errorListener.onError(e.getLocalizedMessage());
    }
}
