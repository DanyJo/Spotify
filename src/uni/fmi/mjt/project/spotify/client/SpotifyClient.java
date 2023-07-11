package uni.fmi.mjt.project.spotify.client;

import uni.fmi.mjt.project.spotify.command.CommandType;
import uni.fmi.mjt.project.spotify.dto.request.ClientRequest;
import uni.fmi.mjt.project.spotify.dto.response.ServerResponse;
import uni.fmi.mjt.project.spotify.dto.song.format.Format;
import uni.fmi.mjt.project.spotify.utility.ErrorLogger;
import uni.fmi.mjt.project.spotify.utility.ObjectByteConvertor;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyClient {
    private static final String ERROR_LOGS_PATH = "Error_logs" + File.separator + "client_%s_error_logs.txt";
    private static final int PORT = 44_444;
    private static final int STREAM_PORT = 44_445;
    private static final String HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private SourceDataLine dataLine = null;
    private String user = "";

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            connectClientToServer(socketChannel);

            communicateWithServer(socketChannel);
        } catch (UncheckedIOException e) {
            System.out.println(e.getMessage());

            ErrorLogger.writeErrorLogsToFile(e, String.format(ERROR_LOGS_PATH, user));
        } catch (Exception e) {
            System.out.println("Something went wrong");

            ErrorLogger.writeErrorLogsToFile(e, String.format(ERROR_LOGS_PATH, user));
        }
    }

    private void connectClientToServer(SocketChannel socketChannel) {
        try {
            socketChannel.connect(new InetSocketAddress(HOST, PORT));
            System.out.println("Welcome to Spotify. Please login/register in order to use its functions.");
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to connect to server! Please try again.", e);
        }
    }

    private void communicateWithServer(SocketChannel socketChannel) {
        String commandMessage;
        ServerResponse response;

        try (ExecutorService executor = Executors.newSingleThreadExecutor();
             Scanner scanner = new Scanner(System.in)) {
            while (true) {
                commandMessage = readClientInput(scanner);

                response = sendServerRequest(socketChannel, commandMessage);

                if (response.getType().equals(CommandType.STOP)) {
                    if (!stopSongIfNeeded()) {
                        continue;
                    }
                }

                System.out.println(response.getMessage());

                if (response.getType().equals(CommandType.DISCONNECT)) {
                    stopSongIfNeeded();
                    break;
                }

                if (response.getType().equals(CommandType.LOGIN) || response.getType().equals(CommandType.REGISTER)) {
                    user = response.getEmail();
                } else if (response.getType().equals(CommandType.PLAY)) {
                    stopSongIfNeeded();
                    Format songFormat = response.getSong().getFormat();
                    executor.submit(() -> streamSong(songFormat));
                }
            }
        }
    }

    private String readClientInput(Scanner scanner) {
        String input = "";

        while (input.isBlank()) {
            input = scanner.nextLine();
        }

        return input;
    }

    private ServerResponse sendServerRequest(SocketChannel socketChannel, String commandMessage) {
        ClientRequest input = new ClientRequest(user, commandMessage);
        writeToServer(socketChannel, ObjectByteConvertor.convertObjectToByteArray(input));

        return (ServerResponse) ObjectByteConvertor.convertByteArrayToObject(getServerReply(socketChannel));
    }

    private void writeToServer(SocketChannel socketChannel, byte[] bytes) {
        try {
            buffer.clear();
            buffer.put(bytes);
            buffer.flip();
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "There was a problem with the server receiving data! Please restart and try again.", e);
        }
    }

    private byte[] getServerReply(SocketChannel socketChannel) {
        try {
            buffer.clear();
            socketChannel.read(buffer);
            buffer.flip();

            byte[] reply = new byte[buffer.remaining()];
            buffer.get(reply);

            return reply;
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "There was a problem with retrieving data from the server! Please restart and try again.", e);
        }
    }

    private boolean stopSongIfNeeded() {
        if (dataLine != null && dataLine.isRunning()) {
            dataLine.stop();
            dataLine.flush();

            return true;
        }

        return false;
    }

    private SourceDataLine getDataLine(Format format) throws IOException, LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format.getAsAudioFormat());

        return (SourceDataLine) AudioSystem.getLine(info);
    }

    private void streamSong(Format format) {
        try (var streamSocket = new Socket(HOST, STREAM_PORT)) {
            dataLine = getDataLine(format);

            dataLine.open();
            dataLine.start();

            try (var reader = new BufferedInputStream(streamSocket.getInputStream())) {
                byte[] music = new byte[BUFFER_SIZE];
                int readBytes;

                while ((readBytes = reader.read(music)) != -1) {
                    dataLine.write(music, 0, readBytes);
                }

                dataLine = null;
            }
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException("Stream socket couldn't be created", e);
        }
    }
}