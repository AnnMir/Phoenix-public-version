package nsu.fit.g14201.marchenko.phoenix.connection;


import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;

public interface UserConnection {
    Intent startSignInAndGetIntent(Context context) throws SignInException;

    boolean isSignedIn(Context context);

    void signOut(OnCompleteListener<Void> listener, Context context) throws SignInException;
}
