package uni.fmi.mjt.project.spotify.dto.response;

import uni.fmi.mjt.project.spotify.command.CommandType;
import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private final CommandType type;
    private final String message;

    private final String email;
    private final StreamableSong song;

    private ServerResponse(ResponseBuilder builder) {
        this.type = builder.type;
        this.message = builder.message;
        this.email = builder.email;
        this.song = builder.song;
    }

    public String getMessage() {
        return message;
    }

    public CommandType getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public StreamableSong getSong() {
        return song;
    }

    public static ResponseBuilder builder(CommandType type, String message) {
        return new ResponseBuilder(type, message);
    }

    public static class ResponseBuilder implements Serializable {
        private final CommandType type;
        private final String message;
        private String email = "";
        private StreamableSong song = null;

        private ResponseBuilder(CommandType type, String message) {
            this.type = type;
            this.message = message;
        }

        public ResponseBuilder setEmail(String email) {
            this.email = email;

            return this;
        }

        public ResponseBuilder setSong(StreamableSong song) {
            this.song = song;

            return this;
        }

        public ServerResponse build() {
            return new ServerResponse(this);
        }
    }
}
