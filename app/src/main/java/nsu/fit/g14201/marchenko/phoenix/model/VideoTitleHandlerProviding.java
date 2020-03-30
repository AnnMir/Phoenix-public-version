package nsu.fit.g14201.marchenko.phoenix.model;

import java.io.FileFilter;

import androidx.annotation.NonNull;

public interface VideoTitleHandlerProviding {
    String getExtension();

    String getNewVideoTitle();

    FileFilter getVideoTitleFilter();

    FileFilter getFragmentTitleFilter();

    boolean isVideoTitle(@NonNull String filename);
}
