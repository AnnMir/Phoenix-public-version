package nsu.fit.g14201.marchenko.phoenix.videoprocessing;

import androidx.annotation.NonNull;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class VideoJoiner {
    public static void joinFragments(
            @NonNull File path,
            @NonNull String[] videoUris,
            @NonNull File resultPath
    ) throws IOException {
        if (videoUris.length < 2) {
            return;
        }

        String pathWithSeparator = path + File.separator;
        List<Movie> inMovies = new ArrayList<>(videoUris.length);
        for (String videoUri : videoUris) {
            inMovies.add(MovieCreator.build(pathWithSeparator + videoUri));
        }

        List<Track> videoTracks = new LinkedList<>();
        List<Track> audioTracks = new LinkedList<>();

        for (Movie movie : inMovies) {
            for (Track track : movie.getTracks()) {
                if (track.getHandler().equals("soun")) {
                    audioTracks.add(track);
                }
                if (track.getHandler().equals("vide")) {
                    videoTracks.add(track);
                }
            }
        }

        Movie result = new Movie();

        if (!audioTracks.isEmpty()) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (!videoTracks.isEmpty()) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fileChannel = new RandomAccessFile(resultPath, "rw").getChannel();
        out.writeContainer(fileChannel);
        fileChannel.close();
    }
}
