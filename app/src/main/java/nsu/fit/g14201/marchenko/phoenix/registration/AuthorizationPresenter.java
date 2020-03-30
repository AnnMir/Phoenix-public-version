package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.coordination.Coordinated;
import nsu.fit.g14201.marchenko.phoenix.coordination.Coordinator;

import static android.app.Activity.RESULT_OK;

class AuthorizationPresenter implements AuthorizationContract.Presenter, Coordinated {
    private final AuthorizationContract.View authView;
    private final Context context;
    private GoogleUserConnection userConnection;
    private Coordinator coordinator;

    AuthorizationPresenter(Context applicationContext, AuthorizationContract.View authView) {
        context = applicationContext;
        this.authView = authView;
        authView.setPresenter(this);
    }

    @Override
    public void start() {
        userConnection = GoogleUserConnection.getInstance();
        if (userConnection.isSignedIn(context)) {
            coordinator.next();
        }
    }

    @Override
    public void signIn() {
        if (userConnection.isSignedIn(context)) {
            authView.showSnack(context.getString(R.string.already_signed_in));
        } else {
            try {
                Log.d(App.getTag(), "Start sign in");
                authView.startSignInActivity(userConnection.startSignInAndGetIntent(context));
            } catch (SignInException e) {
                Log.e(App.getTag(), e.getMessage());
                authView.showSnack(context.getString(R.string.sign_in_failure));
            }
        }
    }

    @Override
    public void handleSignInResult(int resultCode, Intent data) {
        Log.i(App.getTag(), "Handling sign in result");
        Log.i(App.getTag(), String.valueOf(RESULT_OK));
        if (resultCode == RESULT_OK) {
            GoogleSignInAccount googleSignInAccount =
                    GoogleSignIn.getLastSignedInAccount(context);
            String displayedName = googleSignInAccount == null ?
                    "null" :
                    googleSignInAccount.getDisplayName();
            authView.showSnack(context.getString(R.string.signed_in_as, displayedName));
            coordinator.next();
        } else if(resultCode == GoogleSignInStatusCodes.SUCCESS){
            coordinator.next();
        }else {
            Log.e(App.getTag(), "FAILED TO SIGN IN");
            int resultStatusCode = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data)
                    .getStatus()
                    .getStatusCode();
            switch (resultStatusCode) {
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    authView.showSnack("Network error");
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS:
                    authView.showSnack("Sign in is in process already");
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                    authView.showSnack("Sign in failed");
                default:
                    Log.e(App.getTag(), "Failure code: " + resultStatusCode);
            }
        }
    }

    @Override
    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }
}
