package co.casterlabs.kaminari.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;

import co.casterlabs.kaminari.core.audio.AudioConstants;
import co.casterlabs.kaminari.core.audio.AudioMixer;
import co.casterlabs.kaminari.core.audio.PCMTransformer;
import co.casterlabs.rakurai.io.IOUtil;

public class AudioTest extends AudioConstants {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        final File audioFile1 = new File(args[0]);
        final File audioFile2 = new File(args[1]);

        AudioMixer mixer = new AudioMixer();

        // Playback thread.
        Thread playbackThread = new Thread(() -> {
            try {
                // @formatter:off
                OutputStream target = new ProcessBuilder()
                    .command(new String[] {
                        "ffplay",
                        "-hide_banner",
//                        "-v", "warning",

                        // Input
                        "-i", "pipe:0",
                        "-f", AUDIO_FORMAT,
                        "-acodec", "pcm_" + AUDIO_FORMAT,
                        "-ac", String.valueOf(AUDIO_CHANNELS),
                        "-ar", String.valueOf(AUDIO_RATE)
                })
//                    .inheritIO()
                    .redirectInput(Redirect.PIPE)
                    .start()
                    .getOutputStream();
                // @formatter:on

                while (true) {
                    float[] chunk = mixer.read();

                    for (float sample : chunk) {
                        byte[] bytes = AudioConstants.destructSample(sample);

                        target.write(bytes);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        playbackThread.setDaemon(true);
        playbackThread.start();

        // Audio file #1
//        new Thread(() -> {
//            PCMTransformer transformer = new PCMTransformer();
//            mixer.contexts.add(transformer);
//
//            try (FileInputStream fin = new FileInputStream(audioFile1)) {
//                transformer.start();
//                IOUtil.writeInputStreamToOutputStream(fin, transformer.getInput());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();

        // Audio file #2
        new Thread(() -> {
            PCMTransformer transformer = new PCMTransformer();
            transformer.volume = .05f;
            mixer.contexts.add(transformer);

            try (FileInputStream fin = new FileInputStream(audioFile2)) {
                transformer.start();
                IOUtil.writeInputStreamToOutputStream(fin, transformer.getInput());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
