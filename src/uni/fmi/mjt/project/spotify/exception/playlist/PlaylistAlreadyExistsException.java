package uni.fmi.mjt.project.spotify.exception.playlist;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class PlaylistAlreadyExistsException extends SpotifyException {
    public PlaylistAlreadyExistsException(String message) {
        super(message);
    }

    public PlaylistAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
