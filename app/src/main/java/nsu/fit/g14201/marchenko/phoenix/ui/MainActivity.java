package nsu.fit.g14201.marchenko.phoenix.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.connection.UserConnection;
import nsu.fit.g14201.marchenko.phoenix.context.Context;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingContract;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingFragment;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingListener;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingPresenter;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;
import nsu.fit.g14201.marchenko.phoenix.registration.RegistrationActivity;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionContract;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionDetailedProblem;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionModuleListener;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionPresenter;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionProblem;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

import static nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionProblem.FAILED_TO_CREATE_VIDEO_FOLDER;
import static nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionProblem.RECORD_NOT_FOUND_LOCALLY;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        RecordingListener,
        TransmissionModuleListener,
        OnCompleteListener<Void> {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Context appContext;

    private RecordingContract.Presenter recordingPresenter;
    private TransmissionContract.Presenter transmissionPresenter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureToolbarAndNavigationView();
        try {
            appContext = Context.createContext(this);
            configureRecordingBlock();
        } catch (SignInException e) {
            showSnack(getString(R.string.some_error));
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    protected void onStart() {
        recordingPresenter.start();
        super.onStart();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.nav_records_management:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        recordingPresenter.removeRecordingListener();
        recordingPresenter.stop();

        super.onStop();
    }

    @Override
    public void recordWillStart(@NonNull VideoFragmentPath videoFragmentPath) {
        if (transmissionPresenter == null) {
            transmissionPresenter = new TransmissionPresenter(appContext);
        }
        recordingPresenter.setVideoFragmentListener(
                transmissionPresenter.prepareForNewTransmission(videoFragmentPath)
        );
    }

    @Override
    public void recordDidStart() {
        transmissionPresenter.setTransmissionModuleListener(this);
        transmissionPresenter.start();
    }

    @Override
    public void recordDidStop() {
        // TODO
        transmissionPresenter.stop();
    }

    @Override
    public void onUnableToContinueTransmission(@NonNull TransmissionProblem problem) {
        recordingPresenter.removeVideoFragmentListener();

        StringBuilder message = new StringBuilder();
        switch (problem.getType()) {
            case FAILED_TO_CREATE_VIDEO_FOLDER:
                message.append(getApplication().getString(R.string.error_working_with_cloud,
                        ((TransmissionDetailedProblem) problem).getMessage()));
                break;
            case RECORD_NOT_FOUND_LOCALLY:
                message.append(getApplication().getString(R.string.can_not_find_fragment_locally));
        }
        message.append(". ");
        message.append(getApplication().getString(R.string.stop_uploading_data));
        message.append(".");

        showToast(message.toString());
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.d(App.getTag(), "Signed out");
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        } else {
            showSnack(getString(R.string.sign_out_failure));
            Exception exception = task.getException();
            if (exception != null) {
                Log.e(App.getTag(), exception.getMessage());
            }
        }
    }

    private void configureToolbarAndNavigationView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configureRecordingBlock() {
        RecordingFragment recordingFragment =
                (RecordingFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.main_content);

        if (recordingFragment == null) {
            recordingFragment = RecordingFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    recordingFragment,
                    R.id.main_content,
                    null
            );
        }
        recordingPresenter = new RecordingPresenter(getApplicationContext(), recordingFragment);
        recordingPresenter.setContext(appContext);
        recordingPresenter.setRecordingListener(this);
    }

    private void signOut() {
        UserConnection userConnection = GoogleUserConnection.getInstance();
        if (!userConnection.isSignedIn(this)) {
            showSnack(getString(R.string.already_signed_out));
            return;
        }
        try {
            userConnection.signOut(this, this);
        } catch (SignInException e) {
            showSnack(getString(R.string.sign_out_failure));
            Log.e(App.getTag(), e.getMessage());
        }
    }
}

// TODO: Configure backstack: https://developer.android.com/guide/components/activities/tasks-and-back-stack.html
