package uni.fmi.mjt.project.spotify;

import org.junit.jupiter.api.Test;
import uni.fmi.mjt.project.spotify.exception.SpotifyException;
import uni.fmi.mjt.project.spotify.exception.account.AccountAlreadyExistsException;
import uni.fmi.mjt.project.spotify.exception.account.AccountDoesntExistException;
import uni.fmi.mjt.project.spotify.exception.account.IncorrectLoginCredentialsException;
import uni.fmi.mjt.project.spotify.exception.login.UserAlreadyLoggedInException;
import uni.fmi.mjt.project.spotify.exception.login.UserNotLoggedInException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistAlreadyContainsSongException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistAlreadyExistsException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistDoesntExistException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpotifyTest {
    private static final String SONGS_TEST_DIRECTORY = "TestDirectory" + File.separator + "TestSongs";
    private static final String PLAYLISTS_TEST_DIRECTORY = "TestDirectory" + File.separator + "TestPlaylists";
    private Spotify spotify;

    //------------Register------------

    @Test
    void testRegisterNullBlankOrEmptyEmail() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(IllegalArgumentException.class, () -> spotify.register(null, "asd"),
                "Throws IllegalArgumentException when invoked with null email");

        assertThrows(IllegalArgumentException.class, () -> spotify.register("", "asd"),
                "Throws IllegalArgumentException when invoked with empty email");

        assertThrows(IllegalArgumentException.class, () -> spotify.register("       ", "asd"),
                "Throws IllegalArgumentException when invoked with blank email");
    }

    @Test
    void testRegisterNullBlankOrEmptyPassword() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(IllegalArgumentException.class, () -> spotify.register("asd@abv.bg", null),
                "Throws IllegalArgumentException when invoked with null password");

        assertThrows(IllegalArgumentException.class, () -> spotify.register("asd@abv.bg", ""),
                "Throws IllegalArgumentException when invoked with empty password");

        assertThrows(IllegalArgumentException.class, () -> spotify.register("asd@abv.bg", "    "),
                "Throws IllegalArgumentException when invoked with blank password");
    }

    @Test
    void testRegisterExistingAccountSamePassword() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(AccountAlreadyExistsException.class, () -> spotify.register("asd@abv.bg", "123"),
                "Throws AccountAlreadyExistsException trying to register an existing account");
    }

    @Test
    void testRegisterExistingAccountDifferentPassword() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(AccountAlreadyExistsException.class, () -> spotify.register("asd@abv.bg", "321"),
                "Throws AccountAlreadyExistsException trying to register an existing account " +
                        "but with different password");
    }

    @Test
    void testRegisterAddsAccountToFile() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        writer.write("asd@abv.bg 123" + System.lineSeparator());
        spotify = new DefaultSpotify(reader, writer);

        spotify.register("other@abv.bg", "321");

        String expected = "asd@abv.bg 123" + System.lineSeparator() +
                "other@abv.bg 321" + System.lineSeparator();

        assertEquals(expected, writer.toString(),
                "Correctly writes the newly created account to the file");
    }

    @Test
    void testRegisterMarksUserAsLoggedIn() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        writer.write("asd@abv.bg 123" + System.lineSeparator());
        spotify = new DefaultSpotify(reader, writer);

        spotify.register("other@abv.bg", "321");

        Set<String> expected = Set.of("other@abv.bg");
        Set<String> actual = ((DefaultSpotify) spotify).getLoggedInAccounts();

        assertTrue(expected.containsAll(actual) && actual.containsAll(expected),
                "Correctly add the newly created account to the logged in accounts");
    }

    //------------Register------------

    @Test
    void testLoginNullBlankOrEmptyEmail() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(IllegalArgumentException.class, () -> spotify.login(null, "asd"),
                "Throws IllegalArgumentException when invoked with null email");

        assertThrows(IllegalArgumentException.class, () -> spotify.login("", "asd"),
                "Throws IllegalArgumentException when invoked with empty email");

        assertThrows(IllegalArgumentException.class, () -> spotify.login("       ", "asd"),
                "Throws IllegalArgumentException when invoked with blank email");
    }

    @Test
    void testLoginNullBlankOrEmptyPassword() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(IllegalArgumentException.class, () -> spotify.login("asd@abv.bg", null),
                "Throws IllegalArgumentException when invoked with null password");

        assertThrows(IllegalArgumentException.class, () -> spotify.login("asd@abv.bg", ""),
                "Throws IllegalArgumentException when invoked with empty password");

        assertThrows(IllegalArgumentException.class, () -> spotify.login("asd@abv.bg", "    "),
                "Throws IllegalArgumentException when invoked with blank password");
    }

    @Test
    void testLoginWrongPassword() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(IncorrectLoginCredentialsException.class, () -> spotify.login("asd@abv.bg", "321"),
                "Throws IncorrectLoginCredentialsException trying to login with an incorrect password");
    }

    @Test
    void testLoginNonExistentAccount() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(AccountDoesntExistException.class,
                () -> spotify.login("other@abv.bg", "321"),
                "Throws AccountDoesntExistException trying to login an nonexistent account");
    }

    @Test
    void testLoginWithAlreadyLoggedInAccount() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        spotify.login("asd@abv.bg", "123");

        assertThrows(UserAlreadyLoggedInException.class, () -> spotify.login("asd@abv.bg", "123"),
                "Throws UserAlreadyLoggedInException trying to login to an already logged in account");
    }

    @Test
    void testLoginAccountAddedToLoggedInUsers() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator() +
                "other@abv.bg 321" + System.lineSeparator());

        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        spotify.login("asd@abv.bg", "123");
        spotify.login("other@abv.bg", "321");

        Set<String> expected = Set.of("asd@abv.bg", "other@abv.bg");
        Set<String> actual = ((DefaultSpotify) spotify).getLoggedInAccounts();

        assertTrue(expected.containsAll(actual) && actual.containsAll(expected),
                "Correctly add the newly logged in account to the previously logged in accounts");
    }

    //------------Disconnect------------

    @Test
    void testDisconnectLoggedInAccount() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator() +
                "other@abv.bg 321" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        spotify.login("asd@abv.bg", "123");
        spotify.login("other@abv.bg", "321");

        spotify.disconnect("other@abv.bg");

        Set<String> expected = Set.of("asd@abv.bg");
        Set<String> actual = ((DefaultSpotify) spotify).getLoggedInAccounts();

        assertTrue(expected.containsAll(actual) && actual.containsAll(expected),
                "Correctly removes an account from the logged in accounts");
    }

    @Test
    void testDisconnectNotLoggedInAccount() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator() +
                "other@abv.bg 321" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        spotify.login("asd@abv.bg", "123");

        spotify.disconnect("other@abv.bg");

        Set<String> expected = Set.of("asd@abv.bg");
        Set<String> actual = ((DefaultSpotify) spotify).getLoggedInAccounts();

        assertTrue(expected.containsAll(actual) && actual.containsAll(expected),
                "Doesn't throw exception when trying to disconnect invalid user (not logged in)");
    }

    //------------Stream song------------

    @Test
    void testStreamSongNullBlankOrEmptySongName() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(IllegalArgumentException.class, () -> spotify.streamSong(null, "asd@abv.bg"),
                "Throws IllegalArgumentException when invoked with null email");

        assertThrows(IllegalArgumentException.class, () -> spotify.streamSong("", "asd@abv.bg"),
                "Throws IllegalArgumentException when invoked with empty email");

        assertThrows(IllegalArgumentException.class, () -> spotify.streamSong("       ", "asd@abv.bg"),
                "Throws IllegalArgumentException when invoked with blank email");
    }

    @Test
    void testStreamSongAccountNotLoggedIn() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(UserNotLoggedInException.class, () -> spotify.streamSong("song", "asd@abv.bg"),
                "Throws UserNotLoggedInException when invoked by a user that isn't logged in");
    }

    //------------Search------------

    @Test
    void testSearchNullOrEmptyKeywords() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(IllegalArgumentException.class, () -> spotify.search(null, "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with collection that is null");

        assertThrows(IllegalArgumentException.class, () -> spotify.search(List.of(), "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with empty collection");
    }

    @Test
    void testSearchAccountNotLoggedIn() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(UserNotLoggedInException.class,
                () -> spotify.search(List.of("test", "song"), "asd@abv.bg"),
                "Throws UserNotLoggedInException when invoked by a user that isn't logged in");
    }

    @Test
    void testSearchNoSongs() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");
        assertTrue(spotify.search(List.of("keyword"), "asd@abv.bg").isEmpty(),
                "Correctly returns an empty collection when there are no songs containing the keywords");

    }

    @Test
    void testSearchNoSongsContainingKeywords() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        Set<Path> tempSongs = createTempFiles(SONGS_TEST_DIRECTORY, "test.txt");

        try {
            spotify.login("asd@abv.bg", "123");
            assertTrue(spotify.search(List.of("keyword"), "asd@abv.bg").isEmpty(),
                    "Correctly returns an empty collection when there are no songs containing the keywords");
        } finally {
            deleteTempFiles(tempSongs);
        }
    }

    @Test
    void testSearchContainingKeywords() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        Set<Path> tempSongs = createTempFiles(SONGS_TEST_DIRECTORY,
                "test.txt", "SonG.txt", "other.txt", "TEST songs - the 3rd.txt");

        try {
            spotify.login("asd@abv.bg", "123");

            Set<String> expected = Set.of("test", "SonG", "TEST songs - the 3rd");
            Set<String> actual = spotify.search(List.of("song", "Test", "something"), "asd@abv.bg");

            assertTrue(expected.containsAll(actual) && actual.containsAll(expected),
                    "Correctly returns the names of the songs containing the keywords");
        } finally {
            deleteTempFiles(tempSongs);
        }
    }

    private Set<Path> createTempFiles(String directory, String... fileNames) throws IOException {
        Set<Path> tempFiles = new HashSet<>();

        for (String fileName : fileNames) {
            tempFiles.add(Files.createFile(Path.of(directory, fileName)));
        }

        return tempFiles;
    }

    private void deleteTempFiles(Set<Path> tempFiles) throws IOException {
        for (Path tempFile : tempFiles) {
            Files.delete(tempFile);
        }
    }

    //------------Search------------
    @Test
    void testTopNegativeNumber() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(IllegalArgumentException.class,
                () -> spotify.top(-1, "asd@abv.bg"),
                "Throws IllegalArgumentException when invoked with a negative number");
    }

    @Test
    void testTopAccountNotLoggedIn() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(UserNotLoggedInException.class,
                () -> spotify.top(100, "asd@abv.bg"),
                "Throws UserNotLoggedInException when invoked by a user that isn't logged in");
    }

    @Test
    void testTopNoSongsStreamed() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");

        assertTrue(spotify.top(100, "asd@abv.bg").isEmpty(),
                "Correctly returns empty list when no songs were streamed");
    }

    @Test
    void testTopWithStreamedSongs() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        spotify.login("asd@abv.bg", "123");

        spotify.streamSong("Alessia Cara - Here", "asd@abv.bg");
        spotify.streamSong("Alessia Cara - Here", "asd@abv.bg");
        spotify.streamSong("JID, Kenny Mason - Dance Now", "asd@abv.bg");

        List<String> expected = List.of("Alessia Cara - Here", "JID, Kenny Mason - Dance Now");

        assertIterableEquals(expected, spotify.top(100, "asd@abv.bg"),
                "Correctly returns a list containing the most streamed songs, " +
                        "sorted by times played in descending order");
    }

    //------------Create playlist------------
    @Test
    void testCreatePlaylistNullOrEmptyName() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(IllegalArgumentException.class, () -> spotify.createPlaylist(null, "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with name that is null");

        assertThrows(IllegalArgumentException.class, () -> spotify.createPlaylist("", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with empty name");

        assertThrows(IllegalArgumentException.class, () -> spotify.createPlaylist("     ", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with blank name");
    }

    @Test
    void testCreatePlaylistAccountNotLoggedIn() {
        StringReader reader = new StringReader("");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer);

        assertThrows(UserNotLoggedInException.class,
                () -> spotify.createPlaylist("playlist", "asd@abv.bg"),
                "Throws UserNotLoggedInException when invoked by a user that isn't logged in");
    }

    @Test
    void testCreatePlaylistAlreadyExists() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");


        Set<Path> tempPlaylists = createTempFiles(PLAYLISTS_TEST_DIRECTORY,
                "playlist.txt");

        try {
            assertThrows(PlaylistAlreadyExistsException.class,
                    () -> spotify.createPlaylist("playlist", "asd@abv.bg"),
                    "Throws PlaylistAlreadyExistsException when trying to create a playlist with the same name " +
                            "as an already existing one");
        } finally {
            deleteTempFiles(tempPlaylists);
        }
    }

    @Test
    void testCreatePlaylist() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");

        spotify.createPlaylist("playlist", "asd@abv.bg");

        assertTrue(isPlaylistCreated(Path.of(PLAYLISTS_TEST_DIRECTORY), "playlist"),
                "Correctly creates new playlist");

        Files.delete(Path.of(PLAYLISTS_TEST_DIRECTORY, "playlist.txt"));
    }

    @Test
    void testCreatePlaylistContent() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123");
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");

        spotify.createPlaylist("playlist", "asd@abv.bg");
        List<String> expected = List.of("asd@abv.bg");
        List<String> actual = getPlaylistContent(PLAYLISTS_TEST_DIRECTORY + File.separator + "playlist.txt");

        assertIterableEquals(expected, actual,
                "Correctly writes the email of the user that created the playlist");

        Files.delete(Path.of(PLAYLISTS_TEST_DIRECTORY, "playlist.txt"));
    }

    private boolean isPlaylistCreated(Path playlistPath, String playlistName) {
        try (var playlists = Files.newDirectoryStream(playlistPath)) {
            for (var playlist : playlists) {
                String currPlaylist = playlist.getFileName().toString().strip();

                if (currPlaylist.toLowerCase().contains(playlistName.strip().toLowerCase())) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    //------------Add song to playlist------------
    @Test
    void testAddSongToPlaylistNullOrEmptyPlaylistName() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(IllegalArgumentException.class,
                () -> spotify.addSongToPlaylist(null, "song", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with playlist name that is null");

        assertThrows(IllegalArgumentException.class,
                () -> spotify.addSongToPlaylist("", "song", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with playlist name that is empty");

        assertThrows(IllegalArgumentException.class,
                () -> spotify.addSongToPlaylist("       ", "song", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with playlist name that is blank");
    }

    @Test
    void testAddSongToPlaylistNullOrEmptySongName() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(IllegalArgumentException.class,
                () -> spotify.addSongToPlaylist("playlist", null, "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with song name that is null");

        assertThrows(IllegalArgumentException.class,
                () -> spotify.addSongToPlaylist("playlist", "", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with song name that is empty");

        assertThrows(IllegalArgumentException.class,
                () -> spotify.addSongToPlaylist("playlist", "       ", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with song name that is blank");
    }

    @Test
    void testAddSongToPlaylistAccountNotLoggedIn() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(UserNotLoggedInException.class,
                () -> spotify.addSongToPlaylist("playlist", "song", "asd@abv.bg"),
                "Throws UserNotLoggedInException when trying to invoke by a user that isn't logged in");
    }

    @Test
    void testAddSongToPlaylistPlaylistDoesntExist() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");

        assertThrows(PlaylistDoesntExistException.class,
                () -> spotify.addSongToPlaylist("playlist", "song", "asd@abv.bg"),
                "Throws PlaylistDoesntExistException when trying to add song to a non-existent playlist");
    }

    @Test
    void testAddSongToPlaylistSongAlreadyExistsInPlaylist() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");
        spotify.createPlaylist("playlist", "asd@abv.bg");
        spotify.addSongToPlaylist("playlist", "song", "asd@abv.bg");

        assertThrows(PlaylistAlreadyContainsSongException.class,
                () -> spotify.addSongToPlaylist("playlist", "song", "asd@abv.bg"),
                "Throws PlaylistAlreadyContainsSongException when trying to add a duplicate song to playlist");

        Files.delete(Path.of(PLAYLISTS_TEST_DIRECTORY, "playlist.txt"));
    }

    @Test
    void testAddSongToPlaylistSong() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");
        spotify.createPlaylist("playlist", "asd@abv.bg");
        spotify.addSongToPlaylist("playlist", "song", "asd@abv.bg");

        List<String> expected = List.of("asd@abv.bg", "song");
        List<String> actual = getPlaylistContent(PLAYLISTS_TEST_DIRECTORY + File.separator + "playlist.txt");

        assertIterableEquals(expected, actual, "Correctly adds song to the playlist");

        Files.delete(Path.of(PLAYLISTS_TEST_DIRECTORY, "playlist.txt"));
    }

    //------------Add song to playlist------------
    @Test
    void testShowPlaylistNullOrEmptyPlaylistName() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(IllegalArgumentException.class,
                () -> spotify.showPlaylist(null, "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with playlist name that is null");

        assertThrows(IllegalArgumentException.class,
                () -> spotify.showPlaylist("", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with playlist name that is empty");

        assertThrows(IllegalArgumentException.class,
                () -> spotify.showPlaylist("       ", "asd@abv.bg"),
                "Throws IllegalArgumentException when trying to invoke with playlist name that is blank");
    }

    @Test
    void testShowPlaylistAccountNotLoggedIn() {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        assertThrows(UserNotLoggedInException.class,
                () -> spotify.showPlaylist("playlist", "asd@abv.bg"),
                "Throws UserNotLoggedInException when trying to invoke by a user that isn't logged in");
    }

    @Test
    void testShowPlaylistPlaylistDoesntExist() throws SpotifyException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        spotify.login("asd@abv.bg", "123");

        assertThrows(PlaylistDoesntExistException.class,
                () -> spotify.showPlaylist("playlist", "asd@abv.bg"),
                "Throws PlaylistDoesntExistException when trying to show a non-existent playlist");
    }

    @Test
    void testShowPlaylist() throws SpotifyException, IOException {
        StringReader reader = new StringReader("asd@abv.bg 123" + System.lineSeparator());
        StringWriter writer = new StringWriter();
        spotify = new DefaultSpotify(reader, writer, SONGS_TEST_DIRECTORY, PLAYLISTS_TEST_DIRECTORY);

        String playlistName = "playlist";

        spotify.login("asd@abv.bg", "123");
        spotify.createPlaylist(playlistName, "asd@abv.bg");
        spotify.addSongToPlaylist(playlistName, "song1", "asd@abv.bg");
        spotify.addSongToPlaylist(playlistName, "song2", "asd@abv.bg");

        List<String> expected = List.of("asd@abv.bg", "song1", "song2");
        List<String> actual = spotify.showPlaylist(playlistName, "asd@abv.bg");

        assertIterableEquals(expected, actual, "Correctly returns the contents of a playlist");

        Files.delete(Path.of(PLAYLISTS_TEST_DIRECTORY, "playlist.txt"));
    }

    private List<String> getPlaylistContent(String playlistPath) {
        try (var reader = new BufferedReader(new FileReader(playlistPath))) {
            return reader.lines()
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
