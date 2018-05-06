package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;


import java.io.File;
import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryListener;

public interface LocalStorageListener extends RecordRepositoryListener {
    void onRepositoryCreated(File repository);

    void onFailedToCreateRepository();

    void onRecordGot(FileInputStream record);

    void onRecordNotFound();
}
