package uni.fmi.mjt.project.spotify.dto.song;

import org.junit.jupiter.api.Test;
import uni.fmi.mjt.project.spotify.dto.song.StreamableSong;
import uni.fmi.mjt.project.spotify.exception.song.SongDoesntExistException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamableSongTest {
    private static final String SONGS_TEST_DIRECTORY = "Music";
    private StreamableSong song;

    @Test
    void testStreamableSongNonExistentSong() {
        assertThrows(SongDoesntExistException.class,
                () -> new StreamableSong("not a song", SONGS_TEST_DIRECTORY),
                "Throws SongDoesntExistException when trying to get the format of not existing song");
    }
}
