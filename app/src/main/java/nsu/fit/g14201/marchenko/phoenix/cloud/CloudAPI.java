package nsu.fit.g14201.marchenko.phoenix.cloud;


import android.support.annotation.NonNull;

import java.io.FileInputStream;

public interface CloudAPI {
    void setListener(@NonNull CloudListener listener);

    void createAppFolderIfNotExists();

    void createFolder(@NonNull String name);

    void transmitFragment(FileInputStream inputStream);
}
