package nsu.fit.g14201.marchenko.phoenix.transmission;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.VideoFragmentListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRemoteRepoStateListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public class PeriodicRecordRemoteTransmitter implements RecordRemoteRepoStateListener,
        VideoFragmentListener {
    private RecordRepositoriesController recordRepositoriesController;
    private VideoFragmentPath videoFragmentPath;
    private TransmissionListener transmissionListener;

    PeriodicRecordRemoteTransmitter(@NonNull RecordRepositoriesController recordRepositoriesController,
                                    @NonNull VideoFragmentPath videoFragmentPath) {
        this.recordRepositoriesController = recordRepositoriesController;
        this.videoFragmentPath = videoFragmentPath;

        recordRepositoriesController.setRemoteRepoStateListener(this);
    }

    @Override
    public void onFragmentSavedLocally(@NonNull String fragmentName) {
        new Thread() {
            @Override
            public void run() {
                recordRepositoriesController.getRecord(fragmentName, new RecordRepositoriesController.RecordGetter() {
                    @Override
                    public void onRecordGot(FileInputStream record) {
                        transmitVideoFragment(record);
                    }

                    @Override
                    public void onRecordNotFound() {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        Runnable reaction = () -> {
                            transmissionListener.onUnableToContinueTransmission(
                                    new TransmissionProblem(TransmissionProblem.RECORD_NOT_FOUND_LOCALLY)
                            );
                        };
                        mainHandler.post(reaction);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onFailedToCreateVideoRepository(@NonNull Exception e, @NonNull String name) {
        // TODO: Handle no internet access case
        e.printStackTrace();
        Log.e(App.getTag(), e.getLocalizedMessage());
        transmissionListener.onUnableToContinueTransmission(
                new TransmissionDetailedProblem(
                        TransmissionProblem.FAILED_TO_CREATE_VIDEO_FOLDER, name
                )
        );
    }

    void createVideoRepositories() {
        recordRepositoriesController.createVideoRepositoryRemotely(videoFragmentPath.getDirectoryName());
    }

    void setTransmissionListener(@NonNull TransmissionListener listener) {
        transmissionListener = listener;
    }

    void removeTransmissionListener() { // TODO: Use
        transmissionListener = null;
    }

    private void transmitVideoFragment(FileInputStream inputStream) {
        //                // Start by creating a new contents, and setting a callback.
//                Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(
//                        result -> {
//                            // If the operation was not successful, we cannot do
//                            // anything and must fail.
//                            if (!result.getStatus().isSuccess()) {
//                                Log.d(TAG, "Failed to create new contents.");
//                                return;
//                            }
//                            Log.d(TAG, "Connection successful, creating new contents...");
//                            // Otherwise, we can write our data to the new contents.
//                            // Get an output stream for the contents.
//                            OutputStream outputStream = result.getDriveContents()
//                                    .getOutputStream();
//                            FileInputStream fileInputStream = null;
//
//                            try {
//                                final String realPath = VideoUtils.getRealPathFromURI(
//                                        getBaseContext(), videoUri);
//                                Log.d(TAG, "Path: " + realPath);
////                        File video = new File(realPath);
//                                fileInputStream = new FileInputStream(realPath);
//                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                                byte[] buf = new byte[1024];
//                                int n;
//                                while (-1 != (n = fileInputStream.read(buf)))
//                                    baos.write(buf, 0, n);
//                                byte[] photoBytes = baos.toByteArray();
//                                outputStream.write(photoBytes);
//                            } catch (FileNotFoundException e) {
//                                Log.d(TAG, "FileNotFoundException: " + e.getMessage());
//                            } catch (IOException e1) {
//                                Log.d(TAG, "Unable to write file contents." + e1.getMessage());
//                            } finally {
//                                try {
//                                    if (outputStream != null) {
//                                        outputStream.close();
//                                    }
//                                } catch (IOException e) {
//                                    Log.d(TAG, e.getMessage());
//                                }
//                                try {
//                                    if (fileInputStream != null) {
//                                        fileInputStream.close();
//                                    }
//                                } catch (IOException e) {
//                                    Log.d(TAG, e.getMessage());
//                                }
//                            }
//
//                            Log.i(TAG, "Creating new video on Drive");
//
//                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//                                    .setMimeType("video/mp4")
//                                    .setTitle(TEST_FILE_NAME)
//                                    .build();
//
//                            appFolderId.asDriveFolder()
//                                    .createFile(googleApiClient, changeSet, result.getDriveContents())
//                                    .setResultCallback(createFileResult -> {
//                                        if (!createFileResult.getStatus().isSuccess()) {
//                                            Log.d(TAG, "Error while trying to create the file");
//                                            return;
//                                        }
//                                        Log.d(TAG, "Created a file with content: " + createFileResult.getDriveFile().getDriveId());
//                                    });
//
//                        });
    }
}
