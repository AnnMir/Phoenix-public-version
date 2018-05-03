package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.support.annotation.NonNull;

import java.io.File;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;

public interface LocalStorage {
    String getPath();

    File createRecordDirectory(@NonNull String name)
            throws RecordRepositoryException;
}
