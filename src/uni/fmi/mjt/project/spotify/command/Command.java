package uni.fmi.mjt.project.spotify.command;

import java.util.List;

public record Command(CommandType type, List<String> arguments) {
    public Command {
        if (type == null) {
            throw new IllegalArgumentException("Trying to pass a command with type null!");
        }

        if (arguments == null) {
            throw new IllegalArgumentException(
                    "Trying to pass name arguments as null! " +
                            "If the name has no arguments, then empty List should be passed!");
        }
    }
}
