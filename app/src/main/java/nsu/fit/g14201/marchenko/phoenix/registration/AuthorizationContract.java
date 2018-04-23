package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;
import nsu.fit.g14201.marchenko.phoenix.coordination.Coordinated;

public interface AuthorizationContract {
    interface View extends BaseView<Presenter> {
        void startSignInActivity(Intent intent);

        void showSnack(String message);

        void showMessage(String message);
    }

    interface Presenter extends BasePresenter, Coordinated {
        void signIn();

        void handleSignInResult(int resultCode, Intent data);
    }
}
