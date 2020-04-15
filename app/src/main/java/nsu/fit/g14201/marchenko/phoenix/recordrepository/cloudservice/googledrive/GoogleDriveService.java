package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import com.google.api.services.drive.model.File;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
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
    private String appFolder;
    private Drive service;

    public GoogleDriveService(Context context){
        credential = GoogleUserConnection.getInstance().getCredential();
    }

    @Override
    public String getName() {
        return "Google Drive";
    }

    @Override
    public Completable createAppFolderIfNotExists() {
        //mime-type of folder application/vnd.google-apps.folder
        return Completable.create((CompletableEmitter emitter) -> {
            new Thread(() -> {
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

                        URL url1 = new URL("https://www.googleapis.com/drive/v3/files?q=mimeType='application/vnd.google-apps.folder' and name='"+App.getAppName()+"' and trashed=false and 'root' in parents");
                        HttpURLConnection request1 = (HttpURLConnection) url1.openConnection();
                        request1.setRequestMethod("GET");
                        request1.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                        request1.setRequestProperty("Accept", "application/json");
                        request1.setDoInput(true);
                        request1.connect();
                        if(request1.getResponseCode() == HttpURLConnection.HTTP_OK){
                            InputStream response1 = request1.getInputStream();
                            jsonString = convertStreamToString(response1);
                            JsonObject obj1 = new JsonParser().parse(jsonString).getAsJsonObject();
                            JsonElement folders = obj1.get("files");
                            for (JsonElement folder :folders.getAsJsonArray()) {
                                if(folder.getAsJsonObject().get("name").getAsString().equals(App.getAppName())){
                                    appFolder = folder.getAsJsonObject().get("id").getAsString();
                                    emitter.onComplete();
                                    return;
                                }
                            }
                            createAppFolder();
                            appFolder = getFolderId(App.getAppName(),rootFolder);
                            emitter.onComplete();
                        }
                        Log.i(App.getTag(), request1.getResponseCode()+ " " + request1.getResponseMessage());
                    }
                    Log.i(App.getTag(), request.getResponseCode()+" "+request.getResponseMessage());
                } catch (GoogleAuthException | IOException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                }
            }).start();

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
            fileMetadata.setParents(Collections.singletonList(rootFolder));
            file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getFolderId(String name, String parentName){
        String jsonString;
        HttpURLConnection request;
        try {
            URL url = new URL("https://www.googleapis.com/drive/v3/files?q=mimeType='application/vnd.google-apps.folder' and name='" + name + "' and trashed=false and "+parentName+" in parents");
            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty("Authorization", "Bearer " + credential.getToken());
            request.setRequestProperty("Accept", "application/json");
            request.setDoInput(true);
            request.connect();
            if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream response1 = request.getInputStream();
                jsonString = convertStreamToString(response1);
                JsonObject obj1 = new JsonParser().parse(jsonString).getAsJsonObject();
                JsonElement folders = obj1.get("files");
                for (JsonElement folder : folders.getAsJsonArray()) {
                    if (folder.getAsJsonObject().get("name").getAsString().equals(name)) {
                        return folder.getAsJsonObject().get("id").getAsString();
                    }
                }
            }
        } catch (GoogleAuthException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public Single<RecordFolder> createVideoRepository(@NonNull String name) {
        return Single.create(emitter -> {
                try {
                    File file = null;
                    File fileMetadata = new File();
                    fileMetadata.setName(name);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(appFolder));
                        file = service.files().create(fileMetadata)
                                .setFields("id")
                                .execute();
                    emitter.onSuccess(new GoogleDriveRecordFolder(getFolderId(name, App.getAppName())));
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
        });
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
        return Observable.create(emitter -> {
            new Thread(() -> {
                String jsonString;
                HttpURLConnection request;
                try {
                    URL url = new URL("https://www.googleapis.com/drive/v3/files?q=trashed=false and "+appFolder+" in parents");
                    request = (HttpURLConnection) url.openConnection();
                    request.setRequestMethod("GET");
                    request.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                    request.setRequestProperty("Accept", "application/json");
                    request.setDoInput(true);
                    request.connect();
                    if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream response1 = request.getInputStream();
                        jsonString = convertStreamToString(response1);
                        JsonObject obj1 = new JsonParser().parse(jsonString).getAsJsonObject();
                        JsonElement records = obj1.get("files");
                        if (records.getAsJsonArray().size() == 0) {
                            emitter.onComplete();
                            return;
                        }
                        for (JsonElement record : records.getAsJsonArray()) {
                            emitter.onNext(new Record(record.getAsJsonObject().get("name").getAsString()));
                        }
                        emitter.onComplete();
                    }
                } catch (GoogleAuthException | IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }).start();
        });
    }

    @Override
    public Maybe<RecordFolder> getRecordFolder(@NonNull Record record) {
        return Maybe.create(emitter -> {
            String folderId = getFolderId(record.getTitle(),App.getAppName());
            if(folderId.equals("")){
                emitter.onComplete();
                return;
            }
            emitter.onSuccess(new GoogleDriveRecordFolder(folderId));
        });
    }

    @Override
    public Observable<String> getFragments(@NonNull RecordFolder recordFolder) {
        return Observable.create(emitter -> {
            new Thread(() -> {
                if(!(recordFolder instanceof GoogleDriveRecordFolder)){
                    emitter.onError(new IllegalArgumentException());
                    return;
                }
                GoogleDriveRecordFolder googleDriveRecordFolder = (GoogleDriveRecordFolder) recordFolder;
                String id = googleDriveRecordFolder.getDriveId();
                String jsonString;
                HttpURLConnection request;
                try {
                    URL url = new URL("https://www.googleapis.com/drive/v3/files?q=trashed=false and "+id+" in parents");
                    request = (HttpURLConnection) url.openConnection();
                    request.setRequestMethod("GET");
                    request.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                    request.setRequestProperty("Accept", "application/json");
                    request.setDoInput(true);
                    request.connect();
                    if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream response1 = request.getInputStream();
                        jsonString = convertStreamToString(response1);
                        JsonObject obj1 = new JsonParser().parse(jsonString).getAsJsonObject();
                        JsonElement files = obj1.get("files");
                        if (files.getAsJsonArray().size() == 0) {
                            emitter.onComplete();
                            return;
                        }
                        for (JsonElement file : files.getAsJsonArray()) {
                            emitter.onNext(file.getAsJsonObject().get("name").getAsString());
                        }
                        emitter.onComplete();
                    }
                } catch (GoogleAuthException | IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }).start();
        });
    }

    @Override
    public Completable downloadFragment(@NonNull RecordFolder recordFolder, @NonNull java.io.File file) {
        return Completable.create(emitter -> {
            new Thread(() -> {
                if (!(recordFolder instanceof GoogleDriveRecordFolder)) {
                    emitter.onError(new IllegalArgumentException());
                    return;
                }
                GoogleDriveRecordFolder googleDriveRecordFolder = (GoogleDriveRecordFolder) recordFolder;

                String id = googleDriveRecordFolder.getDriveId();
                String jsonString;
                HttpURLConnection request;
                try {
                    URL url = new URL("https://www.googleapis.com/drive/v3/files?q=trashed=false and "+id+" in parents and name='"+file.getName()+"'");
                    request = (HttpURLConnection) url.openConnection();
                    request.setRequestMethod("GET");
                    request.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                    request.setRequestProperty("Accept", "application/json");
                    request.setDoInput(true);
                    request.connect();
                    if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream response1 = request.getInputStream();
                        jsonString = convertStreamToString(response1);
                        JsonObject obj1 = new JsonParser().parse(jsonString).getAsJsonObject();
                        JsonElement files = obj1.get("files");
                        if (files.getAsJsonArray().size() != 1) {
                            throw new Exception("Google Drive returned info about more than one fragment");
                        }
                        JsonElement fragment = files.getAsJsonArray().get(0);
                        String fragmentId = fragment.getAsJsonObject().get("id").getAsString();
                        HttpURLConnection fragmentRequest;
                        URL url1 = new URL("https://www.googleapis.com/drive/v3/files/"+fragmentId);
                        fragmentRequest = (HttpURLConnection) url1.openConnection();
                        fragmentRequest.setRequestMethod("GET");
                        fragmentRequest.setRequestProperty("Authorization", "Bearer " + credential.getToken());
                        //fragmentRequest.setRequestProperty("Accept", "application/json");
                        fragmentRequest.setDoInput(true);
                        fragmentRequest.connect();

                        InputStream inputStream = fragmentRequest.getInputStream();
                        FileOutputStream output = new FileOutputStream(file);
                        byte[] buffer = new byte[64 * 1024];
                        int counter;
                        while ((counter = inputStream.read(buffer)) != -1) {
                            output.write(buffer, 0, counter);
                        }
                        emitter.onComplete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }).start();
        });
    }
}

/*public class GoogleDriveService implements CloudService {


    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private DriveId appFolderId;
    private DriveFolder rootFolder;

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
*/