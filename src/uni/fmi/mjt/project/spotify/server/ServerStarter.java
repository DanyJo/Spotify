package uni.fmi.mjt.project.spotify.server;

import uni.fmi.mjt.project.spotify.utility.ErrorLogger;

import java.io.File;

public class ServerStarter {
    private static final String ERROR_LOGS_PATH = "Error_logs" + File.separator + "server_error_logs.txt";

    public static void main(String[] args) {
        try {
            SpotifyServer server = new SpotifyServer();

            server.start();
        } catch (Exception e) {
            System.out.println("Something went wrong :(");

            ErrorLogger.writeErrorLogsToFile(e, ERROR_LOGS_PATH);
        }
    }
}
