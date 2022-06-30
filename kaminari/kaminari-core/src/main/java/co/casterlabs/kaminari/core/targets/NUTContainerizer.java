package co.casterlabs.kaminari.core.targets;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import co.casterlabs.rakurai.io.IOUtil;
import lombok.Getter;

public class NUTContainerizer implements Closeable {
    private Process aStreamProcess;
    private Process containerizerProcess;

    private @Getter OutputStream videoSink;
    private @Getter OutputStream audioSink;

    public NUTContainerizer(
        OutputStream target,
        String vPixelFormat, int vWidth, int vHeight,
        String targetVCodec,
        String aFormat, int aChannels, int aRate,
        String targetACodec
    ) throws IOException {
        int aPort = this.getRandomEphemeralPort();

        // We pass video directly to the containerizer to lighten the load on the
        // system.
        this.containerizerProcess = new ProcessBuilder()
            .command(
            //@formatter:off
                "ffmpeg",
                "-hide_banner",
                "-v", "warning",
                
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

                "-vcodec", targetVCodec,
                "-acodec", targetACodec,
                "-f", "nut",
                "pipe:1"
                //@formatter:on
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();

        this.videoSink = this.containerizerProcess.getOutputStream();

        this.aStreamProcess = new ProcessBuilder()
            .command(
            //@formatter:off
                "ffmpeg",
                "-hide_banner",
                "-v", "warning",

                "-f", aFormat,
                "-acodec", "pcm_" + aFormat,
                "-ac", String.valueOf(aChannels),
                "-ar", String.valueOf(aRate),
                "-i", "pipe:0",

                "-f", aFormat,
                "-acodec", "pcm_" + aFormat,
                "-ac", String.valueOf(aChannels),
                "-ar", String.valueOf(aRate),
                "tcp://127.0.0.1:" + aPort
                //@formatter:on
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .start();
        this.audioSink = this.aStreamProcess.getOutputStream();

        Thread thread = new Thread(() -> {
            try {
                IOUtil.writeInputStreamToOutputStream(
                    this.containerizerProcess.getInputStream(),
                    target
                );
            } catch (IOException e) {
                try {
                    close();
                } catch (IOException ignored) {}
            }
        });
        thread.setDaemon(false);
        thread.setName("NUT Containerizer Write Thread");
        thread.start();
    }

    @Override
    public void close() throws IOException {
        this.aStreamProcess.destroy();
        this.containerizerProcess.destroy();
    }

    private int getRandomEphemeralPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", 0), 1);
            return serverSocket.getLocalPort();
        }
    }

}
