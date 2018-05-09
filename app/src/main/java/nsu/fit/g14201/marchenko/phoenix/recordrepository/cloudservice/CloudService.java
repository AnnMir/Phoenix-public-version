package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice;

import android.support.annotation.NonNull;

import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.network.cloud.RecordFolder;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepository;

public interface CloudService extends RecordRepository {
    String getName();

    void transmitFragment(@NonNull RecordFolder folder,
                          @NonNull FileInputStream inputStream,
                          @NonNull String name);
}
