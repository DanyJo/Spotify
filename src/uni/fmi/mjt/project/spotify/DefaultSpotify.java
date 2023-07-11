package uni.fmi.mjt.project.spotify;

import uni.fmi.mjt.project.spotify.account.Account;
import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;
import uni.fmi.mjt.project.spotify.exception.ServerSideException;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSpotify implements Spotify {
    private static final String FILE_EXTENSION = ".txt";

    private static final String SONGS_DEFAULT_DIRECTORY = "Music";
    private static final String PLAYLISTS_DEFAULT_DIRECTORY = "Playlists";
    private static final String PLAYLISTS_PATH_PATTERN = "%s" + File.separator + "%s" + FILE_EXTENSION;

    private static final String EMAIL_FIELD_NAME = "email";
    private static final String PASSWORD_FIELD_NAME = "password";
    private static final String SONG_FIELD_NAME = "song name";
    private static final String PLAYLIST_FIELD_NAME = "playlist name";

    private final String songsDirectory;
    private final String playlistsDirectory;

    private final BufferedWriter accountWriter;

    private final Set<Account> accountsDataset;
    private final Set<String> loggedInAccounts;
    private final Map<String, Integer> songsPlayed;

    public DefaultSpotify(Reader accountIn, Writer accountOut) {
        accountWriter = new BufferedWriter(accountOut);
        songsPlayed = new HashMap<>();

        songsDirectory = SONGS_DEFAULT_DIRECTORY;
        playlistsDirectory = PLAYLISTS_DEFAULT_DIRECTORY;

        accountsDataset = loadAccountsToDataset(accountIn);
        loggedInAccounts = new HashSet<>();
    }

    public DefaultSpotify(Reader accountIn, Writer accountOut, String songsDirectory, String playlistsDirectory) {
        accountWriter = new BufferedWriter(accountOut);
        songsPlayed = new HashMap<>();

        this.songsDirectory = songsDirectory;
        this.playlistsDirectory = playlistsDirectory;

        accountsDataset = loadAccountsToDataset(accountIn);
        loggedInAccounts = new HashSet<>();
    }

    @Override
    public void register(String email, String password) throws SpotifyException {
        checkIsNullEmptyOrBlank(email, EMAIL_FIELD_NAME);
        checkIsNullEmptyOrBlank(password, PASSWORD_FIELD_NAME);

        Account account = new Account(email, password);

        checkAccountExists(account);
        createAccount(email, password);

        accountsDataset.add(account);

        loggedInAccounts.add(email);
    }

    @Override
    public void login(String email, String password) throws SpotifyException {
        checkIsNullEmptyOrBlank(email, EMAIL_FIELD_NAME);
        checkIsNullEmptyOrBlank(password, PASSWORD_FIELD_NAME);

        checkIsLoggedIn(email);

        Account account = new Account(email, password);

        checkAccountDoesntExist(account);
        checkLoginCredentials(account);

        loggedInAccounts.add(email);
    }

    @Override
    public void disconnect(String email) {
        Iterator<String> iterator = loggedInAccounts.iterator();

        while (iterator.hasNext()) {
            String currAccount = iterator.next();

            if (currAccount.equalsIgnoreCase(email)) {
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public Set<String> search(Collection<String> keywords, String email) throws SpotifyException {
        checkIsNullOrEmpty(keywords);

        checkIsNotLoggedIn(email);

        return getSongsContainingKeywords(keywords);
    }

    @Override
    public List<String> top(int number, String email) throws SpotifyException {
        checkNegative(number);

        checkIsNotLoggedIn(email);

        return songsPlayed.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, Integer> entry) -> entry.getValue()).reversed())
                .limit(number)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public void createPlaylist(String name, String email) throws SpotifyException {
        checkIsNullEmptyOrBlank(name, PLAYLIST_FIELD_NAME);
        checkIsNotLoggedIn(email);
        checkPlaylistAlreadyExists(name);

        String playListPath = String.format(PLAYLISTS_PATH_PATTERN, playlistsDirectory, name);

        try (var writer = new BufferedWriter(new FileWriter(playListPath))) {
            writer.write(email + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while creating the playlist. Please try again.", e);
        }
    }

    @Override
    public void addSongToPlaylist(String playlistName, String songName, String email) throws SpotifyException {
        checkIsNullEmptyOrBlank(playlistName, PLAYLIST_FIELD_NAME);
        checkIsNullEmptyOrBlank(songName, SONG_FIELD_NAME);

        checkIsNotLoggedIn(email);

        String playlistPath = String.format(PLAYLISTS_PATH_PATTERN, playlistsDirectory, playlistName);

        try (var reader = new BufferedReader(new FileReader(playlistPath));
             var writer = new BufferedWriter(new FileWriter(playlistPath, true))) {
            checkPlaylistAlreadyContainsSong(reader, songName);

            writer.write(songName + System.lineSeparator());
            writer.flush();
        } catch (FileNotFoundException e) {
            throw new PlaylistDoesntExistException("No playlist with the name '" + playlistName + "' was found", e);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "A problem occurred while searching the playlist dataset. Please try again.", e);
        }
    }

    @Override
    public List<String> showPlaylist(String playlistName, String email) throws SpotifyException {
        checkIsNullEmptyOrBlank(playlistName, PLAYLIST_FIELD_NAME);
        checkIsNotLoggedIn(email);

        String playlistPath = String.format(PLAYLISTS_PATH_PATTERN, playlistsDirectory, playlistName);

        try (var reader = new BufferedReader(new FileReader(playlistPath))) {
            return reader.lines().toList();
        } catch (FileNotFoundException e) {
            throw new PlaylistDoesntExistException("No playlist with the name '" + playlistName + "' was found", e);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "A problem occurred while searching the playlist dataset. Please try again.", e);
        }
    }

    @Override
    public StreamableSong streamSong(String songName, String email) throws SpotifyException {

        checkIsNullEmptyOrBlank(songName, SONG_FIELD_NAME);
        checkIsNotLoggedIn(email);

        StreamableSong song = new StreamableSong(songName, songsDirectory);

        songsPlayed.putIfAbsent(songName, 0);
        songsPlayed.put(songName, songsPlayed.get(songName) + 1);

        return song;
    }

    public Set<String> getLoggedInAccounts() {
        return loggedInAccounts;
    }

    //-----------------Helper methods-----------------

    private Set<Account> loadAccountsToDataset(Reader accountsIn) {
        try (var reader = new BufferedReader(accountsIn)) {
            return reader.lines()
                    .map(Account::create)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new ServerSideException("There was a problem accessing the accounts", e);
        }
    }

    private Set<String> getSongsContainingKeywords(Collection<String> keywords) {
        try (Stream<Path> songsStream = Files.walk(Path.of(songsDirectory))) {
            return songsStream
                    .skip(1)
                    .map(this::getSongName)
                    .filter(song -> doesSongContainKeyword(song.toLowerCase(), keywords))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while accessing the song dataset.", e);
        }
    }

    private String getSongName(Path songPath) {
        String songName = songPath.getFileName().toString().strip();

        return songName.substring(0, songName.length() - StreamableSong.EXTENSION.length());
    }

    private boolean doesSongContainKeyword(String songName, Collection<String> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.strip().toLowerCase())
                .anyMatch(songName::contains);
    }

    private boolean doesPlaylistExists(String name) {
        try (Stream<Path> playlistsStream = Files.walk(Path.of(playlistsDirectory))) {
            return playlistsStream
                    .skip(1)
                    .map(playlist -> playlist.getFileName().toString().strip())
                    .anyMatch(playlist -> playlist.equalsIgnoreCase(name + FILE_EXTENSION));
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while accessing the playlist dataset.", e);
        }
    }

    private void createAccount(String email, String password) {
        try {
            final String separator = " ";

            String registration = email + separator + password + System.lineSeparator();
            accountWriter.write(registration);
            accountWriter.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while accessing the account dataset.", e);
        }
    }

    //-----------------Exception handling-----------------

    private void checkIsLoggedIn(String email) throws UserAlreadyLoggedInException {
        if (loggedInAccounts.contains(email)) {
            throw new UserAlreadyLoggedInException(
                    "User " + email + " is already logged. Try disconnecting first.");
        }
    }

    private void checkIsNotLoggedIn(String email) throws UserNotLoggedInException {
        if (!loggedInAccounts.contains(email)) {
            throw new UserNotLoggedInException("You must login/register inorder do anything.");
        }
    }

    private void checkPlaylistAlreadyExists(String name) throws PlaylistAlreadyExistsException {
        if (doesPlaylistExists(name)) {
            throw new PlaylistAlreadyExistsException("There is already an existing playlist with the name " + name);
        }
    }

    private void checkIsNullEmptyOrBlank(String string, String fieldName) {
        if (string == null || string.isEmpty() || string.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null, empty or blank.");
        }
    }

    private void checkIsNullOrEmpty(Collection<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("When searching it is required to have at least one keyword.");
        }
    }

    private void checkNegative(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("When trying to get the top songs, a positive number must be provided.");
        }
    }

    private void checkAccountExists(Account account) throws AccountAlreadyExistsException {
        if (accountsDataset.contains(account)) {
            throw new AccountAlreadyExistsException("Email is already in use.");
        }
    }

    private void checkAccountDoesntExist(Account account) throws AccountDoesntExistException {
        if (!accountsDataset.contains(account)) {
            throw new AccountDoesntExistException("Account with email " + account.email() + " doesn't exist. " +
                    "A registration is required.");
        }
    }

    private void checkLoginCredentials(Account account)
            throws IncorrectLoginCredentialsException {
        for (Account currAccount : accountsDataset) {
            if (currAccount.equals(account)) {
                if (!currAccount.password().equalsIgnoreCase(account.password())) {
                    throw new IncorrectLoginCredentialsException("Incorrect password.");
                }

                break;
            }
        }
    }

    private void checkPlaylistAlreadyContainsSong(BufferedReader reader, String songName)
            throws PlaylistAlreadyContainsSongException {
        boolean isSongAlreadyInPlaylist = reader.lines()
                .anyMatch(x -> x.equalsIgnoreCase(songName));

        if (isSongAlreadyInPlaylist) {
            throw new PlaylistAlreadyContainsSongException("'" + songName + "' was already added to the playlist.");
        }
    }
}
