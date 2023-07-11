package uni.fmi.mjt.project.spotify.exception.account;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class IncorrectLoginCredentialsException extends SpotifyException {
    public IncorrectLoginCredentialsException(String message) {
        super(message);
    }

    public IncorrectLoginCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
