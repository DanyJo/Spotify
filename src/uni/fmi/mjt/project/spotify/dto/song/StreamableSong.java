package uni.fmi.mjt.project.spotify.dto.song;

import uni.fmi.mjt.project.spotify.dto.song.format.Format;
import uni.fmi.mjt.project.spotify.exception.song.SongDoesntExistException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class StreamableSong implements Serializable {
    public static final String EXTENSION = ".wav";
    private static final String SONG_PATH_PATTERN = "%s" + File.separator + "%s" + EXTENSION;
    private final String name;
    private final String path;
    private final Format format;

    public StreamableSong(String name, String songsDirectory) throws SongDoesntExistException {
        this.name = name;
        this.path = String.format(SONG_PATH_PATTERN, songsDirectory, name);

        try {
            this.format = getSongFormat(this.path);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new SongDoesntExistException("Sorry, song " + name + " doesn't exist in the dataset!", e);
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Format getFormat() {
        return format;
    }

    private Format getSongFormat(String path) throws UnsupportedAudioFileException, IOException {
        AudioFormat audioFormat = AudioSystem.getAudioInputStream(new File(path)).getFormat();

        return new Format(audioFormat);
    }
}
