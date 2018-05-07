package nsu.fit.g14201.marchenko.phoenix.network.cloud.googledrive;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;

import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;

public class GoogleDriveAPI implements CloudAPI {
    private static final String FOLDER_NAME = App.getAppName();

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private DriveId appFolderId;
    private DriveFolder rootFolder;

    public GoogleDriveAPI(Context context) throws SignInException {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        if (signInAccount == null) {
            throw new SignInException("User has not logged in");
        }
        // Use the last signed in account here since it already have a Drive scope.
        driveClient = Drive.getDriveClient(context, signInAccount);
        // Build a drive resource client.
        driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
    }

    // TODO: Move listeners to background
    // TODO: Add outer listeners
    @Override
    public void createAppFolderIfNotExists() {
        driveResourceClient
                .getRootFolder()
                .continueWithTask(task -> {
                    Query query = new Query.Builder()
                        .addFilter(Filters.and(
                                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"),
                                Filters.eq(SearchableField.TITLE, FOLDER_NAME),
                                Filters.eq(SearchableField.TRASHED, false)))
                        .build();
                    rootFolder = task.getResult();
                    return driveResourceClient.queryChildren(rootFolder, query);

                })
                .addOnSuccessListener(metadataBuffer -> {
                    for (Metadata metadata : metadataBuffer) {
                        if (metadata.getTitle().equals(FOLDER_NAME)) {
                            Log.d(App.getTag(), "Folder already exists");
                            appFolderId = metadata.getDriveId();
                            return;
                        }
                    }
                    createAppFolder()
                        .addOnFailureListener(e -> {
                            Log.e(App.getTag(), "Failed to create app folder");
                            e.printStackTrace();
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(App.getTag(), "Failed to create app folder");
                    e.printStackTrace();
                });
    }

    @Override
    public void createFolder(@NonNull String name, FolderCreationListener listener) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setMimeType(DriveFolder.MIME_TYPE)
                .build();
        driveResourceClient.createFolder(appFolderId.asDriveFolder(), changeSet)
                .addOnSuccessListener(
                        driveFolder -> listener.onFolderCreated(
                                new GoogleDriveRecordFolder(driveFolder.getDriveId())
                        )
                )
                .addOnFailureListener(listener::onFailedToCreateFolder);
    }

    @Override
    public void transmitFragment(FileInputStream inputStream) {

    }

    private Task<DriveFolder> createAppFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(App.getAppName())
                .setMimeType(DriveFolder.MIME_TYPE)
                .build();
        return driveResourceClient.createFolder(rootFolder, changeSet);
    }
}