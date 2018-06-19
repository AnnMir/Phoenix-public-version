package nsu.fit.g14201.marchenko.phoenix.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
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
import nsu.fit.g14201.marchenko.phoenix.model.VideoFragmentPath;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingContract;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingFragment;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingListener;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingPresenter;
import nsu.fit.g14201.marchenko.phoenix.recordmanagement.RecordManagementContract;
import nsu.fit.g14201.marchenko.phoenix.recordmanagement.RecordManagementFragment;
import nsu.fit.g14201.marchenko.phoenix.recordmanagement.RecordManagementPresenter;
import nsu.fit.g14201.marchenko.phoenix.recordmanagement.record.RecordInfoFragment;
import nsu.fit.g14201.marchenko.phoenix.recordmanagement.record.RecordInfoPresenter;
import nsu.fit.g14201.marchenko.phoenix.registration.RegistrationActivity;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionContract;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionDetailedProblem;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionListener;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionPresenter;
import nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionProblem;

import static nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionProblem.FAILED_TO_CREATE_VIDEO_FOLDER;
import static nsu.fit.g14201.marchenko.phoenix.transmission.TransmissionProblem.RECORD_NOT_FOUND_LOCALLY;

public class MainActivity extends DrawerActivity implements
        RecordingListener,
        TransmissionListener,
        RecordManagementContract.RecordSelectionListener,
        OnCompleteListener<Void> {
    private Context appContext;
    private RecordingContract.Presenter recordingPresenter;
    private TransmissionContract.Presenter transmissionPresenter;

    public final static String ACTION_START_RECORDING = "nsu.fit.g14201.marchenko.phoenix.ui.activities.start_recording";
    private final static String RECORDING_FRAGMENT_TAG = "nsu.fit.g14201.marchenko.phoenix.ui.activities.recording_fragment";
    private final static String RECORDS_MANAGMEENT_FRAGMENT_TAG =
            "nsu.fit.g14201.marchenko.phoenix.ui.activities.records_management";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            appContext = Context.createContext(this);
            Log.d("safari5", "on create");
            configureRecordingBlock();
        } catch (SignInException e) {
            showSnack(getString(R.string.some_error));
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Log.d("safari5", "on start");

        recordingPresenter.start();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        getIntent().setAction(null);
        if (action != null && action.equalsIgnoreCase(MainActivity.ACTION_START_RECORDING)) {
            recordingPresenter.changeRecordingState();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.recording:
//                configureRecordingBlock();
                break;
            case R.id.nav_records_management:
                runRecordManagementBlock();
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
    protected void onDestroy() {
        recordingPresenter.removeRecordingListener();

        super.onDestroy();
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
        transmissionPresenter.setTransmissionListener(this);
        transmissionPresenter.start();
    }

    @Override
    public void onUnableToContinueTransmission(@NonNull TransmissionProblem problem) {
        recordingPresenter.removeVideoFragmentListener();
        transmissionPresenter.removeTransmissionListener();

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
    public void onTransmissionFinished() {
        transmissionPresenter.removeTransmissionListener();
    }

    @Override
    public void onRecordSelected(Record record) {
        RecordInfoFragment fragment = RecordInfoFragment.newInstance();

        RecordInfoPresenter presenter = new RecordInfoPresenter(fragment,
                record, appContext.getLocalStorage(), appContext.getRemoteRepositoriesController());
        fragment.setPresenter(presenter);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit();
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

    private void configureRecordingBlock() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(RECORDING_FRAGMENT_TAG);
        if (fragment != null) {
            Log.d("safari5", "not null");
            return;
        }
        Log.d("safari5", "null");

        RecordingFragment recordingFragment = RecordingFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, recordingFragment, RECORDING_FRAGMENT_TAG)
                .commit();

        recordingPresenter = new RecordingPresenter(getApplicationContext(), recordingFragment);
        recordingPresenter.setContext(appContext);
        recordingPresenter.setRecordingListener(this);
    }

    private void runRecordManagementBlock() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(RECORDS_MANAGMEENT_FRAGMENT_TAG);
        if (fragment != null) {
            return;
        }

        RecordManagementFragment recordManagementFragment = RecordManagementFragment.newInstance();

        RecordManagementPresenter presenter = new RecordManagementPresenter(recordManagementFragment,
                appContext.getLocalStorage(),
                appContext.getRemoteRepositoriesController());
        recordManagementFragment.setPresenter(presenter);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, recordManagementFragment, RECORDS_MANAGMEENT_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
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
