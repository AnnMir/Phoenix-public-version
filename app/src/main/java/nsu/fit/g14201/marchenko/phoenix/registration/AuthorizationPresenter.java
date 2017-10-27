package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import nsu.fit.g14201.marchenko.phoenix.model.UserConnection;

class AuthorizationPresenter implements AuthorizationContract.Presenter {
    private final AuthorizationContract.View authView;
    private UserConnection userConnection;

    AuthorizationPresenter(AuthorizationContract.View authView) {
        this.authView = authView;
        authView.setPresenter(this);
    }

    @Override
    public void start() {
        userConnection = new UserConnection(); //TODO move somewhere
        userConnection.createClient(authView.getAuthorizationActivity());
    }

    @Override
    public void signIn() {
        if (userConnection.isSignedIn()) {
            authView.showSnack("Already Signed In");
        } else {
            authView.startSignInActivity(userConnection.getIntentForSigningIn());
        }
    }

    @Override
    public void handleSignInResult(int resultCode, Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            userConnection.setGoogleSignInAccount(result.getSignInAccount());
            authView.showMessage("Signed is as " + userConnection.getUserDisplayName());
        } else {
            authView.showMessage("Failed to sign in");
        }
    }

    @Override
    public void signOut() {
        if (!userConnection.isSignedIn()) {
            authView.showSnack("Already signed out");
            return;
        }

        userConnection.signOut(status -> {
            if (status.isSuccess()) {
                authView.showMessage("Signed in as NULL");
            } else {
                authView.showSnack("Failed to log out!");
            }
        });
    }
}
