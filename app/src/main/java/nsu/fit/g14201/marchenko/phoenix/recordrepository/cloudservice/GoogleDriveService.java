package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice;

import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.cloud.googledrive.GoogleDriveAPI;

public class GoogleDriveService implements CloudService {
    private GoogleDriveAPI googleDriveAPI;

    public GoogleDriveService(GoogleDriveAPI googleDriveAPI) {
        this.googleDriveAPI = googleDriveAPI;
        googleDriveAPI.createAppFolderIfNotExists();
    }

    @Override
    public void createVideoRepository(@NonNull String repositoryName) {
        googleDriveAPI.createFolder(repositoryName);
    }
}
