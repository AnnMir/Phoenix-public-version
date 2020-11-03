package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.camerapermission.RequiredPermissionsActivity;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;

public class RegistrationActivity extends AppCompatActivity {

    private GoogleUserConnection userConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestSignIn();
    }

    private void requestSignIn(){
        Log.i(App.getTag(),"start signing in");
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        GoogleUserConnection.getInstance().setGoogleSignInClient(client);
        startActivityForResult(client.getSignInIntent(),400);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 400 && resultCode == RESULT_OK) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleSignInAccount -> {
                        GoogleAccountCredential credential = GoogleAccountCredential
                                .usingOAuth2(RegistrationActivity.this, Collections.singleton(DriveScopes.DRIVE));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());
                        credential.setBackOff(new ExponentialBackOff());
                        GoogleUserConnection.getInstance().setCredential(credential);
                        next();
                    })
                    .addOnFailureListener(e -> {
                        int resultStatusCode = Auth.GoogleSignInApi
                                .getSignInResultFromIntent(data)
                                .getStatus()
                                .getStatusCode();
                        switch (resultStatusCode) {
                            case GoogleSignInStatusCodes.NETWORK_ERROR:
                                Toast.makeText(RegistrationActivity.this, "Network error", Toast.LENGTH_LONG).show();
                                break;
                            case GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS:
                                Toast.makeText(RegistrationActivity.this, "Sign in is in process already", Toast.LENGTH_LONG).show();
                                break;
                            case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                                Toast.makeText(RegistrationActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();
                            default:
                                Log.e(App.getTag(), "Failure code: " + resultStatusCode);
                        }
                    });
        }
    }

    public void next() {
        Intent intent = new Intent(this, RequiredPermissionsActivity.class);
        startActivity(intent);
    }
}

