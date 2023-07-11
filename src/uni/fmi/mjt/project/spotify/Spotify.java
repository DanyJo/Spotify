package uni.fmi.mjt.project.spotify;

import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;
import uni.fmi.mjt.project.spotify.exception.SpotifyException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Spotify {
    void register(String email, String password) throws SpotifyException;

    void login(String email, String password) throws SpotifyException;

    void disconnect(String email);

    Set<String> search(Collection<String> keywords, String email) throws SpotifyException;

    List<String> top(int number, String email) throws SpotifyException;

    void createPlaylist(String name, String email) throws SpotifyException;

    void addSongToPlaylist(String playlistName, String songName, String email) throws SpotifyException;

    List<String> showPlaylist(String playlistName, String email) throws SpotifyException;

    StreamableSong streamSong(String songName, String email) throws SpotifyException;
}
