package co.casterlabs.kaminari.core.audio;

import java.io.Closeable;

import org.jetbrains.annotations.Nullable;

public abstract class AudioContext extends AudioConstants implements Closeable {
    public float volume = 1;

    public final @Nullable float[] read() {
        float[] chunk = this.read0();
        if (chunk == null) return null;

        for (int channel = 0; channel < AUDIO_CHANNELS; channel++) {
            float sample = chunk[channel] * this.volume;
            sample = range(-1, 1, sample);

            chunk[channel] = sample;
        }

        return chunk;
    }

    protected abstract @Nullable float[] read0();

}
