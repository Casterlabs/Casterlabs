package co.casterlabs.kaminari.core.source;

import java.awt.Font;
import java.awt.Graphics;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.Looper;
import co.casterlabs.kaminari.core.audio.AudioConstants;
import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;

@Deprecated
public class DebugTextSource extends TextSource {
    private String frameBandwidth;
    private String audioBandwidth;

    public DebugTextSource(@NonNull Scene scene) {
        super(scene, "DEBUG-SOURCE", "Debug Source");

        // Styling.
        this.setTextColor("white");
        this.setFont(Font.MONOSPACED, 16);

        // Span the whole area.
        this.setPosition(0, 0);
        this.setSize(1, 1);

        this.audioBandwidth = String.format(
            "%.2fmbps",
            ((AudioConstants.AUDIO_BYTES_PER_SAMPLE * AudioConstants.AUDIO_CHANNELS * 8d) * AudioConstants.AUDIO_RATE)
                / 1000 / 1000
        );
    }

    @Override
    public void onPack() {
        Kaminari kaminari = this.scene.getKaminari();

        int width = kaminari.getWidth();
        int height = kaminari.getHeight();
        int framerate = kaminari.getFrameRate();

        double size = (width * height) * Kaminari.IMAGE_FORMAT_BITS * framerate;

        this.frameBandwidth = String.format(
            "%.2fmbps",
            size / 1000 / 1000
        );
    }

    @Override
    public void render(Graphics g) {
        Kaminari kaminari = this.scene.getKaminari();
        Looper videoLooper = kaminari.getVideoLooper();

        this.setLines(
        // @formatter:off
            String.format("Rendered frames:   %d/%d",         videoLooper.getFramesRendered(), videoLooper.getFramesTargeted()),
            String.format("Video:             %dx%d @ %dfps", kaminari.getWidth(), kaminari.getHeight(), kaminari.getFrameRate()),
            String.format("Video bandwidth:   %s",            this.frameBandwidth),
            String.format("Frame time:        %dms",          videoLooper.getFrameTime()),
            String.format("Frame interval:    %.1fms",        videoLooper.getFrameInterval() / 1e+6),
            String.format("Frame format:      %s",            Kaminari.IMAGE_FORMAT),
            String.format("Audio:             %dch @ %.1fk",  AudioConstants.AUDIO_CHANNELS, AudioConstants.AUDIO_RATE / 1000d),
            String.format("Audio bandwidth:   %s",            this.audioBandwidth),
            String.format("Sample format:     %s",            AudioConstants.AUDIO_FORMAT),
            String.format("Sampling interval: %dms",          AudioConstants.AUDIO_BUFFER_TIME)
        // @formatter:on
        );

        super.render(g);
    }

}
