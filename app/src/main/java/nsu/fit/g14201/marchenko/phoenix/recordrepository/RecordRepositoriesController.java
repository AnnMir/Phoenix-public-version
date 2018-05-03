package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.cloud.CloudListener;
import nsu.fit.g14201.marchenko.phoenix.cloud.RecordFolder;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class RecordRepositoriesController implements CloudListener {
    private LocalStorage localStorage;
    private Set<CloudService> cloudServices;
    private Map<CloudService, RecordFolder> recordFolders;
    private RecordStorageErrorListener errorListener;

    public RecordRepositoriesController(LocalStorage localStorage) {
        this.localStorage = localStorage;
        cloudServices = new HashSet<>();
        recordFolders = new HashMap<>();
    }

    public void addCloudService(CloudService cloudService) {
        cloudServices.add(cloudService);
    }

    public void setErrorListener(RecordStorageErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public String getLocalStoragePath() {
        return localStorage.getPath();
    }

    public void createVideoRepositoryLocally(@NonNull String repositoryName)
            throws RecordRepositoryException {
        localStorage.createRecordDirectory(repositoryName);
    }

    public void createVideoRepositoryRemotely(@NonNull String repositoryName) {
        for (CloudService cloudService : cloudServices) {
            cloudService.createVideoRepository(repositoryName);
        }
    }

    public FileInputStream getRecord(@NonNull String name) throws FileNotFoundException {
        return localStorage.getRecord(name);
    }

    @Override
    public void onVideoFolderCreated(RecordFolder recordFolder) {

    }

    @Override
    public void onFailedToCreateVideoFolder(Exception e) {
        Log.e(App.getTag(), e.getLocalizedMessage());
        errorListener.onError(e.getLocalizedMessage());
    }
}
