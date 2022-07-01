package co.casterlabs.kaminari.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.time.Instant;
import java.util.UUID;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.audio.AudioConstants;
import co.casterlabs.kaminari.core.audio.PCMTransformer;
import co.casterlabs.kaminari.core.scene.Scene;
import co.casterlabs.kaminari.core.source.DebugTextSource;
import co.casterlabs.kaminari.core.source.ImageSource;
import co.casterlabs.kaminari.core.source.TextSource;
import co.casterlabs.kaminari.core.targets.NUTContainerizer;
import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
public class MuxerTest extends AudioConstants {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        FastLoggingFramework.setColorEnabled(false);

        final File audioFile1 = new File(args[0]);

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
//                "-f", "nut",
//                "-vcodec", VCODEC,
//                "-acodec", ACODEC,
                "pipe:0"
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .start();

        OutputStream target = ffProcess.getOutputStream();

        // Muxer.
        {
            @SuppressWarnings("resource")
            NUTContainerizer containerizer = new NUTContainerizer(
                target,
                Kaminari.IMAGE_FORMAT, kaminari.getWidth(), kaminari.getHeight(),
                AudioConstants.AUDIO_FORMAT, AudioConstants.AUDIO_CHANNELS, AudioConstants.AUDIO_RATE,
                // @formatter:off
                "-acodec", "aac",
                "-vcodec", "h264"
                // @formatter:on
            );

            kaminari.setTarget(containerizer.getVideoSink());
            kaminari.setAudioTarget(containerizer.getAudioSink());
        }

        Scene testScene = new Scene(kaminari, "test", "Test");

        // Test Audio.
        new Thread(() -> {
            PCMTransformer transformer = new PCMTransformer();
            transformer.volume = .5f;
            testScene.mixer.contexts.add(transformer);

            try (FileInputStream fin = new FileInputStream(audioFile1)) {
                transformer.start();
                IOUtil.writeInputStreamToOutputStream(fin, transformer.getInput());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

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
        }

        kaminari.setCurrentSceneIndex(0);
        kaminari.scenes.add(testScene);

        kaminari.start();
    }

}
