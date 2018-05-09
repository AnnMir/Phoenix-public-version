package nsu.fit.g14201.marchenko.phoenix.network.cloud;


import android.support.annotation.NonNull;

import java.io.FileInputStream;

public interface CloudAPI {
    void createAppFolderIfNotExists();

    void createFolder(@NonNull String name, FolderCreationListener listener);

    interface FolderCreationListener {
        void onFolderCreated(@NonNull RecordFolder folder);

        void onFailedToCreateFolder(@NonNull Exception exception);
    }

    void transmitFragment(@NonNull RecordFolder folder,
                          @NonNull FileInputStream inputStream,
                          @NonNull String name);
}
