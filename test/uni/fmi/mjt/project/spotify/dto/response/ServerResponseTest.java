package uni.fmi.mjt.project.spotify.dto.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.fmi.mjt.project.spotify.command.CommandType;
import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ServerResponseTest {
    private ServerResponse response;
    private static final CommandType type = CommandType.DISCONNECT;
    private static final String message = "disconnected";
    private static final String email = "asd@abv.bg";

    @Mock
    private StreamableSong songMock;

    @Test
    void testBuilderOnlyCommandTypeAndMessage() {
        response = ServerResponse.builder(type, message).build();

        assertEquals(type, response.getType(), "Checks if the type is set correctly");
        assertEquals(message, response.getMessage(), "Checks if the message is set correctly");
        assertTrue(response.getEmail().isEmpty(), "Checks if the email is set correctly to an empty string");
        assertNull(response.getSong(), "Checks if the song is set correctly to null");
    }

    @Test
    void testBuilderCommandTypeMessageAndEmail() {
        response = ServerResponse.builder(type, message).setEmail(email).build();

        assertEquals(type, response.getType(), "Checks if the type is set correctly");
        assertEquals(message, response.getMessage(), "Checks if the message is set correctly");
        assertEquals(email, response.getEmail(), "Checks if the email is set correctly");
        assertNull(response.getSong(), "Checks if the song is set correctly to null");
    }

    @Test
    void testBuilderCommandTypeMessageAndSong() {
        response = ServerResponse.builder(type, message).setSong(songMock).build();

        assertEquals(type, response.getType(), "Checks if the type is set correctly");
        assertEquals(message, response.getMessage(), "Checks if the message is set correctly");
        assertTrue(response.getEmail().isEmpty(), "Checks if the email is set correctly to an empty string");
        assertEquals(songMock, response.getSong(), "Checks if the song is set correctly");
    }

    @Test
    void testBuilderCommandTypeMessageEmailAndSong() {
        response = ServerResponse.builder(type, message).setEmail(email).setSong(songMock).build();

        assertEquals(type, response.getType(), "Checks if the type is set correctly");
        assertEquals(message, response.getMessage(), "Checks if the message is set correctly");
        assertEquals(email, response.getEmail(), "Checks if the email is set correctly");
        assertEquals(songMock, response.getSong(), "Checks if the song is set correctly");
    }
}
