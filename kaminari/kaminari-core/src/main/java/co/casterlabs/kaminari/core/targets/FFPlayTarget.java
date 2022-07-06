package co.casterlabs.kaminari.core.targets;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;

import lombok.Getter;

public class FFPlayTarget implements Closeable {
    private Process aProcess;
    private Process vProcess;

    private @Getter OutputStream videoSink;
    private @Getter OutputStream audioSink;

    public FFPlayTarget(
        String vPixelFormat, int vWidth, int vHeight,
        String aFormat, int aChannels, int aRate
    ) throws IOException {
        this.vProcess = new ProcessBuilder()
            .command(
            //@formatter:off
                "ffplay",
                "-hide_banner",
                "-v", "error",
                
                "-f", "rawvideo",
                "-vcodec", "rawvideo",
                "-pixel_format", vPixelFormat,
                "-video_size", String.format("%dx%d", vWidth, vHeight),
                "-i", "pipe:0"
                //@formatter:on
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();
        this.videoSink = this.vProcess.getOutputStream();

        this.aProcess = new ProcessBuilder()
            .command(
            //@formatter:off
                "ffplay",
                "-hide_banner",
                "-v", "error",
                
                "-f", aFormat,
                "-acodec", "pcm_" + aFormat,
                "-ac", String.valueOf(aChannels),
                "-ar", String.valueOf(aRate),
                "-i", "pipe:0"
                //@formatter:on
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();
        this.audioSink = this.aProcess.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        this.aProcess.destroy();
        this.vProcess.destroy();
    }

}
