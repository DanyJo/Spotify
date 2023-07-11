package uni.fmi.mjt.project.spotify.exception.command;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class NoSuchCommandException extends SpotifyException {
    public NoSuchCommandException(String message) {
        super(message);
    }

    public NoSuchCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
