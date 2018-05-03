package nsu.fit.g14201.marchenko.phoenix.cloud.googledrive;

import com.google.android.gms.drive.DriveId;

import nsu.fit.g14201.marchenko.phoenix.cloud.RecordFolder;

public class GoogleDriveRecordFolder implements RecordFolder {
    private DriveId driveId;

    public GoogleDriveRecordFolder(DriveId driveId) {
        this.driveId = driveId;
    }

    public DriveId getDriveId() {
        return driveId;
    }
}
