package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import java.io.File;

import androidx.annotation.NonNull;
import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandlerProviding;

class LocalStorageUtils {
    private  LocalStorageUtils() {}

    static File[] getVideoTitles(@NonNull File directory,
                                 @NonNull VideoTitleHandlerProviding titleHandler) {
        return directory.listFiles(titleHandler.getVideoTitleFilter());
    }

    static File[] getFragmentTitles(@NonNull File directory,
                                    @NonNull VideoTitleHandlerProviding titleHandler) {
        return directory.listFiles(titleHandler.getFragmentTitleFilter());
    }

    static boolean isVideo(@NonNull File file,
                           @NonNull VideoTitleHandlerProviding titleHandler) {
        String extension = getFileExtension(file);
        if (extension == null) {
            return  false;
        }
        String nameWithExtension = file.getName();
        String filename = nameWithExtension.substring(0, nameWithExtension.indexOf("."));
        return extension.equals(titleHandler.getExtension()) && titleHandler.isVideoTitle(filename);
    }

    static boolean isFragmentedVideo(@NonNull File file,
                                 @NonNull VideoTitleHandlerProviding titleHandler) {
        return titleHandler.isVideoTitle(file.getName()) &&
                file.listFiles(titleHandler.getFragmentTitleFilter()).length > 0;
    }

    private static String getFileExtension(File file) {
        String filename = file.getName();
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        } else {
            return null;
        }
    }
}
