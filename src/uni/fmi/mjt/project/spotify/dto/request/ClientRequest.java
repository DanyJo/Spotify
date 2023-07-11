package uni.fmi.mjt.project.spotify.dto.request;

import java.io.Serializable;

public record ClientRequest(String userEmail, String message) implements Serializable {
}
