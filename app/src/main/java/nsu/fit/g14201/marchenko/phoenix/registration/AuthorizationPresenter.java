package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

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
            //try {
                Log.d(App.getTag(), "Start sign in");
//                authView.startSignInActivity(userConnection.startSignInAndGetIntent(context));
     /*       } catch (SignInException e) {
                Log.e(App.getTag(), e.getMessage());
                authView.showSnack(context.getString(R.string.sign_in_failure));
            }*/
        }
    }

    @Override
    public void handleSignInResult(int resultCode, Intent data) {
        Log.i(App.getTag(), "Handling sign in result");
        Log.i(App.getTag(), String.valueOf(RESULT_OK));

        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {
                    Log.i(App.getTag(), "success sign in");

                    GoogleAccountCredential credential = GoogleAccountCredential
                            .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE));
                    credential.setSelectedAccount(googleSignInAccount.getAccount());
                    credential.setBackOff(new ExponentialBackOff());
                    GoogleUserConnection.getInstance().setCredential(credential);
                    coordinator.next();
                })
                .addOnFailureListener(e -> {
                    Log.e(App.getTag(), "FAILED TO SIGN IN");
                    int resultStatusCode = Auth.GoogleSignInApi
                            .getSignInResultFromIntent(data)
                            .getStatus()
                            .getStatusCode();
                    switch (resultStatusCode) {
                        case GoogleSignInStatusCodes.NETWORK_ERROR:
                            Toast.makeText(context, "Network error", Toast.LENGTH_LONG).show();
                            break;
                        case GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS:
                            Toast.makeText(context, "Sign in is in process already", Toast.LENGTH_LONG).show();
                            break;
                        case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                            Toast.makeText(context, "Sign in failed", Toast.LENGTH_LONG).show();
                        default:
                            Log.e(App.getTag(), "Failure code: " + resultStatusCode);
                    }
                });
    }

    @Override
    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }
}