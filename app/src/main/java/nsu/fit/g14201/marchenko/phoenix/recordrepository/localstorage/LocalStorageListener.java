package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;


import java.io.File;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryListener;

public interface LocalStorageListener extends RecordRepositoryListener {
    void onRepositoryCreated(File repository);

    void onFailedToCreateRepository();
}
