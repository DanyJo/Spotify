package uni.fmi.mjt.project.spotify.command;

import uni.fmi.mjt.project.spotify.exception.command.NoSuchCommandException;

import java.util.ArrayList;
import java.util.List;

public class CommandCreator {
    public static Command createCommand(String line) throws NoSuchCommandException {
        List<String> tokens = CommandCreator.splitCommand(line);
        try {
            String typeName = tokens.get(0).replace('-', '_');
            CommandType type = CommandType.valueOf(typeName.toUpperCase());

            return new Command(type, tokens.subList(1, tokens.size()));
        } catch (Exception e) {
            throw new NoSuchCommandException("Command with name \"" + tokens.get(0) +
                    "\" doesn't exist");
        }
    }

    private static List<String> splitCommand(String input) {
        final char separator = ' ';

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean insideQuote = false;

        for (char symbol : input.toCharArray()) {
            if (symbol == '"') {
                insideQuote = !insideQuote;
            }

            if (symbol == separator && !insideQuote) {
                addToList(tokens, getStringBuilderContent(sb));

                sb.delete(0, sb.length());
            } else {
                sb.append(symbol);
            }
        }

        addToList(tokens, getStringBuilderContent(sb));

        return tokens;
    }

    private static String getStringBuilderContent(StringBuilder sb) {
        return sb.toString().replace("\"", "").strip();
    }

    private static void addToList(List<String> list, String token) {
        if (!token.isEmpty()) {
            list.add(token);
        }
    }
}
