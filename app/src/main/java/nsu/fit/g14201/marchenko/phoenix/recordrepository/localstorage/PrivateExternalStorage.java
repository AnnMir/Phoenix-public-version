package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PrivateExternalStorage implements LocalStorage {
    private final File path;
    private LocalStorageListener listener;

    public PrivateExternalStorage(@NonNull Context context) {
        path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
    }

    @Override
    public void createVideoRepository(@NonNull String name) {
        File directory = new File(path, name);
        if (!directory.mkdirs()) {
            listener.onFailedToCreateRepository();
        }

        listener.onRepositoryCreated(directory);
    }

    @Override
    public String getPath() {
        return path.getAbsolutePath() + "/";
    }

    @Override
    public void getRecord(@NonNull String name, @NonNull RecordGetter recordGetter) {
        try {
            recordGetter.onRecordGot(new FileInputStream(path + "/" + name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            recordGetter.onRecordNotFound();
        }
    }

    public void setListener(@NonNull LocalStorageListener listener) {
        this.listener = listener;
    }
}
