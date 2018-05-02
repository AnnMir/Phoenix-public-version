package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;

public interface RecordStorageErrorListener {
    void onError(@NonNull String description);
}
