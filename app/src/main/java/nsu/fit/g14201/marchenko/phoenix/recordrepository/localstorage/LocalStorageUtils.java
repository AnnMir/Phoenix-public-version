package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.support.annotation.NonNull;

import java.io.File;

import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandlerProviding;

class LocalStorageUtils {
    private  LocalStorageUtils() {}

    static File[] getVideoTitles(@NonNull File directory,
                                 @NonNull VideoTitleHandlerProviding titleHandler) {
        return directory.listFiles(titleHandler.getVideoTitleFilter());
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

    /**
     * @return 0 if file isn't a fragmented video, number of fragments otherwise
     */
    static int isFragmentedVideo(@NonNull File file,
                                 @NonNull VideoTitleHandlerProviding titleHandler) {
        if (!titleHandler.isVideoTitle(file.getName())) {
            return 0;
        }
        return file.listFiles(titleHandler.getFragmentTitleFilter()).length;
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
