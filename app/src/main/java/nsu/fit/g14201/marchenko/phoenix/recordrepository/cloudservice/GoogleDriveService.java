package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice;

import android.support.annotation.NonNull;

import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.network.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.RecordFolder;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.googledrive.GoogleDriveAPI;

public class GoogleDriveService implements CloudService {
    private GoogleDriveAPI googleDriveAPI;
    private RecordFolder recordFolder;
    private CloudServiceListener listener;

    public GoogleDriveService(GoogleDriveAPI googleDriveAPI) {
        this.googleDriveAPI = googleDriveAPI;
        googleDriveAPI.createAppFolderIfNotExists();
    }

    @Override
    public String getName() {
        return "Google Drive";
    }

    @Override
    public void createVideoRepository(@NonNull String name) {
        googleDriveAPI.createFolder(name, new CloudAPI.FolderCreationListener() {
            @Override
            public void onFolderCreated(@NonNull RecordFolder folder) {
                recordFolder = folder;
                listener.onVideoRepositoryCreated(GoogleDriveService.this, folder);
            }

            @Override
            public void onFailedToCreateFolder(@NonNull Exception exception) {
                listener.onFailedToCreateVideoRepository(GoogleDriveService.this, exception);
            }
        });
    }

    @Override
    public void getRecord(@NonNull String name) {
        // TODO
    }

    @Override
    public void transmitFragment(@NonNull RecordFolder folder,
                                 @NonNull FileInputStream inputStream,
                                 @NonNull String name) {
        googleDriveAPI.transmitFragment(folder, inputStream, name);
    }


    public void setListener(CloudServiceListener listener) {
        this.listener = listener;
    }
}
