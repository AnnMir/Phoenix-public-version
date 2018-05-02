package nsu.fit.g14201.marchenko.phoenix.cloud;


import android.support.annotation.NonNull;

public interface CloudAPI {
    void setListener(@NonNull CloudErrorListener listener);

    void createAppFolderIfNotExists();

    void createFolder(@NonNull String name);
}
