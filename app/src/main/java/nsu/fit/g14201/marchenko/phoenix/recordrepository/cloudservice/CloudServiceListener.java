package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.network.cloud.RecordFolder;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryListener;

public interface CloudServiceListener extends RecordRepositoryListener {
    void onVideoRepositoryCreated(@NonNull CloudService cloudService, @NonNull RecordFolder repository);

    void onFailedToCreateVideoRepository(@NonNull CloudService cloudService, Exception exception);
}
