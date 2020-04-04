package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.RecordFolder;

public class GoogleDriveService implements CloudService{
    private static final String FOLDER_NAME = App.getAppName();
    private GoogleAccountCredential credential;
    private String rootFolder;
    private Drive service;

    public GoogleDriveService(Context context){
        credential = GoogleUserConnection.getInstance().getCredential();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Completable createAppFolderIfNotExists() {
        //mime-type of folder application/vnd.google-apps.folder
        return Completable.create((CompletableEmitter emitter) -> {
                    try {
                        service = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("Phoenix")
                                .build();
                        //rootFolder = service.files().get("root").setFields("id").execute().getId();
                        //Log.i(App.getTag(),rootFolder);
                        URL url = new URL("https://www.googleapis.com/drive/v3/files/root?fields=id");
                        HttpURLConnection request = (HttpURLConnection) url.openConnection();
                        request.setRequestMethod("GET");
                        request.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                        request.setRequestProperty("Accept", "application/json");
                        request.setDoInput(true);
                        request.connect();

                        String jsonString;
                        if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream response = request.getInputStream();
                            jsonString = convertStreamToString(response);
                            JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();
                            rootFolder = obj.get("id").getAsString();
                            //rootFolder = request.getHeaderField("id");
                            //emitter.onComplete();
                            //Log.i(App.getTag(), rootFolder);

                            URL url1 = new URL("https://www.googleapis.com/drive/v3/files?fields=files&q=mimeType='application/vnd.google-apps.folder'&name="+App.getAppName());
                            HttpURLConnection request1 = (HttpURLConnection) url1.openConnection();
                            request1.setRequestMethod("GET");
                            request1.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                            request1.setRequestProperty("Accept", "application/json");
                            request1.setDoInput(true);
                            request1.connect();
                            if(request1.getResponseCode() == HttpURLConnection.HTTP_OK){
                                String fileId = request1.getHeaderField("files");
                            }
                            Log.i(App.getTag(), request1.getResponseCode()+ " " + request1.getResponseMessage());
                        }
                        Log.i(App.getTag(), request.getResponseCode()+" "+request.getResponseMessage());
                    } catch (GoogleAuthException | IOException e) {
                           emitter.onError(e);
                           e.printStackTrace();
                       }

                        createAppFolder();
        });

    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private File createAppFolder() {
        File file = null;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(App.getAppName());
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /*Completable createAppFolderIfNotExists() {
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
    }*/
/*
    private Task<DriveFolder> createAppFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(App.getAppName())
                .setMimeType(DriveFolder.MIME_TYPE)
                .build();
        return driveResourceClient.createFolder(rootFolder, changeSet);
    }
}*/

    @Override
    public Single<RecordFolder> createVideoRepository(@NonNull String name) {
        return null;
    }

    @Override
    public Completable transmitFragment(@NonNull RecordFolder folder, @NonNull FileInputStream inputStream, @NonNull String name) {
        HttpURLConnection request = null;
        String sessionUri = "";
        try {
            //String request = "POST /upload/drive/v3/files?uploadType=resumable HTTP/1.1 Host: www.googleapis.com Authorization: Bearer your_auth_token Content-Length: 38 Content-Type: application/json; charset=UTF-8 X-Upload-Content-Type: image/jpeg X-Upload-Content-Length: 2000000 { \"name\": \"My File\" }";
            URL url = new URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable");
            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("POST");
            request.setDoInput(true);
            request.setDoOutput(true);
            request.setRequestProperty("Authorization", "Bearer " + credential.getToken());
            request.setRequestProperty("X-Upload-Content-Type", "video/mp4");
            //request.setRequestProperty("X-Upload-Content-Length", String.format(Locale.ENGLISH, "%d", inputStream.length()));
            //Log.i(App.getTag(), String.format(Locale.ENGLISH, "%d", file.length()));
            request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String body = "{\"name\": \"" + name + "\"}";
            request.setRequestProperty("Content-Length", String.format(Locale.ENGLISH, "%d", body.getBytes().length));
            OutputStream outputStream = request.getOutputStream();
            outputStream.write(body.getBytes());

            outputStream.flush();
            outputStream.close();
            request.connect();
            if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
                sessionUri = request.getHeaderField("location");
                Log.i(App.getTag(), sessionUri);
            }


            //simple resumable upload
            URL url1 = new URL(sessionUri);
            request = (HttpURLConnection) url1.openConnection();
            request.setRequestMethod("PUT");
            request.setDoOutput(true);
            //String len = Long.toString(file.length());
            //Log.i("GDProj", "length:" + len);
            //request.setRequestProperty("Content-Length", len);
            request.setRequestProperty("Content-Type", "video/mp4");

            DataOutputStream output = new DataOutputStream(request.getOutputStream());
            //FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = new byte[64 * 1024];
            int counter;
            while ((counter = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, counter);
            }

            output.flush();
            request.connect();
            int response = request.getResponseCode();
            String code = "" + response;
            Log.i(App.getTag(), code);
            if (response == HttpURLConnection.HTTP_OK) {
                Log.i(App.getTag(), "Success send");
            }else{
                String s = response + " " + request.getResponseMessage();
                Log.e(App.getTag(), s);
            }
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        } finally {
            if (request != null)
                request.disconnect();
        }
        return null;
    }

    @Override
    public Single<FileInputStream> getRecord(@NonNull String name) {
        // TODO
        return null;
    }

    @Override
    public Observable<Record> getRecords() {
        return null;
    }

    @Override
    public Maybe<RecordFolder> getRecordFolder(@NonNull Record record) {
        return null;
    }

    @Override
    public Observable<String> getFragments(@NonNull RecordFolder recordFolder) {
        return null;
    }

    @Override
    public Completable downloadFragment(@NonNull RecordFolder recordFolder, @NonNull java.io.File file) {
        return null;
    }
}

/*public class GoogleDriveService implements CloudService {


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
    public Observable<Record> getRecords() {
        return Observable.create(emitter -> {
            SortOrder sortOrder = new SortOrder.Builder().addSortDescending(SortableField.CREATED_DATE).build();
            Query query = new Query.Builder()
                    .setSortOrder(sortOrder)
                    .addFilter(Filters.eq(SearchableField.TRASHED, false))
                    .build();
            Task<MetadataBuffer> queryTask = driveResourceClient.queryChildren(appFolderId.asDriveFolder(), query);
            queryTask
                    .addOnSuccessListener(Runnable::run, metadataBuffer -> {
                        for (Metadata metadata : metadataBuffer) {
                            String title = metadata.getTitle();
                            emitter.onNext(new Record(title));
                        }
                        metadataBuffer.release();
                        emitter.onComplete();
                    })
                    .addOnFailureListener(Runnable::run, e -> emitter.onError(e));
        });
    }

    @Override
    public Maybe<RecordFolder> getRecordFolder(@NonNull Record record) {
        return Maybe.create(emitter -> {
            Query recordQuery = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, record.getTitle()))
                    .addFilter(Filters.eq(SearchableField.TRASHED, false))
                    .build();
            Task<MetadataBuffer> recordTask = driveResourceClient.queryChildren(
                    appFolderId.asDriveFolder(), recordQuery)
                    .addOnSuccessListener(Runnable::run, metadataBuffer -> {
                        try {
                            if (metadataBuffer.getCount() == 0) {
                                emitter.onComplete();
                            }

                            Metadata metadata = metadataBuffer.get(0);
                            emitter.onSuccess(new GoogleDriveRecordFolder(metadata.getDriveId()));
                        } finally {
                            metadataBuffer.release();
                        }
                    })
                    .addOnFailureListener(Runnable::run, e -> emitter.onError(e) );
        });
    }

    @Override
    public Observable<String> getFragments(@NonNull RecordFolder recordFolder) {
        return Observable.create(emitter -> {
            if (!(recordFolder instanceof GoogleDriveRecordFolder)) {
                emitter.onError(new IllegalArgumentException());
                return;
            }
            GoogleDriveRecordFolder googleDriveRecordFolder = (GoogleDriveRecordFolder) recordFolder;

            Query fragmentsQuery = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TRASHED, false))
                    .build();
            Task<MetadataBuffer> fragmentsTask = driveResourceClient.queryChildren(
                    googleDriveRecordFolder.getDriveId().asDriveFolder(), fragmentsQuery)
                    .addOnSuccessListener(Runnable::run, metadataBuffer -> {
                        try {
                            if (metadataBuffer.getCount() == 0) {
                                emitter.onComplete();
                                return;
                            }

                            for (Metadata metadata : metadataBuffer) {
                                emitter.onNext(metadata.getTitle());
                            }
                        } finally {
                            metadataBuffer.release();
                        }
                        emitter.onComplete();
                    })
                    .addOnFailureListener(Runnable::run, e -> emitter.onError(e) );
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

    @Override
    public Completable downloadFragment(@NonNull RecordFolder recordFolder, @NonNull File file) {
        return Completable.create(emitter -> {
            if (!(recordFolder instanceof GoogleDriveRecordFolder)) {
                emitter.onError(new IllegalArgumentException());
                return;
            }
            GoogleDriveRecordFolder googleDriveRecordFolder = (GoogleDriveRecordFolder) recordFolder;

            Query fragmentsQuery = new Query.Builder()
                    .addFilter(Filters.and(
                            Filters.eq(SearchableField.TRASHED, false),
                            Filters.eq(SearchableField.TITLE, file.getName())))
                    .build();
            Task<MetadataBuffer> fragmentsTask = driveResourceClient.queryChildren(
                    googleDriveRecordFolder.getDriveId().asDriveFolder(), fragmentsQuery);

            fragmentsTask.continueWithTask(Runnable::run, task -> {
                MetadataBuffer metadataBuffer = task.getResult();
                try {
                    if (metadataBuffer.getCount() != 1) {
                        throw new Exception("Google Drive returned info about more than one fragment");
                    }
                    Metadata metadata = metadataBuffer.get(0);
                    DriveFile driveFile = metadata.getDriveId().asDriveFile();

                    return driveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY);
                } finally {
                    metadataBuffer.release();
                }
            }).continueWithTask(Runnable::run, task -> {
                DriveContents contents = task.getResult();

                InputStream inputStream = contents.getInputStream();
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bufferLength;
                    while ( (bufferLength = inputStream.read(buffer)) != -1 ) {
                        fileOutputStream.write(buffer, 0, bufferLength);
                    }
                }

                return driveResourceClient.discardContents(contents);
            })
                    .addOnSuccessListener(Runnable::run, s -> emitter.onComplete())
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
*/