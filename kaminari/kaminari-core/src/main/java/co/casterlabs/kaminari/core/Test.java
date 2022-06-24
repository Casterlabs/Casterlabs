package co.casterlabs.kaminari.core;

import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.UUID;

import co.casterlabs.kaminari.core.source.ColorSource;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Test {

    @SneakyThrows
    public static void main(String[] args) {
        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        FastLoggingFramework.setColorEnabled(false);

        // Setup Kaminari.
        Kaminari kaminari = new Kaminari("Test Instance");

        kaminari.setSize(1280, 720);
        kaminari.setFramerate(30);

        // Setup ffplay.
        Process ffplayProcess = new ProcessBuilder()
            .command(
                "ffplay",
                "-hide_banner",
                "-v", "warning",
                "-f", "rawvideo",
                "-vcodec", "rawvideo",
                "-pixel_format", Kaminari.IMAGE_FORMAT,
//                "-framerate", String.valueOf(kaminari.getFrameRate()),
                "-video_size", String.format("%dx%d", kaminari.getWidth(), kaminari.getHeight()),
                "pipe:0"
            )
            .inheritIO()
            .redirectInput(Redirect.PIPE)
            .start();

        OutputStream ffplayStream = ffplayProcess.getOutputStream();

        kaminari.setTarget(ffplayStream);

        // Test source.
        ColorSource testSource = new ColorSource(kaminari, UUID.randomUUID().toString(), "Test");

        testSource.setPosition(.5f, .5f); // Percent
        testSource.setSize(.25f, .25f); // Percent

        kaminari.add(testSource);

        Thread colorCycler = new Thread(() -> {
            while (true) {
                try {
                    testSource.setColor("red");
                    Thread.sleep(1000);
                    testSource.setColor("green");
                    Thread.sleep(1000);
                    testSource.setColor("blue");
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        });

        colorCycler.setDaemon(true);
        colorCycler.start();

        kaminari.start();
    }

}
