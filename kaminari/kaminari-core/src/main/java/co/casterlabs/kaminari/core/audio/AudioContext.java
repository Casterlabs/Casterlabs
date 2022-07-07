package co.casterlabs.kaminari.core.audio;

import static co.casterlabs.kaminari.core.audio.AudioConstants.*;

import org.jetbrains.annotations.Nullable;

public abstract class AudioContext {
    public float volume = 1;

    public final @Nullable float[] readChunk() {
        float[] chunk = this.readChunk0();
        if (chunk == null) return null;

        // Apply volume scaling.
        {
            for (int sIdx = 0; sIdx < chunk.length; sIdx++) {
                float sample = chunk[sIdx];

                sample *= this.volume;
                sample = range(-1, 1, sample);

                chunk[sIdx] = sample;
            }
        }

        return chunk;
    }

    protected abstract @Nullable float[] readChunk0();

}
