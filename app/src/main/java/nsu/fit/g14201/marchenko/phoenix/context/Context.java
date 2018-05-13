package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private RecordReposControllerProviding recordRepositoriesController;
    private String directoryPattern;

    public Context(RecordReposControllerProviding recordRepositoriesController,
                   String directoryPattern) {
        this.recordRepositoriesController = recordRepositoriesController;
        this.directoryPattern = directoryPattern;
    }

    public RecordReposControllerProviding getRecordRepositoriesController() {
        return recordRepositoriesController;
    }

    public String getDirectoryPattern() {
        return directoryPattern;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        PrivateExternalStorage localStorage = new PrivateExternalStorage(
                context,
                "[\\d]{2}-[\\d]{2}-[\\d]{4}_[\\d]{2}:[\\d]{2}:[\\d]{2}"
        );
        RecordReposControllerProviding recordRepositoriesController =
                new RecordRepositoriesController(localStorage);
        Context newContext = new Context(recordRepositoriesController, "dd-MM-yyyy_HH:mm:ss");
        localStorage.setListener(recordRepositoriesController);

        GoogleDriveService googleDriveService = new GoogleDriveService(context);
        googleDriveService.createAppFolderIfNotExists();

        recordRepositoriesController.addCloudService(googleDriveService);
        googleDriveService.setListener(recordRepositoriesController);

        return newContext;
    }
}
