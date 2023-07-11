package uni.fmi.mjt.project.spotify.command;

import org.junit.jupiter.api.Test;
import uni.fmi.mjt.project.spotify.exception.command.NoSuchCommandException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandCreatorTest {
    @Test
    void createCommandOneWordCommand() throws NoSuchCommandException {
        Command command = CommandCreator.createCommand("login   asd@abv.bg   123");

        assertEquals(CommandType.LOGIN, command.type(), "Command is created with the correct type");
        assertIterableEquals(List.of("asd@abv.bg", "123"), command.arguments(),
                "Command is created with the correct arguments");
    }

    @Test
    void createCommandMultipleWordCommand() throws NoSuchCommandException {
        Command command = CommandCreator.createCommand("show-playlist playlist");

        assertEquals(CommandType.SHOW_PLAYLIST, command.type(), "Command is created with the correct type");
        assertIterableEquals(List.of("playlist"), command.arguments(),
                "Command is created with the correct arguments");
    }

    @Test
    void createCommandMultipleWordArgument() throws NoSuchCommandException {
        Command command = CommandCreator.createCommand("play \"a song with multiple words\"");

        assertEquals(CommandType.PLAY, command.type(), "Command is created with the correct type");
        assertIterableEquals(List.of("a song with multiple words"), command.arguments(),
                "Command is created with the correct arguments");
    }

    @Test
    void createCommandWrongCommand() {
        assertThrows(NoSuchCommandException.class, () -> CommandCreator.createCommand("not-a-command asd"),
                "Throws NoSuchCommandException when trying to create an invalid command");
    }
}
