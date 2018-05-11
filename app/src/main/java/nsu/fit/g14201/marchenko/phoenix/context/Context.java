package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private RecordReposControllerProviding recordRepositoriesController;

    public Context(RecordReposControllerProviding recordRepositoriesController) {
        this.recordRepositoriesController = recordRepositoriesController;
    }

    public RecordReposControllerProviding getRecordRepositoriesController() {
        return recordRepositoriesController;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        PrivateExternalStorage localStorage = new PrivateExternalStorage(context);
        RecordReposControllerProviding recordRepositoriesController =
                new RecordRepositoriesController(localStorage);
        Context newContext = new Context(recordRepositoriesController);
        localStorage.setListener(recordRepositoriesController);

        GoogleDriveService googleDriveService = new GoogleDriveService(context);
        googleDriveService.createAppFolderIfNotExists();

        recordRepositoriesController.addCloudService(googleDriveService);
        googleDriveService.setListener(recordRepositoriesController);

        return newContext;
    }
}
