package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;

interface AuthorizationContract {
    interface View extends BaseView<Presenter> {
        void startSignInActivity(Intent intent);

        void showSnack(String message);

        void showMessage(String message);

        void startNextView();
    }

    interface Presenter extends BasePresenter {
        void signIn();

        void handleSignInResult(int resultCode, Intent data);

        void signOut();
    }
}
