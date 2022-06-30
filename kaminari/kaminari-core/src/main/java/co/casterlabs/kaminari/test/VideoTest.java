package co.casterlabs.kaminari.test;

import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.time.Instant;
import java.util.UUID;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.scene.Scene;
import co.casterlabs.kaminari.core.source.DebugTextSource;
import co.casterlabs.kaminari.core.source.ImageSource;
import co.casterlabs.kaminari.core.source.TextSource;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
public class VideoTest {

    @SneakyThrows
    public static void main(String[] args) {
        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        FastLoggingFramework.setColorEnabled(false);

        // Setup Kaminari.
        Kaminari kaminari = new Kaminari("Test Instance");

        kaminari.setSize(1280, 720);
        kaminari.setFramerate(30);

        // Setup ffplay.
        Process ffProcess = new ProcessBuilder()
            .command(
                "ffplay",
                "-hide_banner",
                "-v", "warning",

                // Input
                "-f", "rawvideo",
                "-vcodec", "rawvideo",
                "-pixel_format", Kaminari.IMAGE_FORMAT,
//                "-framerate", String.valueOf(kaminari.getFrameRate()),
                "-video_size", String.format("%dx%d", kaminari.getWidth(), kaminari.getHeight()),
                "pipe:0"

//                "ffmpeg",
//                "-hide_banner",
//                "-v", "warning",
//
//                // Input
//                "-f", "rawvideo",
//                "-vcodec", "rawvideo",
//                "-pixel_format", Kaminari.IMAGE_FORMAT,
//                "-framerate", String.valueOf(kaminari.getFrameRate()),
//                "-video_size", String.format("%dx%d", kaminari.getWidth(), kaminari.getHeight()),
//                "-i", "pipe:0",
//
//                // Output
//                "-vcodec", "libx264",
//                "-framerate", String.valueOf(kaminari.getFrameRate()),
//                "-video_size", String.format("%dx%d", kaminari.getWidth(), kaminari.getHeight()),
//                "-preset", "fast",
//                "-vb", "3000k",
//                "-pixel_format", "yuv420p",
//                "-f", "flv",
//                "rtmp://dfw.contribute.live-video.net/app/" + args[0] // STREAMKEY
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .start();

        OutputStream target = ffProcess.getOutputStream();
        kaminari.setTarget(target);

        Scene testScene = new Scene(kaminari, "test", "Test");

        // Setup the test scene
        {
            // Test source.
            ImageSource testSource = new ImageSource(testScene, UUID.randomUUID().toString(), "Test");

            testSource.setPosition(.5f, .5f); // Percent
            testSource.setSize(.25f, .25f); // Percent
            testSource.setImageFromDataUri("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mP8z8AARIQB46hC+ioEAGX8E/cKr6qsAAAAAElFTkSuQmCC");

            testScene.add(testSource);
        }
        {
            TextSource timeSource = new TextSource(testScene, UUID.randomUUID().toString(), "Time");

            timeSource.setPosition(.25f, .25f); // Percent
            timeSource.setSize(.25f, .5f); // Percent

            testScene.add(timeSource);

            Thread timekeeper = new Thread(() -> {
                while (true) {
                    try {
                        timeSource.setText(Instant.now().toString());
                        Thread.sleep(50);
                    } catch (Exception ignored) {}
                }
            });

            timekeeper.setDaemon(true);
            timekeeper.start();

            testScene.add(new DebugTextSource(testScene));

            kaminari.setCurrentSceneIndex(0);
            kaminari.scenes.add(testScene);
        }

        kaminari.start();
    }

}
