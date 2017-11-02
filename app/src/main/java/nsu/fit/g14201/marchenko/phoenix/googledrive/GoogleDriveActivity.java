package nsu.fit.g14201.marchenko.phoenix.googledrive;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.activity.BaseActivity;

public class GoogleDriveActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener {

    protected static final int REQUEST_CODE_RESOLUTION = 1;
    protected static final int REQUEST_VIDEO_CAPTURE = 2;
    protected static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final String TAG = "Christina";
    private static final String FOLDER_NAME = "Phoenix";
    private static final String TEST_FILE_NAME = "Test file";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView loginTextView;
    private VideoView videoView;
    private Button uploadButton;
    private Uri videoUri;
    private DriveId appFolderId;
    //TODO
//    private boolean waitToCreateFile = false;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureToolbarAndNavigationView();
        uploadButton = findViewById(R.id.upload_button);
        uploadButton.setClickable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (googleApiClient == null) {
            GoogleSignInOptions googleSignInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                            .build();

            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .addApi(Drive.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            startActivityForResult(
                    Auth.GoogleSignInApi.getSignInIntent(googleApiClient),
                    REQUEST_CODE_RESOLUTION);
        } else {
            googleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }

    @Override
    protected void onPause() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.d(TAG, "Disconnected");
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
                uploadButton.setClickable(true);
                videoView = findViewById(R.id.video_view);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(videoUri);
                videoView.start();
            } else {
                showToast("Failed to record video");
            }
            return;
        }

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            if (googleSignInAccount != null) {
                loginTextView.setText(googleSignInAccount.getDisplayName());
            } else {
                loginTextView.setText("NULL");
            }
        } else {
            loginTextView.setText("NULL");
        }

        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            googleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
            Log.d(TAG, "It's connect");
        } else {
            Log.d(TAG, "It's not connect");
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_google_drive;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected");
        createAppFolderIfNotExists();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());

        if (!connectionResult.hasResolution()) {
            Log.d(TAG, "No resolution");
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        try {
            Log.d(TAG, "Resolution exists");
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.nav_logout:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.record_button)
    void onRecordClick() {
        videoUri = null;
        videoView = findViewById(R.id.video_view);
        videoView.setVisibility(View.GONE);
        uploadButton.setClickable(false);

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @OnClick(R.id.upload_button)
    void onUploadClick() {
//        Log.d(TAG, videoUri.getPath());
        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(result -> {
                    if (!result.getStatus().isSuccess()) {
                        Log.d(TAG, "Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    } else {
                        saveFileToDrive();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveFileToDrive();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void saveFileToDrive() {
        new Thread() {
            @Override
            public void run() {
                // Start by creating a new contents, and setting a callback.
                Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(
                        result -> {
                            // If the operation was not successful, we cannot do
                            // anything and must fail.
                            if (!result.getStatus().isSuccess()) {
                                Log.d(TAG, "Failed to create new contents.");
                                return;
                            }
                            Log.d(TAG, "Connection successful, creating new contents...");
                            // Otherwise, we can write our data to the new contents.
                            // Get an output stream for the contents.
                            OutputStream outputStream = result.getDriveContents()
                                    .getOutputStream();
                            FileInputStream fileInputStream = null;

                            try {
                                final String realPath = VideoUtils.getRealPathFromURI(
                                        getBaseContext(), videoUri);
                                Log.d(TAG, "Path: " + realPath);
//                        File video = new File(realPath);
                                fileInputStream = new FileInputStream(realPath);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buf = new byte[1024];
                                int n;
                                while (-1 != (n = fileInputStream.read(buf)))
                                    baos.write(buf, 0, n);
                                byte[] photoBytes = baos.toByteArray();
                                outputStream.write(photoBytes);
                            } catch (FileNotFoundException e) {
                                Log.d(TAG, "FileNotFoundException: " + e.getMessage());
                            } catch (IOException e1) {
                                Log.d(TAG, "Unable to write file contents." + e1.getMessage());
                            } finally {
                                try {
                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                } catch (IOException e) {
                                    Log.d(TAG, e.getMessage());
                                }
                                try {
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                } catch (IOException e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            }

                            Log.i(TAG, "Creating new video on Drive");

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("video/mp4")
                                    .setTitle(TEST_FILE_NAME)
                                    .build();

                            appFolderId.asDriveFolder()
                                    .createFile(googleApiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(createFileResult -> {
                                        if (!createFileResult.getStatus().isSuccess()) {
                                            Log.d(TAG, "Error while trying to create the file");
                                            return;
                                        }
                                        Log.d(TAG, "Created a file with content: " + createFileResult.getDriveFile().getDriveId());
                                    });

                        });
            }
        }.start();
    }

    private void configureToolbarAndNavigationView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.google_drive_drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        NavigationView navigationView = findViewById(R.id.google_drive_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        loginTextView = header.findViewById(R.id.login_text_view);
        loginTextView.setText("NULL");
    }

    private void createAppFolderIfNotExists() {
        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"),
                        Filters.eq(SearchableField.TITLE, FOLDER_NAME),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(googleApiClient, query)
                .setResultCallback(result -> {
                    if (!result.getStatus().isSuccess()) {
                        Log.d(TAG, "Error while trying to create the folder");
                        Log.d(TAG, result.getStatus().getStatusMessage());
                    } else {
                        for (Metadata metadata : result.getMetadataBuffer()) {
                            if (metadata.getTitle().equals(FOLDER_NAME)) {
                                Log.d(TAG, "Folder already exists");
                                appFolderId = metadata.getDriveId();
                                return;
                            }
                        }
                        createAppFolder();
                    }
                });
    }

    private void createAppFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(FOLDER_NAME)
                .build();
        Drive.DriveApi.getRootFolder(googleApiClient)
                .createFolder(googleApiClient, changeSet)
                .setResultCallback(driveFolderResult -> {
                    Status status = driveFolderResult.getStatus();
                    status.getStatusCode();
                    if (!status.isSuccess()) {
                        Log.d(TAG, "Error while trying to create the folder");
                        return;
                    }
                    appFolderId = driveFolderResult.getDriveFolder().getDriveId();
                    Log.d(TAG, "Created a folder: " + appFolderId.toString());
                });
    }
}
