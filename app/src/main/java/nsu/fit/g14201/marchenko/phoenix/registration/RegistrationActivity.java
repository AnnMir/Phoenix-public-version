package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.coordination.Coordinator;
import nsu.fit.g14201.marchenko.phoenix.coordination.CoordinatorImpl;
import nsu.fit.g14201.marchenko.phoenix.coordination.SuperiorActivity;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseActivity;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class RegistrationActivity extends BaseActivity implements SuperiorActivity {
    private AuthorizationPresenter authorizationPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_registration;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        goToNextView(); //TODO Temp

        /*AuthorizationFragment authorizationFragment =
                (AuthorizationFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.contentFrame);

        if (authorizationFragment == null) {
            authorizationFragment = AuthorizationFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    authorizationFragment,
                    R.id.contentFrame);
        }

        authorizationFragment.setSuperiorActivity(this);
        authorizationPresenter = new AuthorizationPresenter(
                getApplicationContext(), authorizationFragment);*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO Temp 2
//        authorizationPresenter.start();
    }

    @Override
    public void goToNextView() {
        Coordinator coordinator = new CoordinatorImpl();
        Intent intent = new Intent(
                this,
                coordinator.getNextClass(Coordinator.View.REGISTRATION));
        intent.putExtra(App.getExtraCoordinator(), coordinator);
        startActivity(intent);
    }
}
