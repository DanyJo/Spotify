package uni.fmi.mjt.project.spotify.command;

import uni.fmi.mjt.project.spotify.Spotify;
import uni.fmi.mjt.project.spotify.dto.response.ServerResponse;
import uni.fmi.mjt.project.spotify.exception.ServerSideException;
import uni.fmi.mjt.project.spotify.exception.login.UserAlreadyLoggedInException;
import uni.fmi.mjt.project.spotify.exception.login.UserNotLoggedInException;
import uni.fmi.mjt.project.spotify.exception.account.AccountAlreadyExistsException;
import uni.fmi.mjt.project.spotify.exception.account.AccountDoesntExistException;
import uni.fmi.mjt.project.spotify.exception.account.IncorrectLoginCredentialsException;
import uni.fmi.mjt.project.spotify.exception.account.InvalidEmailFormatException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistAlreadyContainsSongException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistAlreadyExistsException;
import uni.fmi.mjt.project.spotify.exception.playlist.PlaylistDoesntExistException;
import uni.fmi.mjt.project.spotify.exception.song.SongDoesntExistException;
import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandExecutor {
    private static final String ERROR_MESSAGE_PATTERN = "There was an error!" + System.lineSeparator() + "%s";
    private static final String ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS = "Insufficient arguments when trying to %s, " +
            "expected %d argument\\s: %s";
    private final Spotify spotify;

    public CommandExecutor(Spotify spotify) {
        this.spotify = spotify;
    }

    //Refactor! Use different class for every command (check command pattern)
    public ServerResponse execute(Command command, String email) {
        return switch (command.type()) {
            case REGISTER -> register(command.arguments());
            case LOGIN -> login(command.arguments());
            case DISCONNECT -> disconnect(email);
            case SEARCH -> search(command.arguments(), email);
            case TOP -> top(command.arguments(), email);
            case CREATE_PLAYLIST -> createPlaylist(command.arguments(), email);
            case ADD_SONG_TO -> addSongToPlaylist(command.arguments(), email);
            case SHOW_PLAYLIST -> showPlaylist(command.arguments(), email);
            case PLAY -> play(command.arguments(), email);
            case STOP -> stop();
            default -> ServerResponse.builder(CommandType.ERROR, "Not a valid command").build();
        };
    }

    private ServerResponse register(List<String> arguments) {
        if (arguments.size() < 2) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "register", 2, "email and password");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        String email = arguments.get(0).strip();
        String password = arguments.get(1).strip();

        CommandType type;
        String message;

        try {
            this.spotify.register(email, password);
            type = CommandType.REGISTER;
            message = "Account was registered successfully";

            return ServerResponse.builder(type, message).setEmail(email).build();
        } catch (IllegalArgumentException | InvalidEmailFormatException | AccountAlreadyExistsException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());

            return ServerResponse.builder(type, message).build();
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to register an account", e);
        }
    }

    private ServerResponse login(List<String> arguments) {
        if (arguments.size() < 2) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "login", 2, "email and password");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        String email = arguments.get(0).strip();
        String password = arguments.get(1).strip();

        CommandType type;
        String message;

        try {
            this.spotify.login(email, password);

            type = CommandType.LOGIN;
            message = "Welcome " + email;

            return ServerResponse.builder(type, message).setEmail(email).build();
        } catch (IllegalArgumentException | UserAlreadyLoggedInException | InvalidEmailFormatException |
                 AccountDoesntExistException | IncorrectLoginCredentialsException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());

            return ServerResponse.builder(type, message).build();
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to login to an account", e);
        }
    }

    private ServerResponse disconnect(String email) {
        this.spotify.disconnect(email);

        return ServerResponse.builder(CommandType.DISCONNECT, "Disconnected").build();
    }

    private ServerResponse search(List<String> keywords, String email) {
        if (keywords.isEmpty()) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "search for songs", 1, "keywords");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        CommandType type;
        String message;

        try {
            Set<String> songs = this.spotify.search(keywords, email);

            type = CommandType.SEARCH;

            if (songs.isEmpty()) {
                message = "No songs were found containing the keywords";
            } else {
                message = "These are the songs that were found using the keywords:" +
                        System.lineSeparator() + '\t' + getCollectionAsString(songs);
            }
        } catch (IllegalArgumentException | UserNotLoggedInException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to search songs for keywords", e);
        }

        return ServerResponse.builder(type, message).build();
    }

    private ServerResponse top(List<String> arguments, String email) {
        if (arguments.isEmpty()) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "get top songs", 1, "count of top songs");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        int number = Integer.parseInt(arguments.get(0).strip());

        CommandType type;
        String message;

        try {
            List<String> songs = this.spotify.top(number, email);

            type = CommandType.TOP;

            if (songs.isEmpty()) {
                message = "No songs have been played";
            } else {
                message = "Most played songs:" +
                        System.lineSeparator() + '\t' + getCollectionAsString(songs);
            }
        } catch (IllegalArgumentException | UserNotLoggedInException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to get most listened songs", e);
        }

        return ServerResponse.builder(type, message).build();
    }

    private ServerResponse createPlaylist(List<String> arguments, String email) {
        if (arguments.isEmpty()) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "create a playlist", 1, "playlist name");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        String playlistName = arguments.get(0).strip();

        CommandType type;
        String message;

        try {
            this.spotify.createPlaylist(playlistName, email);

            type = CommandType.CREATE_PLAYLIST;
            message = "Playlist '" + playlistName + "' was created";
        } catch (IllegalArgumentException | UserNotLoggedInException | PlaylistAlreadyExistsException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to create a playlist", e);
        }

        return ServerResponse.builder(type, message).build();
    }

    private ServerResponse addSongToPlaylist(List<String> arguments, String email) {
        if (arguments.size() < 2) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "add song to playlist", 2, "playlist name and song name");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        String playlistName = arguments.get(0).strip();
        String songName = arguments.get(1).strip();

        CommandType type;
        String message;

        try {
            this.spotify.addSongToPlaylist(playlistName, songName, email);

            type = CommandType.ADD_SONG_TO;
            message = "'" + songName + "' was added successfully to '" + playlistName + "'";
        } catch (IllegalArgumentException | UserNotLoggedInException | PlaylistAlreadyContainsSongException |
                 PlaylistDoesntExistException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to add song to a playlist", e);
        }

        return ServerResponse.builder(type, message).build();
    }

    private ServerResponse showPlaylist(List<String> arguments, String email) {
        if (arguments.isEmpty()) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "show playlist", 1, "playlist name");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        String playlistName = arguments.get(0).strip();

        CommandType type;
        String message;

        try {
            List<String> playlistContents = this.spotify.showPlaylist(playlistName, email);

            type = CommandType.SHOW_PLAYLIST;

            if (playlistContents.size() == 1) {
                message = "Playlist '" + playlistName + "' is empty";
            } else {
                String playlistCreator = playlistContents.get(0).strip();

                message = "Playlist '" + playlistName + "' created by '" + playlistCreator + "'" +
                        System.lineSeparator() + "\t" +
                        getCollectionAsString(playlistContents.subList(1, playlistContents.size()));
            }
        } catch (IllegalArgumentException | UserNotLoggedInException | PlaylistDoesntExistException e) {
            type = CommandType.ERROR;
            message = String.format(ERROR_MESSAGE_PATTERN, e.getMessage());
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to retrieve a playlist", e);
        }

        return ServerResponse.builder(type, message).build();
    }

    private ServerResponse play(List<String> arguments, String email) {
        if (arguments.isEmpty()) {
            String errorMessage = String.format(ERROR_MESSAGE_INSUFFICIENT_ARGUMENTS,
                    "play a song", 1, "song name");

            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, errorMessage)).build();
        }

        String songName = arguments.get(0).strip();

        try {
            StreamableSong song = spotify.streamSong(songName, email);

            String message = "Playing song \"" + songName + "\"";

            return ServerResponse.builder(CommandType.PLAY, message).setSong(song).build();
        } catch (IllegalArgumentException | UserNotLoggedInException | SongDoesntExistException e) {
            return ServerResponse.builder(CommandType.ERROR,
                    String.format(ERROR_MESSAGE_PATTERN, e.getMessage())).build();
        } catch (Exception e) {
            throw new ServerSideException("A problem occurred while trying to retrieve a playlist", e);
        }
    }

    private ServerResponse stop() {
        return ServerResponse.builder(CommandType.STOP, "Song stopped").build();
    }

    private String getCollectionAsString(Collection<String> songs) {
        return songs.stream()
                .map(String::strip)
                .collect(Collectors.joining(System.lineSeparator() + '\t'));
    }

}
