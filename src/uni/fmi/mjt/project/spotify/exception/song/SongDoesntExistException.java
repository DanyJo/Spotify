package uni.fmi.mjt.project.spotify.exception.song;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class SongDoesntExistException extends SpotifyException {
    public SongDoesntExistException(String message) {
        super(message);
    }

    public SongDoesntExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
