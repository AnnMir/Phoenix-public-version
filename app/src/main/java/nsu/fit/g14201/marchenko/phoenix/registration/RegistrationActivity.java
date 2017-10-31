package nsu.fit.g14201.marchenko.phoenix.registration;


import android.os.Bundle;
import android.support.annotation.Nullable;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.activity.BaseActivity;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class RegistrationActivity extends BaseActivity {
    private AuthorizationPresenter authorizationPresenter;

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
                    R.id.contentFrame);

            authorizationPresenter = new AuthorizationPresenter(authorizationFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        authorizationPresenter.start();
    }

    //TODO Добавить сохранение при повороте
}
