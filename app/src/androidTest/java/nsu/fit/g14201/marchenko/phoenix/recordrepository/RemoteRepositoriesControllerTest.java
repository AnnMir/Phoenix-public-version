package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import androidx.test.platform.app.InstrumentationRegistry;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.registration.RegistrationActivity;

import static org.junit.Assert.*;

public class RemoteRepositoriesControllerTest {

    RemoteRepositoriesController remoteRepositoriesController;

    @Before
    public void setUp() throws Exception {

        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(appContext, Collections.singleton(DriveScopes.DRIVE));
        //credential.setSelectedAccount(googleSignInAccount.getAccount());
        credential.setBackOff(new ExponentialBackOff());
        GoogleUserConnection.getInstance().setCredential(credential);
        GoogleDriveService googleDriveService = new GoogleDriveService(appContext);
        remoteRepositoriesController = new RemoteRepositoriesController();
        remoteRepositoriesController.addCloudService(googleDriveService);
    }

    @Test
    public void createVideoRepository() {
        remoteRepositoriesController.createVideoRepository("Test Repository");

    }

    @Test
    public void getRecords() {
    }

    @Test
    public void getRecordFolder() {
    }

    @Test
    public void getFragments() {
    }

    @Test
    public void downloadFragment() {
    }

    @Test
    public void transmitVideo() {
    }
}