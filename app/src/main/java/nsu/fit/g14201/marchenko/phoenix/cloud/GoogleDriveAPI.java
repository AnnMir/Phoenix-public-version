package nsu.fit.g14201.marchenko.phoenix.cloud;


import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;

public class GoogleDriveAPI implements CloudAPI {
    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;

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

    @Override
    public void createAppFolderIfNotExists() {
        // TODO NEXT: Add checkup if folder exits
        // TODO: Move to background
        // TODO: Add outer listeners
        driveResourceClient
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(App.getAppName())
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .build();
                    return driveResourceClient.createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(driveFolder -> Log.d(App.getTag(), "Created app folder"))
                .addOnFailureListener(e -> {
                    Log.d(App.getTag(), "Failed to create add folder");
                    e.printStackTrace();
                });
    }
}
