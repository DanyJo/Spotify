package uni.fmi.mjt.project.spotify.exception;

public class SpotifyException extends Exception {
    public SpotifyException(String message) {
        super(message);
    }

    public SpotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
