package nsu.fit.g14201.marchenko.phoenix.cloud;

public interface CloudListener {
    void onVideoFolderCreated(RecordFolder recordFolder);

    void onFailedToCreateVideoFolder(Exception e);
}
