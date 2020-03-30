package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.Nullable;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.camerapermission.RequiredPermissionsActivity;
import nsu.fit.g14201.marchenko.phoenix.coordination.Coordinator;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.BaseActivity;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class RegistrationActivity extends BaseActivity implements Coordinator {
    private AuthorizationContract.Presenter authorizationPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_registration;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthorizationFragment authorizationFragment =
                (AuthorizationFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.contentFrame);

        if (authorizationFragment == null) {
            authorizationFragment = AuthorizationFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    authorizationFragment,
                    R.id.contentFrame,
                    null);
        }

        authorizationPresenter = new AuthorizationPresenter(
                getApplicationContext(), authorizationFragment);
        authorizationPresenter.setCoordinator(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        authorizationPresenter.start();
    }

    @Override
    public void next() {
        Intent intent = new Intent(this, RequiredPermissionsActivity.class);
        startActivity(intent);
    }
}

