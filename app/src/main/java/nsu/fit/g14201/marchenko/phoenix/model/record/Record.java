package nsu.fit.g14201.marchenko.phoenix.model.record;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

public class Record {
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormat.forPattern("dd-MM-yyyy_HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
    private static final Pattern VIDEO_TITLE_PATTERN = Pattern.compile(
            "[\\d]{2}-[\\d]{2}-[\\d]{4}_[\\d]{2}:[\\d]{2}:[\\d]{2}");

    private File path = null;

    private String userNamedTitle = null;
    private DateTime dateTime;
    private boolean isInCloud = false;

    public Record(@NonNull File path) {
        this.path = path;
        dateTime = INPUT_FORMATTER.parseDateTime(path.getName()); // TODO: Get data if file was renamed
    }

    public Record(@NonNull String title, @NonNull Date date) {
        dateTime = new DateTime(date);
        if (!VIDEO_TITLE_PATTERN.matcher(title).matches()) {
            userNamedTitle = title;
        }
        isInCloud = true;
    }

    public String getTitle() {
        return path == null ? getDateTime() : path.getName();
    }

    public String getUserNamedTitle() {
        return userNamedTitle;
    }

    public boolean isHereLocally() {
        return path != null;
    }

    public boolean isInCloud() {
        return isInCloud;
    }

    public String getDateTime() {
        return OUTPUT_FORMATTER.print(dateTime);
    }

    public File getPath() {
        return path;
    }
}
