package uni.fmi.mjt.project.spotify;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class StreamingMusicHandler implements Runnable {
    private final Socket musicStreamingClientSocket;
    private final String songPath;
    private final int bufferSize;

    public StreamingMusicHandler(Socket musicStreamingClientSocket, String songPath, int bufferSize) {
        this.musicStreamingClientSocket = musicStreamingClientSocket;
        this.songPath = songPath;
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        byte[] music = new byte[bufferSize];
        try (var musicFile = new BufferedInputStream(new FileInputStream(songPath));
             var send = new BufferedOutputStream(musicStreamingClientSocket.getOutputStream())) {

            System.out.println("Streaming music " + songPath);

            int bytesRead;

            while ((bytesRead = musicFile.read(music, 0, bufferSize)) != -1) {
                send.write(music, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Stream server didnt start", e);
        }
    }
}
