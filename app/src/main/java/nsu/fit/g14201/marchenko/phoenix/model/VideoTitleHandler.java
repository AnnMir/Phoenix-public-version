package nsu.fit.g14201.marchenko.phoenix.model;

import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public class VideoTitleHandler implements VideoTitleHandlerProviding {
    private final Pattern videoTitlePattern;
    private final Pattern fragmentTitlePattern;
    private final String extension;
    private final DateFormat dateFormat;

    public VideoTitleHandler(@NonNull String videoTitlePattern,
                             @NonNull String fragmentTitlePattern,
                             @NonNull String extension,
                             @NonNull String dateTimePattern) {
        this.videoTitlePattern = Pattern.compile(videoTitlePattern);
        this.fragmentTitlePattern = Pattern.compile(fragmentTitlePattern + extension);
        this.extension = extension;
        dateFormat = new SimpleDateFormat(dateTimePattern);
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public FileFilter getVideoTitleFilter() {
        return file -> videoTitlePattern.matcher(file.getName()).matches();
    }

    @Override
    public FileFilter getFragmentTitleFilter() {
        return file -> fragmentTitlePattern.matcher(file.getName()).matches();
    }

    @Override
    public String getNewVideoTitle() {
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    @Override
    public boolean isVideoTitle(@NonNull String filename) {
        return videoTitlePattern.matcher(filename).matches();
    }
}
