package nsu.fit.g14201.marchenko.phoenix.connection;


import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

public class GoogleUserConnection implements UserConnection {
    private GoogleSignInClient googleSignInClient;
    private GoogleAccountCredential credential;

    private GoogleUserConnection() {}

    private static class SingletonHelper {
        private static final GoogleUserConnection INSTANCE = new GoogleUserConnection();
    }

    public static GoogleUserConnection getInstance(){
        return SingletonHelper.INSTANCE;
    }

    public void setCredential(GoogleAccountCredential credential) {
        this.credential = credential;
    }

    public GoogleAccountCredential getCredential(){
        return credential;
    }

    @Override
    public Intent startSignInAndGetIntent(Context context) throws SignInException {
        if (isSignedIn(context)) {
            throw new SignInException("Already signed in");
        }
        if (googleSignInClient == null) {
            setGoogleSignInClient(context);
        }
        return googleSignInClient.getSignInIntent();
    }

    @Override
    public boolean isSignedIn(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context) != null && credential != null;
    }

    @Override
    public void signOut(OnCompleteListener<Void> listener, Context context) throws SignInException {
        if (!isSignedIn(context)) {
            throw new SignInException("Not signed in");
        }
        setGoogleSignInClient(context);
        googleSignInClient.signOut()
                .addOnCompleteListener(listener);
    }

    private void setGoogleSignInClient(Context context) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
    }
}
