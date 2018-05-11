package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;

import java.io.FileInputStream;


public interface RecordRepository {
    void createVideoRepository(@NonNull String name);

    void getRecord(@NonNull String name, @NonNull RecordGetter recordGetter);

    interface RecordGetter {
        void onRecordGot(FileInputStream record);

        void onRecordNotFound();
    }
}
