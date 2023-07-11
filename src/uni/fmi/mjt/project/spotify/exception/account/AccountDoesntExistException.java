package uni.fmi.mjt.project.spotify.exception.account;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class AccountDoesntExistException extends SpotifyException {
    public AccountDoesntExistException(String message) {
        super(message);
    }

    public AccountDoesntExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
