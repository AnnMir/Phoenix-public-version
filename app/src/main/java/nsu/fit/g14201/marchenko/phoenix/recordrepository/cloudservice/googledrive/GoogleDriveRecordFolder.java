package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive;



import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.RecordFolder;

public class GoogleDriveRecordFolder implements RecordFolder {
    private String driveId;

    public GoogleDriveRecordFolder(String driveId) {
        this.driveId = driveId;
    }

    public String getDriveId() {
        return driveId;
    }
}
