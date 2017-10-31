package nsu.fit.g14201.marchenko.phoenix.model;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class UserConnection implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    private GoogleSignInAccount googleSignInAccount;

    private UserConnection() {
    }

    public static UserConnection getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public void createClient(FragmentActivity fragmentActivity) {
        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        googleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    public Intent getIntentForSigningIn() {
        return Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    }

    public void setGoogleSignInAccount(GoogleSignInAccount account) {
        googleSignInAccount = account;
    }

    public boolean isSignedIn() {
        return googleSignInAccount != null;
    }

    public String getUserDisplayName() {
        return googleSignInAccount.getDisplayName();
    }

    public void signOut(ResultCallback<Status> callback) {
        googleSignInAccount = null;
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(callback);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("TAG", "Oops! Connection failed"); //FIXME
    }

    private static class SingletonHelper {
        private static final UserConnection INSTANCE = new UserConnection();
    }
}
