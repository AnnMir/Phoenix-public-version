package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;

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
    public Single<FileInputStream> getRecord(@NonNull String name) {
        return Single.create(emitter -> {
            try {
                Log.e(App.getTag(), "getRecord " + Thread.currentThread().getName());
                emitter.onSuccess(new FileInputStream(path + "/" + name));
            } catch (FileNotFoundException e) {
                emitter.onError(new RecordRepositoryException(
                        RecordRepositoryException.RECORD_NOT_FOUND
                ));
            }
        });
    }

    public void setListener(@NonNull LocalStorageListener listener) {
        this.listener = listener;
    }
}
