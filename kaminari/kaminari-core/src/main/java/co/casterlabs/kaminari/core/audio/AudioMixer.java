package co.casterlabs.kaminari.core.audio;

import static co.casterlabs.kaminari.core.audio.AudioConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class AudioMixer extends AudioContext {
    public final List<AudioContext> contexts = new ArrayList<>();

    @Override
    protected @Nullable float[] readChunk0() {
        float[] result = new float[AUDIO_BUFFER_SIZE];
        int mixCount = 0;

        for (AudioContext context : this.contexts) {
            float[] chunk = context.readChunk();
            if (chunk == null) continue;

            for (int sIdx = 0; sIdx < AUDIO_BUFFER_SIZE; sIdx++) {
                float sample = chunk[sIdx];

                result[sIdx] += sample / 2;
            }

            mixCount++;
        }

        if (mixCount == 0) {
            return null;
        }

        // Duck all the audio levels to prevent clipping.
        for (int sIdx = 0; sIdx < AUDIO_BUFFER_SIZE; sIdx++) {
            result[sIdx] /= mixCount;
            result[sIdx] = range(-1, 1, result[sIdx]);
        }

        return result;
    }

}
