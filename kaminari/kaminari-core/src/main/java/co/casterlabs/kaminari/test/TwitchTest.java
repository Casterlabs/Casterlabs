package co.casterlabs.kaminari.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.audio.AudioConstants;
import co.casterlabs.kaminari.core.audio.PCMTransformer;
import co.casterlabs.kaminari.core.scene.Scene;
import co.casterlabs.kaminari.core.source.DebugTextSource;
import co.casterlabs.kaminari.core.source.ImageSource;
import co.casterlabs.kaminari.core.source.TextSource;
import co.casterlabs.kaminari.core.targets.TwitchTarget;
import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
public class TwitchTest extends AudioConstants {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        FastLoggingFramework.setColorEnabled(false);

        final File audioFile1 = new File(args[0]);

        // Setup Kaminari.
        Kaminari kaminari = new Kaminari("Test Instance");

        kaminari.setSize(1280, 720);
        kaminari.setFramerate(60);

        // Twitch.
        {
            TwitchTarget target = new TwitchTarget(
                kaminari,
                Kaminari.IMAGE_FORMAT, kaminari.getWidth(), kaminari.getHeight(),
                AudioConstants.AUDIO_FORMAT, AudioConstants.AUDIO_CHANNELS, AudioConstants.AUDIO_RATE,
                args[1] // Stream key.
            );

            kaminari.setTarget(target.getVideoSink());
            kaminari.setAudioTarget(target.getAudioSink());
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
