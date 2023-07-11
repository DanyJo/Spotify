package uni.fmi.mjt.project.spotify.dto.song.format;

import javax.sound.sampled.AudioFormat;
import java.io.Serializable;

public class Format implements Serializable {
    private final String encoding;
    private final float sampleRate;
    private final int sampleBits;
    private final int channels;
    private final int frameSize;
    private final float frameRate;
    private final boolean isBigEndian;

    public Format(AudioFormat audioFormat) {
        this.encoding = audioFormat.getEncoding().toString();
        this.sampleRate = audioFormat.getSampleRate();
        this.sampleBits = audioFormat.getSampleSizeInBits();
        this.channels = audioFormat.getChannels();
        this.frameSize = audioFormat.getFrameSize();
        this.frameRate = audioFormat.getFrameRate();
        this.isBigEndian = audioFormat.isBigEndian();
    }

    public AudioFormat getAsAudioFormat() {
        return new AudioFormat(new AudioFormat.Encoding(encoding), this.sampleRate, this.sampleBits,
                this.channels, this.frameSize, this.frameRate, this.isBigEndian);
    }
}
