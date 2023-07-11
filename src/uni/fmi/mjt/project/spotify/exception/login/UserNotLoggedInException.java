package uni.fmi.mjt.project.spotify.exception.login;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class UserNotLoggedInException extends SpotifyException {
    public UserNotLoggedInException(String message) {
        super(message);
    }

    public UserNotLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}
