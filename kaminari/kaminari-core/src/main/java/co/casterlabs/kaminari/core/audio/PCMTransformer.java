package co.casterlabs.kaminari.core.audio;

import static co.casterlabs.kaminari.core.audio.AudioConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.SneakyThrows;

public class PCMTransformer extends StreamedAudioContext {
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

        this.startReading();
    }

    @SneakyThrows
    @Override
    protected int read(byte[] buf, int off, int len) {
        if (!this.isOpen()) {
            return 0;
        }

        try {
            int read = this.ffmpegOut.read(buf, off, len);

            return read;
        } catch (IOException e) {
            e.printStackTrace();
            this.close();
            return 0;
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
        super.close();

        if (this.process != null) {
            this.process.destroy();
            this.process = null;
            this.ffmpegOut = null;
            this.ffmpegIn = null;
        }
    }

}
