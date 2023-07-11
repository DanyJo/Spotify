package uni.fmi.mjt.project.spotify.exception.login;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class UserAlreadyLoggedInException extends SpotifyException {
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }

    public UserAlreadyLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}
