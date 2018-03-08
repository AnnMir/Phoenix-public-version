package nsu.fit.g14201.marchenko.phoenix.camerapermission;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseActivity;
import nsu.fit.g14201.marchenko.phoenix.ui.MainActivity;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class RequiredPermissionsActivity extends BaseActivity {
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String NO_CAMERA_ACCESS_TAG = "NO_CAMERA_ACCESS_TAG";
    private static final String NO_WRITE_EXTERNAL_ACCESS_TAG = "NO_WRITE_EXTERNAL_ACCESS_TAG";
    private boolean permissionIsBeingRequested = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionIsBeingRequested = true;
        requestPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (permissionIsBeingRequested) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (getSupportFragmentManager().findFragmentByTag(NO_CAMERA_ACCESS_TAG) == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content,
                                NoRequiredPermissionFragment.newInstance(Manifest.permission.CAMERA),
                                NO_CAMERA_ACCESS_TAG)
                        .commit();
            }
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (getSupportFragmentManager().findFragmentByTag(NO_WRITE_EXTERNAL_ACCESS_TAG) == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content,
                                    NoRequiredPermissionFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    NO_WRITE_EXTERNAL_ACCESS_TAG)
                            .commit();
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return;
        }

        goToNextView();
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
                } else {
                    checkWriteExternalStoragePermission();
                }
                break;
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(),
                            NoRequiredPermissionFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            R.id.content,
                            NO_WRITE_EXTERNAL_ACCESS_TAG
                    );
                } else {
                    goToNextView();
                }
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {
            checkWriteExternalStoragePermission();
        }
    }

    private void checkWriteExternalStoragePermission() {
        permissionIsBeingRequested = true;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            goToNextView();
            return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    private void goToNextView() {
        permissionIsBeingRequested = false;
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        startActivity(new Intent(this, MainActivity.class));
    }
}
