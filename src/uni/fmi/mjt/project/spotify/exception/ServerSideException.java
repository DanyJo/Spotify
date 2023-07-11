package uni.fmi.mjt.project.spotify.exception;

public class ServerSideException extends RuntimeException {
    public ServerSideException(String message) {
        super(message);
    }

    public ServerSideException(String message, Throwable cause) {
        super(message, cause);
    }
}
