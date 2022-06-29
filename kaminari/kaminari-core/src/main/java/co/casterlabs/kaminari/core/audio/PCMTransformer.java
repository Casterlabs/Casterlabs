package co.casterlabs.kaminari.core.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jetbrains.annotations.Nullable;

import lombok.SneakyThrows;

public class PCMTransformer extends AudioContext {
    // @formatter:off
    private static final String[] FFMPEG_CONVERSION = {
          "ffmpeg",
          "-hide_banner",
          "-v", "warning",
          
          "-i", "pipe:0",
          "-f", AUDIO_FORMAT,
          "-acodec", "pcm_" + AUDIO_FORMAT,
          "-ac", String.valueOf(AUDIO_CHANNELS),
          "-ar", String.valueOf(AUDIO_RATE),
          "pipe:1"
    };
    // @formatter:on

    private Process process;
    private InputStream ffmpegOut;
    private OutputStream ffmpegIn;

    public void start() throws IOException {
        this.process = Runtime.getRuntime().exec(FFMPEG_CONVERSION);
        this.ffmpegOut = this.process.getInputStream();
        this.ffmpegIn = this.process.getOutputStream();
    }

    @SneakyThrows
    @Override
    protected @Nullable float[] read0() {
        if (!this.isOpen()) {
            return null;
        }

        try {
            float[] result = new float[AUDIO_CHANNELS];

            for (int channel = 0; channel < AUDIO_CHANNELS; channel++) {
                byte[] sample = this.ffmpegOut.readNBytes(AUDIO_BYTES_PER_SAMPLE);

                result[channel] = makeSample(sample);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            this.close();
            return null;
        }
    }

    public boolean isOpen() {
        return (this.process != null) && this.process.isAlive();
    }

    public OutputStream getInput() {
        return this.ffmpegIn;
    }

    @Override
    public void close() throws IOException {
        if (this.process != null) {
            this.process.destroy();
            this.process = null;
            this.ffmpegOut = null;
            this.ffmpegIn = null;
        }
    }

}
