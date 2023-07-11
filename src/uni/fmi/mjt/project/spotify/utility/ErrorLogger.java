package uni.fmi.mjt.project.spotify.utility;

import java.io.IOException;
import java.io.PrintWriter;

public class ErrorLogger {
    public static void writeErrorLogsToFile(Exception e, String path) {
        try (var fileWriter = new PrintWriter(path)) {
            e.printStackTrace(fileWriter);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
