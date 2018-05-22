package nsu.fit.g14201.marchenko.phoenix.camerapermission;


import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.fragments.BaseFragment;

public class NoRequiredPermissionFragment extends BaseFragment {
    private static final String ARG_PERMISSION = "arg_permission";

    static NoRequiredPermissionFragment newInstance(@NonNull String permission) {
        final NoRequiredPermissionFragment fragment = new NoRequiredPermissionFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_PERMISSION, permission);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String permission = getArguments().getString(ARG_PERMISSION);

        if (permission != null) {
            TextView grantAccess = view.findViewById(R.id.grant_access);
            TextView needForPermissionExplanation = view.findViewById(R.id.need_for_permission_explanation);
            switch (permission) {
                case Manifest.permission.CAMERA:
                    grantAccess.setText(R.string.grant_camera_access);
                    needForPermissionExplanation.setText(R.string.need_for_camera_explanation);
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    grantAccess.setText(R.string.grant_write_external_storage_access);
                    needForPermissionExplanation.setText(R.string.need_for_write_external_storage_explanation);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_no_required_permission;
    }

    @OnClick(R.id.system_settings)
    void onSystemSettingsClick() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
