package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.googledrive.GoogleDriveAPI;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private CloudAPI cloudAPI; // FIXME: Do we need it here?
    private RecordReposControllerProviding recordRepositoriesController;

    public Context(CloudAPI cloudAPI, RecordReposControllerProviding recordRepositoriesController) {
        this.cloudAPI = cloudAPI;
        this.recordRepositoriesController = recordRepositoriesController;
    }

    public RecordReposControllerProviding getRecordRepositoriesController() {
        return recordRepositoriesController;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        GoogleDriveAPI googleDriveAPI = new GoogleDriveAPI(context);
        PrivateExternalStorage localStorage = new PrivateExternalStorage(context);
        RecordReposControllerProviding recordRepositoriesController =
                new RecordRepositoriesController(localStorage);
        Context newContext = new Context(googleDriveAPI, recordRepositoriesController);
        localStorage.setListener(recordRepositoriesController);

        GoogleDriveService googleDriveService = new GoogleDriveService(googleDriveAPI);

        recordRepositoriesController.addCloudService(googleDriveService);
        googleDriveService.setListener(recordRepositoriesController);

        return newContext;
    }
}
