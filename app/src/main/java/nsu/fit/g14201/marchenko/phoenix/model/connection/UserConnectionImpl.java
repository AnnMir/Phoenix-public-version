package nsu.fit.g14201.marchenko.phoenix.model.connection;


import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;

public class UserConnectionImpl implements UserConnection {
    private GoogleSignInClient googleSignInClient;

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
        return GoogleSignIn.getLastSignedInAccount(context) != null;
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
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
    }
}
