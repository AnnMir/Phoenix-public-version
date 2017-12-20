package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.model.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.model.connection.UserConnectionImpl;

import static android.app.Activity.RESULT_OK;

class AuthorizationPresenter implements AuthorizationContract.Presenter,
        OnCompleteListener<Void> {
    private final AuthorizationContract.View authView;
    private final Context context;
    private UserConnectionImpl userConnection;

    AuthorizationPresenter(Context applicationContext, AuthorizationContract.View authView) {
        context = applicationContext;
        this.authView = authView;
        authView.setPresenter(this);
    }

    @Override
    public void start() {
        userConnection = UserConnectionImpl.getInstance();
        if (userConnection.isSignedIn(context)) {
            authView.startNextView(); //FIXME Remove
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
        if (resultCode == RESULT_OK) {
            GoogleSignInAccount googleSignInAccount =
                    GoogleSignIn.getLastSignedInAccount(context);
            String displayedName = googleSignInAccount == null ?
                    "null" :
                    googleSignInAccount.getDisplayName();
            authView.showSnack(context.getString(R.string.signed_in_as, displayedName));
            authView.startNextView();

            //TODO move Drive code
            /*// Use the last signed in account here since it already have a Drive scope.
            mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
            // Build a drive resource client.
            mDriveResourceClient =
                    Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
            */
        } else {
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
    public void signOut() {
        if (!userConnection.isSignedIn(context)) {
            authView.showSnack(context.getString(R.string.already_signed_out));
            return;
        }
        try {
            userConnection.signOut(this, context);
        } catch (SignInException e) {
            Log.e(App.getTag(), e.getMessage());
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            authView.showSnack(context.getString(R.string.signed_out));
        } else {
            authView.showSnack(context.getString(R.string.sign_out_failure));
            Exception exception = task.getException();
            if (exception != null) {
                Log.e(App.getTag(), exception.getMessage());
            }
        }
    }
}
