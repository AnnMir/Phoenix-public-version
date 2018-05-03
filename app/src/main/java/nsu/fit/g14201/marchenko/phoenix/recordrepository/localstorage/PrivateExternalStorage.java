package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;

public class PrivateExternalStorage implements LocalStorage {
    private final File path;

    public PrivateExternalStorage(@NonNull Context context) {
        path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
    }

    @Override
    public String getPath() {
        return path.getAbsolutePath() + "/";
    }

    @Override
    public File createRecordDirectory(@NonNull String name)
            throws RecordRepositoryException {
        File directory = new File(path, name);
        if (!directory.mkdirs()) {
            throw new RecordRepositoryException(RecordRepositoryException.DIRECTORY_CREATION_ERROR);
        }

        return directory;
    }

    public FileInputStream getRecord(@NonNull String name) throws FileNotFoundException {
        return new FileInputStream(path + "/" + name);
    }
}
