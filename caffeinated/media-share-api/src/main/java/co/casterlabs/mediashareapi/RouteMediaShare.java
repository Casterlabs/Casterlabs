package co.casterlabs.mediashareapi;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpResponse.ResponseContent;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.NonNull;
import lombok.SneakyThrows;

public class RouteMediaShare implements HttpProvider {
    public static final boolean IS_WINDOWS = System.getProperty("os.name", "").contains("Windows");
    public static final String YTDLP_EXEC = IS_WINDOWS ? "yt-dlp" : "./yt-dlp";
    public static final String FFMPEG_EXEC = IS_WINDOWS ? "ffmpeg" : "./ffmpeg";

    @SuppressWarnings("unused")
    @SneakyThrows
    @HttpEndpoint(uri = "/public/v2/caffeinated/mediashare")
    public HttpResponse onSharedMedia(SoraHttpSession session) {
        String urlToGrab = session.getQueryParameters().get("url");
        boolean outputInMp4 = session.getQueryParameters().containsKey("mp4");

//        if (outputInMp4) {
        return this.outputInMp4(urlToGrab);
//        }
    }

    private HttpResponse outputInMp4(@NonNull String urlToGrab) throws IOException {
        Process ytdlpProc = new ProcessBuilder()
            .command(
                YTDLP_EXEC,
                "--ffmpeg-location", FFMPEG_EXEC,
                "-f", "bv*[height<=720]+ba/b[height<=720] / wv*+ba/w", // MAX 720p, or lowest available.
                urlToGrab,
                "-o", "-"
            )
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .redirectError(Redirect.PIPE)
            .start();

        Process ffmpegProc = new ProcessBuilder()
            .command(
                FFMPEG_EXEC,
                "-i", "pipe:0",
                "-c:a", "libopus",
                "-b:a", "128k",
                "-ac", "2",
                "-c:v", "copy",
                "-movflags", "frag_keyframe+empty_moov",
                "-f", "mp4",
                "pipe:1"
            )
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .redirectError(Redirect.PIPE)
            .start();

        return new HttpResponse(
            new ResponseContent() {
                @Override
                public void write(OutputStream out) throws IOException {
                    // Read and discard the logs.
                    AsyncTask.create(() -> {
                        try {
                            IOUtil.writeInputStreamToOutputStream(ytdlpProc.getErrorStream(), new OutputStream() {
                                @Override
                                public void write(byte b[], int off, int len) {}

                                @Override
                                public void write(int b) throws IOException {}
                            });
                        } catch (IOException ignored) {}
                    });
                    AsyncTask.create(() -> {
                        try {
                            IOUtil.writeInputStreamToOutputStream(ffmpegProc.getErrorStream(), new OutputStream() {
                                @Override
                                public void write(byte b[], int off, int len) {}

                                @Override
                                public void write(int b) throws IOException {}
                            });
                        } catch (IOException ignored) {}
                    });

                    // Write the ytdlp stream to FFMpeg.
                    AsyncTask.create(() -> {
                        try {
                            IOUtil.writeInputStreamToOutputStream(ytdlpProc.getInputStream(), ffmpegProc.getOutputStream());
                        } catch (IOException ignored) {
                            this.close();
                        }
                        // do NOT call close() when done, ffmpeg still needs to process the data.
                    });

                    // Write the FFMpeg result to the http channel.
                    try {
                        IOUtil.writeInputStreamToOutputStream(ffmpegProc.getInputStream(), out);
                    } catch (IOException ignored) {}
                }

                @Override
                public void close() {
                    ytdlpProc.destroy();
                    ffmpegProc.destroy();
                }

                @Override
                public long getLength() {
                    return Integer.MAX_VALUE;
                }
            },
            StandardHttpStatus.OK
        )
            .setMimeType("video/mp4");
    }

}
