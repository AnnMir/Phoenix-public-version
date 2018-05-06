package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.RecordFolder;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudServiceListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorageListener;

public class RecordRepositoriesController implements LocalStorageListener, CloudServiceListener {
    private LocalStorage localStorage;
    private List<CloudService> cloudServices;
    private Map<CloudService, RecordFolder> recordFolders;

    private RecordRemoteRepoStateListener remoteRepoListener;
    private RecordLocalRepoStateListener localRepoListener; // TODO local repositories listener

    private RecordGetter recordGetter = null;

    public RecordRepositoriesController(LocalStorage localStorage) {
        this.localStorage = localStorage;
        cloudServices = new ArrayList<>();
        recordFolders = new HashMap<>();
    }

    public void addCloudService(CloudService cloudService) {
        cloudServices.add(cloudService);
    }

    public void setLocalRepoStateListener(RecordLocalRepoStateListener localRepoListener) {
        this.localRepoListener = localRepoListener;
    }

    public void setRemoteRepoStateListener(RecordRemoteRepoStateListener remoteRepoListener) {
        this.remoteRepoListener = remoteRepoListener;
    }

    public String getLocalStoragePath() {
        return localStorage.getPath();
    }

    public void createVideoRepositoryLocally(@NonNull String repositoryName) {
        localStorage.createVideoRepository(repositoryName);
    }

    public void createVideoRepositoryRemotely(@NonNull String repositoryName) {
        for (CloudService cloudService : cloudServices) {
            cloudService.createVideoRepository(repositoryName);
        }
    }

    public void getRecord(@NonNull String name, @NonNull RecordGetter recordGetter) {
        localStorage.getRecord(name);
        this.recordGetter = recordGetter;
    }

    // LocalStorageListener

    @Override
    public void onRepositoryCreated(File repository) {
        Log.d(App.getTag(), "Video repository created locally: " + repository.getName());
    }

    @Override
    public void onFailedToCreateRepository() {
        // TODO WAITING FOR LOCAL REPO STATE LISTENER
    }

    @Override
    public void onRecordGot(FileInputStream record) {
        if (recordGetter != null) {
            recordGetter.onRecordGot(record);
            recordGetter = null;
        }
    }

    @Override
    public void onRecordNotFound() {
        if (recordGetter != null) {
            recordGetter.onRecordNotFound();
            recordGetter = null;
        }
    }

    // CloudServiceListener

    @Override
    public void onVideoRepositoryCreated(@NonNull CloudService cloudService,
                                         @NonNull RecordFolder repository) {
        Log.d(App.getTag(), "Video repository created on " + cloudService.getName());
        recordFolders.put(cloudService, repository);
    }

    @Override
    public void onFailedToCreateVideoRepository(@NonNull CloudService cloudService, Exception exception) {
        remoteRepoListener.onFailedToCreateVideoRepository(exception, cloudService.getName());
    }

    public interface RecordGetter {
        void onRecordGot(FileInputStream record);

        void onRecordNotFound();
    }
}
