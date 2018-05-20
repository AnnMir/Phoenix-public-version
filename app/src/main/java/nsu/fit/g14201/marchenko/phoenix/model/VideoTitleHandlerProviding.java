package nsu.fit.g14201.marchenko.phoenix.model;

import android.support.annotation.NonNull;

import java.io.FileFilter;

public interface VideoTitleHandlerProviding {
    String getExtension();

    String getNewVideoTitle();

    FileFilter getVideoTitleFilter();

    FileFilter getFragmentTitleFilter();

    boolean isVideoTitle(@NonNull String filename);
}
