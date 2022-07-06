package co.casterlabs.kaminari.core.targets;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import co.casterlabs.kaminari.core.Kaminari;
import lombok.Getter;

public class TwitchTarget implements Closeable {
    public static String INGEST_SERVER = "rtmp://dfw.contribute.live-video.net/app/";

    private Process aStreamProcess;
    private Process streamProcess;

    private @Getter OutputStream videoSink;
    private @Getter OutputStream audioSink;

    public TwitchTarget(
        Kaminari kaminari,
        String vPixelFormat, int vWidth, int vHeight,
        String aFormat, int aChannels, int aRate,
        String streamKey
    ) throws IOException {
        int aPort = this.getRandomEphemeralPort();

        this.streamProcess = new ProcessBuilder()
            .command(
            //@formatter:off
                "ffmpeg",
                "-hide_banner",
                "-v", "error",
                
                "-f", aFormat,
                "-acodec", "pcm_" + aFormat,
                "-ac", String.valueOf(aChannels),
                "-ar", String.valueOf(aRate),
                "-i", "tcp://127.0.0.1:" + aPort + "?listen",

                "-f", "rawvideo",
                "-vcodec", "rawvideo",
                "-pixel_format", vPixelFormat,
                "-video_size", String.format("%dx%d", vWidth, vHeight),
                "-i", "pipe:0",

                "-vcodec", "libx264",
                "-framerate", String.valueOf(kaminari.getFrameRate()),
                "-video_size", String.format("%dx%d", kaminari.getWidth(), kaminari.getHeight()),
                "-preset", "fast",
                "-vb", "4000k",
                "-pixel_format", "yuv420p",
                "-f", "flv", 
                "rtmp://dfw.contribute.live-video.net/app/" + streamKey
                //@formatter:on
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();
        this.videoSink = this.streamProcess.getOutputStream();

        this.aStreamProcess = new ProcessBuilder()
            .command(
            //@formatter:off
                "ffmpeg",
                "-hide_banner",
                "-v", "error",

                "-f", aFormat,
                "-acodec", "pcm_" + aFormat,
                "-ac", String.valueOf(aChannels),
                "-ar", String.valueOf(aRate),
                "-i", "pipe:0",

                "-f", aFormat,
//                "-acodec", "copy",
                "tcp://127.0.0.1:" + aPort
                //@formatter:on
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .start();
        this.audioSink = this.aStreamProcess.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        this.aStreamProcess.destroy();
        this.streamProcess.destroy();
    }

    private int getRandomEphemeralPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", 0), 1);
            return serverSocket.getLocalPort();
        }
    }

}
