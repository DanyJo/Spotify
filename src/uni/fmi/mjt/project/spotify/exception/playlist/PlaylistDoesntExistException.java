package uni.fmi.mjt.project.spotify.exception.playlist;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class PlaylistDoesntExistException extends SpotifyException {
    public PlaylistDoesntExistException(String message) {
        super(message);
    }

    public PlaylistDoesntExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
