package uni.fmi.mjt.project.spotify.exception.playlist;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class PlaylistAlreadyContainsSongException extends SpotifyException {
    public PlaylistAlreadyContainsSongException(String message) {
        super(message);
    }

    public PlaylistAlreadyContainsSongException(String message, Throwable cause) {
        super(message, cause);
    }
}
