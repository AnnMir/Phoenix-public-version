package nsu.fit.g14201.marchenko.phoenix.googledrive;


import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.activity.BaseActivity;

public class GoogleDriveActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener {

    protected static final int REQUEST_CODE_RESOLUTION = 1;
    protected static final int REQUEST_VIDEO_CAPTURE = 2;
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
        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(result -> {
                    if (!result.getStatus().isSuccess()) {
                        Log.d(TAG, "Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents
                            OutputStream outputStream = driveContents.getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);
                            try {
                                writer.write("Hello World!");
                                writer.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle(TEST_FILE_NAME)
                                    .setMimeType("text/plain")
                                    .build();

                            appFolderId.asDriveFolder()
                                    .createFile(googleApiClient, changeSet, driveContents)
                                    .setResultCallback(result -> {
                                        if (!result.getStatus().isSuccess()) {
                                            Log.d(TAG, "Error while trying to create the file");
                                            return;
                                        }
                                        Log.d(TAG, "Created a file with content: " + result.getDriveFile().getDriveId());
                                    });
                        }
                    }.start();
                });
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
