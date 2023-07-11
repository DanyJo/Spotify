package uni.fmi.mjt.project.spotify.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.fmi.mjt.project.spotify.Spotify;
import uni.fmi.mjt.project.spotify.dto.response.ServerResponse;
import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;
import uni.fmi.mjt.project.spotify.exception.ServerSideException;
import uni.fmi.mjt.project.spotify.exception.SpotifyException;
import uni.fmi.mjt.project.spotify.exception.account.AccountAlreadyExistsException;
import uni.fmi.mjt.project.spotify.exception.account.AccountDoesntExistException;
import uni.fmi.mjt.project.spotify.exception.account.IncorrectLoginCredentialsException;
import uni.fmi.mjt.project.spotify.exception.account.InvalidEmailFormatException;
import uni.fmi.mjt.project.spotify.exception.login.UserAlreadyLoggedInException;
import uni.fmi.mjt.project.spotify.exception.login.UserNotLoggedInException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistAlreadyContainsSongException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistAlreadyExistsException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistDoesntExistException;
import uni.fmi.mjt.project.spotify.exception.song.SongDoesntExistException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {
    private static final String ERROR_MESSAGE_PATTERN = "There was an error!" + System.lineSeparator() + "%s";
    private static final String ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS = "Insufficient arguments when trying to %s, " +
            "expected %d argument\\s: %s";

    private static final String email = "asd@abv.bg";
    private static final String password = "123";
    private static final List<String> keywords = List.of("song", "test");
    private static final String song1 = "song1";
    private static final String song2 = "song2";
    private static final String numberAsString = "100";
    private static final int number = 100;
    private static final String playlistName = "playlist";

    @Mock
    private Spotify spotifyMock;
    @InjectMocks
    private CommandExecutor commandExecutor;

    //-------------Register-------------

    @Test
    void testExecuteRegisterErrorInsufficientArguments() {
        Command command = new Command(CommandType.REGISTER, List.of("one arg"));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS, "register", 2, "email and password");

        assertEquals(CommandType.ERROR, response.getType(), "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(), "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteRegisterErrorIllegalArgumentException() throws SpotifyException {
        doThrow(new InvalidEmailFormatException("error message")).when(spotifyMock).register(email, password);

        Command command = new Command(CommandType.REGISTER, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteRegisterErrorInvalidEmailFormatException() throws SpotifyException {
        doThrow(new InvalidEmailFormatException("error message")).when(spotifyMock).register(email, password);

        Command command = new Command(CommandType.REGISTER, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteRegisterErrorAccountAlreadyExistsException() throws SpotifyException {
        doThrow(new AccountAlreadyExistsException("error message")).when(spotifyMock).register(email, password);

        Command command = new Command(CommandType.REGISTER, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteRegisterThrowsServerSideException() throws SpotifyException {
        doThrow(new UncheckedIOException(new IOException())).when(spotifyMock).register(email, password);

        Command command = new Command(CommandType.REGISTER, List.of(email, password));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.register method throws exception that is " +
                        "different from AccountAlreadyExistsException, InvalidEmailFormatException or" +
                        "InvalidEmailFormatException");
    }

    @Test
    void testExecuteRegister() {
        Command command = new Command(CommandType.REGISTER, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Account was registered successfully";

        assertEquals(CommandType.REGISTER, response.getType(),
                "Checks if the returned command type is REGISTER");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
        assertEquals(email, response.getEmail(),
                "Checks if the returned message is correct");
    }

    //-------------Login-------------
    @Test
    void testExecuteLoginErrorInsufficientArguments() {
        Command command = new Command(CommandType.LOGIN, List.of("one arg"));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "login", 2, "email and password");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteLoginErrorIllegalArgumentException() throws SpotifyException {
        doThrow(new InvalidEmailFormatException("error message")).when(spotifyMock).login(email, password);

        Command command = new Command(CommandType.LOGIN, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteLoginUserAlreadyLoggedInException() throws SpotifyException {
        doThrow(new UserAlreadyLoggedInException("error message")).when(spotifyMock).login(email, password);

        Command command = new Command(CommandType.LOGIN, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteLoginErrorInvalidEmailFormatException() throws SpotifyException {
        doThrow(new InvalidEmailFormatException("error message")).when(spotifyMock).login(email, password);

        Command command = new Command(CommandType.LOGIN, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteLoginErrorAccountDoesntExistException() throws SpotifyException {
        doThrow(new AccountDoesntExistException("error message")).when(spotifyMock).login(email, password);

        Command command = new Command(CommandType.LOGIN, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteLoginErrorIncorrectLoginCredentialsException() throws SpotifyException {
        doThrow(new IncorrectLoginCredentialsException("error message")).when(spotifyMock).login(email, password);

        Command command = new Command(CommandType.LOGIN, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }


    @Test
    void testExecuteLoginThrowsServerSideException() throws SpotifyException {

        doThrow(new UncheckedIOException(new IOException())).when(spotifyMock).login(email, password);

        Command command = new Command(CommandType.LOGIN, List.of(email, password));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.login method throws exception that is " +
                        "different from IllegalArgumentException, UserAlreadyLoggedInException, " +
                        "InvalidEmailFormatException, AccountDoesntExistException, " +
                        "IncorrectLoginCredentialsException");
    }

    @Test
    void testExecuteLogin() {
        Command command = new Command(CommandType.LOGIN, List.of(email, password));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Welcome " + email;

        assertEquals(CommandType.LOGIN, response.getType(),
                "Checks if the returned command type is LOGIN");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
        assertEquals(email, response.getEmail(),
                "Checks if the returned email is correct");
    }

    //-------------Disconnect-------------
    @Test
    void testExecuteDisconnect() {
        Command command = new Command(CommandType.DISCONNECT, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Disconnected";

        assertEquals(CommandType.DISCONNECT, response.getType(),
                "Checks if the returned command type is DISCONNECT");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    //-------------Search-------------
    @Test
    void testExecuteSearchErrorInsufficientArguments() {
        Command command = new Command(CommandType.SEARCH, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "search for songs", 1, "keywords");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteSearchErrorIllegalArgumentException() throws SpotifyException {
        when(spotifyMock.search(keywords, email)).thenThrow(new IllegalArgumentException("error message"));

        Command command = new Command(CommandType.SEARCH, keywords);
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteSearchErrorUserNotLoggedInException() throws SpotifyException {
        when(spotifyMock.search(keywords, email)).thenThrow(new UserNotLoggedInException("error message"));

        Command command = new Command(CommandType.SEARCH, keywords);
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteSearchThrowsServerSideException() throws SpotifyException {
        when(spotifyMock.search(keywords, email)).thenThrow(new UncheckedIOException(new IOException()));
        Command command = new Command(CommandType.SEARCH, keywords);

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.search method throws exception that is " +
                        "different fromIllegalArgumentException or UserNotLoggedInException");
    }

    @Test
    void testExecuteSearchNoSongsContainKeywords() throws SpotifyException {
        when(spotifyMock.search(keywords, email)).thenReturn(Set.of());

        Command command = new Command(CommandType.SEARCH, keywords);
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "No songs were found containing the keywords";

        assertEquals(CommandType.SEARCH, response.getType(),
                "Checks if the returned command type is SEARCH");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    @Test
    void testExecuteSearchSongsContainKeywords() throws SpotifyException {
        when(spotifyMock.search(keywords, email)).thenReturn(Set.of(song1, song2));

        Command command = new Command(CommandType.SEARCH, keywords);
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "These are the songs that were found using the keywords:" + System.lineSeparator() +
                "\t" + song1 + System.lineSeparator() +
                "\t" + song2;

        assertEquals(CommandType.SEARCH, response.getType(),
                "Checks if the returned command type is SEARCH");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    //-------------Top-------------
    @Test
    void testExecuteTopErrorInsufficientArguments() {
        Command command = new Command(CommandType.TOP, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "get top songs", 1, "count of top songs");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteTopErrorIllegalArgumentException() throws SpotifyException {
        when(spotifyMock.top(number, email)).thenThrow(new IllegalArgumentException("error message"));

        Command command = new Command(CommandType.TOP, List.of(numberAsString));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteTopErrorUserNotLoggedInException() throws SpotifyException {
        when(spotifyMock.top(number, email)).thenThrow(new UserNotLoggedInException("error message"));

        Command command = new Command(CommandType.TOP, List.of(numberAsString));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteTopThrowsServerSideException() throws SpotifyException {
        when(spotifyMock.top(number, email)).thenThrow(new UncheckedIOException(new IOException()));
        Command command = new Command(CommandType.TOP, List.of(numberAsString));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.top method throws exception that is " +
                        "different fromIllegalArgumentException or UserNotLoggedInException");
    }

    @Test
    void testExecuteTopNoSongsInDataset() throws SpotifyException {
        when(spotifyMock.top(number, email)).thenReturn(List.of());

        Command command = new Command(CommandType.TOP, List.of(numberAsString));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "No songs have been played";

        assertEquals(CommandType.TOP, response.getType(),
                "Checks if the returned command type is TOP");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    @Test
    void testExecuteTopSongsInDataset() throws SpotifyException {
        when(spotifyMock.top(number, email)).thenReturn(List.of(song1, song2));

        Command command = new Command(CommandType.TOP, List.of(numberAsString));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Most played songs:" + System.lineSeparator() +
                "\t" + song1 + System.lineSeparator() +
                "\t" + song2;

        assertEquals(CommandType.TOP, response.getType(),
                "Checks if the returned command type is TOP");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    //-------------Create playlist-------------
    @Test
    void testExecuteCreatePlaylistErrorInsufficientArguments() {
        Command command = new Command(CommandType.CREATE_PLAYLIST, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "create a playlist", 1, "playlist name");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteCreatePlaylistErrorIllegalArgumentException() throws SpotifyException {
        doThrow(new IllegalArgumentException("error message")).when(spotifyMock).createPlaylist(playlistName, email);

        Command command = new Command(CommandType.CREATE_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteCreatePlaylistErrorUserNotLoggedInException() throws SpotifyException {
        doThrow(new UserNotLoggedInException("error message")).when(spotifyMock).createPlaylist(playlistName, email);

        Command command = new Command(CommandType.CREATE_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteCreatePlaylistErrorPlaylistAlreadyExistsException() throws SpotifyException {
        doThrow(new PlaylistAlreadyExistsException("error message"))
                .when(spotifyMock).createPlaylist(playlistName, email);

        Command command = new Command(CommandType.CREATE_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteCreatePlaylistThrowsServerSideException() throws SpotifyException {
        doThrow(new UncheckedIOException(new IOException()))
                .when(spotifyMock).createPlaylist(playlistName, password);

        Command command = new Command(CommandType.CREATE_PLAYLIST, List.of(playlistName));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.register method throws exception that is " +
                        "different from IllegalArgumentException, UserNotLoggedInException or " +
                        "PlaylistAlreadyExistsException");
    }

    @Test
    void testExecuteCreatePlaylist() {
        Command command = new Command(CommandType.CREATE_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Playlist '" + playlistName + "' was created";


        assertEquals(CommandType.CREATE_PLAYLIST, response.getType(),
                "Checks if the returned command type is CREATE_PLAYLIST");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    //-------------AddSongToPlaylist-------------
    @Test
    void testExecuteAddSongToPlaylistErrorInsufficientArguments() {
        Command command = new Command(CommandType.ADD_SONG_TO, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "add song to playlist", 2, "playlist name and song name");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteAddSongToPlaylistErrorIllegalArgumentException() throws SpotifyException {
        doThrow(new IllegalArgumentException("error message"))
                .when(spotifyMock).addSongToPlaylist(playlistName, song1, email);

        Command command = new Command(CommandType.ADD_SONG_TO, List.of(playlistName, song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteAddSongToPlaylistErrorUserNotLoggedInException() throws SpotifyException {
        doThrow(new UserNotLoggedInException("error message"))
                .when(spotifyMock).addSongToPlaylist(playlistName, song1, email);

        Command command = new Command(CommandType.ADD_SONG_TO, List.of(playlistName, song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteAddSongToPlaylistErrorPlaylistAlreadyContainsSongException() throws SpotifyException {
        doThrow(new PlaylistAlreadyContainsSongException("error message"))
                .when(spotifyMock).addSongToPlaylist(playlistName, song1, email);

        Command command = new Command(CommandType.ADD_SONG_TO, List.of(playlistName, song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteAddSongToPlaylistErrorPlaylistDoesntExistException() throws SpotifyException {
        doThrow(new PlaylistDoesntExistException("error message"))
                .when(spotifyMock).addSongToPlaylist(playlistName, song1, email);

        Command command = new Command(CommandType.ADD_SONG_TO, List.of(playlistName, song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteAddSongToPlaylistThrowsServerSideException() throws SpotifyException {
        doThrow(new UncheckedIOException(new IOException()))
                .when(spotifyMock).addSongToPlaylist(playlistName, song1, email);

        Command command = new Command(CommandType.ADD_SONG_TO, List.of(playlistName, song1));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.register method throws exception that is " +
                        "different from IllegalArgumentException, UserNotLoggedInException," +
                        " PlaylistAlreadyContainsSongException or PlaylistDoesntExistException");
    }

    @Test
    void testExecuteAddSongToPlaylist() {
        Command command = new Command(CommandType.ADD_SONG_TO, List.of(playlistName, song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "'" + song1 + "' was added successfully to '" + playlistName + "'";

        assertEquals(CommandType.ADD_SONG_TO, response.getType(),
                "Checks if the returned command type is ADD_SONG_TO");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    //-------------Show playlist-------------
    @Test
    void testExecuteShowPlaylistErrorInsufficientArguments() {
        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "show playlist", 1, "playlist name");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteShowPlaylistErrorIllegalArgumentException() throws SpotifyException {
        when(spotifyMock.showPlaylist(playlistName, email)).thenThrow(new IllegalArgumentException("error message"));

        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteShowPlaylistErrorUserNotLoggedInException() throws SpotifyException {
        when(spotifyMock.showPlaylist(playlistName, email)).thenThrow(new UserNotLoggedInException("error message"));

        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteShowPlaylistErrorPlaylistDoesntExistException() throws SpotifyException {
        when(spotifyMock.showPlaylist(playlistName, email))
                .thenThrow(new PlaylistDoesntExistException("error message"));

        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecuteShowPlaylistThrowsServerSideException() throws SpotifyException {
        when(spotifyMock.showPlaylist(playlistName, email)).thenThrow(new UncheckedIOException(new IOException()));

        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of(playlistName));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.showPlaylist method throws exception that is " +
                        "different from IllegalArgumentException, UserNotLoggedInException or" +
                        "PlaylistDoesntExistException");
    }

    @Test
    void testExecuteShowPlaylistEmptyPlaylist() throws SpotifyException {
        when(spotifyMock.showPlaylist(playlistName, email)).thenReturn(List.of(email));

        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Playlist '" + playlistName + "' is empty";

        assertEquals(CommandType.SHOW_PLAYLIST, response.getType(),
                "Checks if the returned command type is SHOW_PLAYLIST");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    @Test
    void testExecuteShowPlaylistNotEmptyPlaylist() throws SpotifyException {
        when(spotifyMock.showPlaylist(playlistName, email)).thenReturn(List.of(email, song1, song2));

        Command command = new Command(CommandType.SHOW_PLAYLIST, List.of(playlistName));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Playlist '" + playlistName + "' created by '" + email + "'" + System.lineSeparator() +
                "\t" + song1 + System.lineSeparator() +
                "\t" + song2;

        assertEquals(CommandType.SHOW_PLAYLIST, response.getType(),
                "Checks if the returned command type is SHOW_PLAYLIST");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    //-------------Play-------------

    @Test
    void testExecutePlayErrorInsufficientArguments() {
        Command command = new Command(CommandType.PLAY, List.of());
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                "play a song", 1, "song name");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(String.format(ERROR_MESSAGE_PATTERN, expectedMessage), response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecutePlayErrorIllegalArgumentException() throws SpotifyException {
        when(spotifyMock.streamSong(song1, email))
                .thenThrow(new IllegalArgumentException("error message"));

        Command command = new Command(CommandType.PLAY, List.of(song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecutePlayErrorUserNotLoggedInException() throws SpotifyException {
        when(spotifyMock.streamSong(song1, email))
                .thenThrow(new UserNotLoggedInException("error message"));

        Command command = new Command(CommandType.PLAY, List.of(song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }

    @Test
    void testExecutePlayErrorSongDoesntExistException() throws SpotifyException {
        when(spotifyMock.streamSong(song1, email))
                .thenThrow(new SongDoesntExistException("error message"));

        Command command = new Command(CommandType.PLAY, List.of(song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = String.format(ERROR_MESSAGE_PATTERN, "error message");

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");
        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }


    @Test
    void testExecutePlayThrowsServerSideException() throws SpotifyException {
        when(spotifyMock.streamSong(song1, email)).thenThrow(new UncheckedIOException(new IOException()));

        Command command = new Command(CommandType.PLAY, List.of(playlistName));

        assertThrows(ServerSideException.class, () -> commandExecutor.execute(command, email),
                "Throws ServerSideException when the spotify.showPlaylist method throws exception that is " +
                        "different from IllegalArgumentException, UserNotLoggedInException or" +
                        "SongDoesntExistException");
    }

    @Test
    void testExecutePlay() throws SpotifyException {
        StreamableSong streamableSongMock = mock(StreamableSong.class);
        when(spotifyMock.streamSong(song1, email)).thenReturn(streamableSongMock);

        Command command = new Command(CommandType.PLAY, List.of(song1));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Playing song \"" + song1 + "\"";

        assertEquals(CommandType.PLAY, response.getType(),
                "Checks if the returned command type is PLAY");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");

        assertEquals(streamableSongMock, response.getSong(),
                "Checks if the returned song is correct");
    }

    //-------------Stop-------------
    @Test
    void testExecuteStop() {
        Command command = new Command(CommandType.STOP, List.of(""));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Song stopped";

        assertEquals(CommandType.STOP, response.getType(),
                "Checks if the returned command type is STOP");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned message is correct");
    }

    //-------------Error-------------
    @Test
    void testExecuteErrorCommand() {
        Command command = new Command(CommandType.ERROR, List.of("error command"));
        ServerResponse response = commandExecutor.execute(command, email);

        String expectedMessage = "Not a valid command";

        assertEquals(CommandType.ERROR, response.getType(),
                "Checks if the returned command type is ERROR");

        assertEquals(expectedMessage, response.getMessage(),
                "Checks if the returned error message is correct");
    }
}
