package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.RecordFolder;

public class GoogleDriveService implements CloudService {
    private static final String FOLDER_NAME = App.getAppName();

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private DriveId appFolderId;
    private DriveFolder rootFolder;

    public GoogleDriveService(Context context) throws SignInException {
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
    public Single<FileInputStream> getRecord(@NonNull String name) {
        // TODO
        return null;
    }

    @Override
    public Observable<Record> getRecords() {
        return Observable.create(emitter -> {
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TRASHED, false))
                    .build();
            Task<MetadataBuffer> queryTask = driveResourceClient.queryChildren(appFolderId.asDriveFolder(), query);
            queryTask
                    .addOnSuccessListener(Runnable::run, metadataBuffer -> {
                        for (Metadata metadata : metadataBuffer) {
                            String title = metadata.getTitle();
                            Date dateTime = metadata.getCreatedDate();
                            emitter.onNext(new Record(title, dateTime));
                        }
                        metadataBuffer.release();
                        emitter.onComplete();
                    })
                    .addOnFailureListener(Runnable::run, e -> emitter.onError(e));
        });
    }

    @Override
    public Single<RecordFolder> createVideoRepository(@NonNull String name) {
        return Single.create(emitter -> {
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(name)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .build();
            driveResourceClient.createFolder(appFolderId.asDriveFolder(), changeSet)
                    .addOnSuccessListener(Runnable::run,
                            driveFolder -> {
                                emitter.onSuccess(new GoogleDriveRecordFolder(driveFolder.getDriveId()));
                            }
                    )
                    .addOnFailureListener(Runnable::run, emitter::onError);
        });
    }

    @Override
    public String getName() {
        return "Google Drive";
    }

    @Override
    public Completable createAppFolderIfNotExists() {
        return Completable.create((CompletableEmitter emitter) -> {
            driveResourceClient
                    .getRootFolder()
                    .continueWithTask(Runnable::run, task -> {
                        Query query = new Query.Builder()
                                .addFilter(Filters.and(
                                        Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"),
                                        Filters.eq(SearchableField.TITLE, FOLDER_NAME),
                                        Filters.eq(SearchableField.TRASHED, false)))
                                .build();
                        rootFolder = task.getResult();
                        return driveResourceClient.queryChildren(rootFolder, query);

                    })
                    .addOnSuccessListener(Runnable::run, metadataBuffer -> {
                        for (Metadata metadata : metadataBuffer) {
                            if (metadata.getTitle().equals(FOLDER_NAME)) {
                                Log.d(App.getTag(), "Folder already exists");
                                appFolderId = metadata.getDriveId();
                                emitter.onComplete();
                                return;
                            }
                        }
                        createAppFolder()
                                .addOnSuccessListener(Runnable::run, buffer -> emitter.onComplete())
                                .addOnFailureListener(Runnable::run, e -> {
                                    Log.e(App.getTag(), "Failed to create app folder");
                                    emitter.onError(e);
                                });
                    })
                    .addOnFailureListener(Runnable::run, e -> {
                        Log.e(App.getTag(), "Failed to create app folder");
                        emitter.onError(e);
                    });
        });
    }

    @Override
    public Completable transmitFragment(@NonNull RecordFolder folder,
                                        @NonNull FileInputStream inputStream,
                                        @NonNull String name) {
        return Completable.create((CompletableEmitter emitter) -> {
//            Log.e(App.getTag(), "Transmission 1 " + Thread.currentThread().getName());
            Task<DriveContents> createContentsTask = driveResourceClient.createContents();
            createContentsTask.continueWithTask(Runnable::run, task -> {
                DriveFolder videoFolder = ((GoogleDriveRecordFolder) folder)
                        .getDriveId().asDriveFolder();
                DriveContents contents = createContentsTask.getResult();
                OutputStream outputStream = contents.getOutputStream();

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int num;
                    while ((num = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, num);
                    }
                    outputStream.write(baos.toByteArray());
                } finally {
                    inputStream.close();
                }
//                Log.e(App.getTag(), "Transmission 2 " + Thread.currentThread().getName());

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(name)
                        .setMimeType("video/mp4")
                        .build();

                return driveResourceClient.createFile(videoFolder, changeSet, contents);
            })
                    .addOnSuccessListener(Runnable::run, driveFile -> emitter.onComplete())
                    .addOnFailureListener(Runnable::run, emitter::onError);
        });
    }

    private Task<DriveFolder> createAppFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(App.getAppName())
                .setMimeType(DriveFolder.MIME_TYPE)
                .build();
        return driveResourceClient.createFolder(rootFolder, changeSet);
    }
}
