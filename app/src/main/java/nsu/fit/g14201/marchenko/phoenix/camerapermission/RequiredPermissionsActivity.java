package nsu.fit.g14201.marchenko.phoenix.camerapermission;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.BaseActivity;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.MainActivity;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class RequiredPermissionsActivity extends BaseActivity {
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 3;
    private static final int PERMISSIONS_SEND_SMS = 4;
    private static final String NO_CAMERA_ACCESS_TAG = "NO_CAMERA_ACCESS_TAG";
    private static final String NO_WRITE_EXTERNAL_ACCESS_TAG = "NO_WRITE_EXTERNAL_ACCESS_TAG";
    private static final String NO_READ_CONTACTS_ACCESS_TAG = "NO_READ_CONTACTS_ACCESS_TAG";
    private static final String NO_SEND_SMS_ACCESS_TAG = "NO_SEND_SMS_ACCESS_TAG";

    private boolean permissionIsBeingRequested = false;
    private boolean determinedToGoToNextView = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!permissionIsBeingRequested && !determinedToGoToNextView) {
            requestPermissions();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera_permission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        permissionIsBeingRequested = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(),
                            NoRequiredPermissionFragment.newInstance(Manifest.permission.CAMERA),
                            R.id.content,
                            NO_CAMERA_ACCESS_TAG
                    );
                    return;
                }
                break;
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(),
                            NoRequiredPermissionFragment.newInstance(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            R.id.content,
                            NO_WRITE_EXTERNAL_ACCESS_TAG
                    );
                    return;
                }
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(),
                            NoRequiredPermissionFragment.newInstance(
                                    Manifest.permission.READ_CONTACTS),
                            R.id.content,
                            NO_READ_CONTACTS_ACCESS_TAG
                    );
                    return;
                }
            case PERMISSIONS_SEND_SMS:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(),
                            NoRequiredPermissionFragment.newInstance(
                                    Manifest.permission.SEND_SMS),
                            R.id.content,
                            NO_SEND_SMS_ACCESS_TAG
                    );
                    return;
                }
        }
        requestPermissions();
    }

    private void requestPermissions() {
        if (!ifCameraGranted()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                if (getSupportFragmentManager().findFragmentByTag(NO_CAMERA_ACCESS_TAG) == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content,
                                    NoRequiredPermissionFragment.newInstance(Manifest.permission.CAMERA),
                                    NO_CAMERA_ACCESS_TAG)
                            .commit();
                }
            } else {
                requestCameraPermission();
            }
        } else {
            Fragment noCameraAccessFragment = getSupportFragmentManager()
                    .findFragmentByTag(NO_CAMERA_ACCESS_TAG);
            if (noCameraAccessFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(noCameraAccessFragment)
                        .commit();
            }
            if (!ifRecordAudioGranted() &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                requestRecordAudioPermission();
            } else if (!ifWriteExternalStorageGranted()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (getSupportFragmentManager()
                            .findFragmentByTag(NO_WRITE_EXTERNAL_ACCESS_TAG) == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content,
                                        NoRequiredPermissionFragment.newInstance(
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        NO_WRITE_EXTERNAL_ACCESS_TAG)
                                .commit();
                    }
                } else {
                    requestWriteExternalStoragePermission();
                }
            }else if(!ifReadContactsGranted()){
                recuestContactsPermission();
            } else if(!ifSendSmsGranted()){
                requestSendSmsPermission();
            }else {
                goToNextView();
            }
        }
    }

    private void goToNextView() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        determinedToGoToNextView = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean ifCameraGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean ifRecordAudioGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean ifWriteExternalStorageGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean ifReadContactsGranted(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean ifSendSmsGranted(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermission(Manifest.permission.CAMERA, PERMISSIONS_REQUEST_CAMERA);
    }

    private void requestRecordAudioPermission() {
        requestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    private void requestWriteExternalStoragePermission() {
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    private void recuestContactsPermission(){
        requestPermission(Manifest.permission.READ_CONTACTS, PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    private void requestSendSmsPermission(){
        requestPermission(Manifest.permission.SEND_SMS, PERMISSIONS_SEND_SMS);
    }

    private void requestPermission(String permission, int tag) {
        permissionIsBeingRequested = true;
        ActivityCompat.requestPermissions(this,
                new String[]{permission}, tag);
    }


}
