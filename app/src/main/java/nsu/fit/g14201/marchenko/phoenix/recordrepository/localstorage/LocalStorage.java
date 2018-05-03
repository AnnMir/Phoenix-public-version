package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;

public interface LocalStorage {
    String getPath();

    File createRecordDirectory(@NonNull String name)
            throws RecordRepositoryException;

    FileInputStream getRecord(@NonNull String name) throws FileNotFoundException;
}
