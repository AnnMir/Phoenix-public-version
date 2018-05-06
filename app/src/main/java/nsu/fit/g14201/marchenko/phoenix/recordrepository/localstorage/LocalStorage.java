package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;


import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepository;

public interface LocalStorage extends RecordRepository {
    String getPath();
}
