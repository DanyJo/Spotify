package uni.fmi.mjt.project.spotify.server;

import uni.fmi.mjt.project.spotify.DefaultSpotify;
import uni.fmi.mjt.project.spotify.StreamingMusicHandler;
import uni.fmi.mjt.project.spotify.command.Command;
import uni.fmi.mjt.project.spotify.command.CommandCreator;
import uni.fmi.mjt.project.spotify.command.CommandExecutor;
import uni.fmi.mjt.project.spotify.command.CommandType;
import uni.fmi.mjt.project.spotify.dto.request.ClientRequest;
import uni.fmi.mjt.project.spotify.dto.response.ServerResponse;
import uni.fmi.mjt.project.spotify.exception.command.NoSuchCommandException;
import uni.fmi.mjt.project.spotify.exception.ServerSideException;
import uni.fmi.mjt.project.spotify.utility.ObjectByteConvertor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyServer {
    private static final String ACCOUNTS_FILE_PATH = "Accounts" + File.separator + "Accounts.txt";
    private static final String HOST = "localhost";
    private static final int PORT = 44_444;
    private static final int STREAM_PORT = 44_445;
    private static final int BUFFER_SIZE = 1024;
    private static final int NUM_OF_THREADS = 10;
    private ByteBuffer buffer;
    private Selector selector;
    private final CommandExecutor commandExecutor;
    private final ExecutorService executor;
    private final FileReader accountsFileReader;
    private final FileWriter accountsFileWriter;

    public SpotifyServer() {
        try {
            accountsFileReader = new FileReader(ACCOUNTS_FILE_PATH);
            accountsFileWriter = new FileWriter(ACCOUNTS_FILE_PATH, true);
        } catch (IOException e) {
            throw new ServerSideException("Accounts file is inaccessible", e);
        }

        commandExecutor = new CommandExecutor(new DefaultSpotify(accountsFileReader, accountsFileWriter));
        executor = Executors.newFixedThreadPool(NUM_OF_THREADS + 1);
    }

    public void start() {
        try (var serverChannel = ServerSocketChannel.open();
             var musicStreamingSocket = new ServerSocket(STREAM_PORT)) {
            selector = Selector.open();
            configureServerChannel(serverChannel);

            buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                int channelsReady = selector.select();

                if (channelsReady == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                resolveRequests(keyIterator, musicStreamingSocket);
            }
        } catch (IOException e) {
            throw new ServerSideException("A problem occurred while opening the server sockets", e);
        } finally {
            executor.shutdown();
            closeWritersAndReaders();
        }
    }

    private void closeWritersAndReaders() {
        try {
            accountsFileReader.close();
            accountsFileWriter.close();
        } catch (IOException e) {
            throw new ServerSideException("A problem occurred while closing the reader and writer" +
                    "for the file with the accounts", e);
        }
    }

    private void resolveRequests(Iterator<SelectionKey> keyIterator, ServerSocket musicStreamingSocket)
            throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (key.isAcceptable()) {
                acceptClient(key);
                System.out.println("Client has connected!");
            } else if (key.isReadable()) {
                completeClientCommunication(key, musicStreamingSocket);
            }

            keyIterator.remove();
        }
    }

    private void completeClientCommunication(SelectionKey key, ServerSocket musicStreamingSocket)
            throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        ClientRequest clientRequest = getClientInput(clientChannel);

        if (clientRequest == null) {
            System.out.println("Client has disconnected");
            return;
        }

        try {
            Command command = CommandCreator.createCommand(clientRequest.message());
            ServerResponse result = commandExecutor.execute(command, clientRequest.userEmail());

            if (result.getType().equals(CommandType.PLAY)) {
                streamSong(clientChannel, result, musicStreamingSocket);
            } else {
                writeToClient(clientChannel, ObjectByteConvertor.convertObjectToByteArray(result));
            }
        } catch (NoSuchCommandException e) {
            ServerResponse wrongServerResponse = ServerResponse.builder(CommandType.ERROR, e.getMessage()).build();

            writeToClient(clientChannel, ObjectByteConvertor.convertObjectToByteArray(wrongServerResponse));
        }
    }

    private void streamSongBytes(ServerSocket streamMusicServerSocket, String songPath) {
        Socket musicStreamingClientSocket;
        try {
            musicStreamingClientSocket = streamMusicServerSocket.accept();

            StreamingMusicHandler handler =
                    new StreamingMusicHandler(musicStreamingClientSocket, songPath, BUFFER_SIZE);

            executor.submit(handler);
        } catch (IOException e) {
            throw new ServerSideException("A problem occurred while trying to stream a song!", e);
        }
    }

    private void streamSong(SocketChannel clientChannel, ServerResponse result, ServerSocket musicStreamingSocket) {
        try {
            writeToClient(clientChannel, ObjectByteConvertor.convertObjectToByteArray(result));

            executor.submit(() -> streamSongBytes(musicStreamingSocket, result.getSong().getPath()));
        } catch (IOException e) {
            throw new ServerSideException("A problem occurred while trying to send the client message!", e);
        }
    }

    private void writeToClient(SocketChannel clientChannel, byte[] array) throws IOException {
        buffer.clear();
        buffer.put(array);
        buffer.flip();

        clientChannel.write(buffer);
    }

    private ClientRequest getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead < 0) {
            clientChannel.close();

            return null;
        }

        buffer.flip();
        byte[] clientInput = new byte[buffer.remaining()];
        buffer.get(clientInput);

        return (ClientRequest) ObjectByteConvertor.convertByteArrayToObject(clientInput);
    }

    private void acceptClient(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();

            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new ServerSideException("A problem occurred while accepting a connection!", e);
        }
    }

    private void configureServerChannel(ServerSocketChannel serverChannel) throws IOException {
        serverChannel.bind(new InetSocketAddress(HOST, PORT));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

    }
}
