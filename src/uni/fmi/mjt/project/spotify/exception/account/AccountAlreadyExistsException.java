package uni.fmi.mjt.project.spotify.exception.account;

import uni.fmi.mjt.project.spotify.exception.SpotifyException;

public class AccountAlreadyExistsException extends SpotifyException {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }

    public AccountAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
