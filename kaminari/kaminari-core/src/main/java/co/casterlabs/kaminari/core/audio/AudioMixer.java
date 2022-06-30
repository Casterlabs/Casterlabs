package co.casterlabs.kaminari.core.audio;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class AudioMixer extends AudioContext {
    public final List<AudioContext> contexts = new ArrayList<>();

    @Override
    protected @Nullable float[] read0() {
        float[] result = new float[AUDIO_CHANNELS];
        int mixCount = 0;

        for (AudioContext context : this.contexts) {
            float[] chunk = context.read();
            if (chunk == null) continue;

            for (int channel = 0; channel < AUDIO_CHANNELS; channel++) {
                float sample = chunk[channel];

                result[channel] += sample / 2;
            }

            mixCount++;
        }

        if (mixCount > 0) {
            // Duck all the audio levels to prevent clipping.
            for (int channel = 0; channel < AUDIO_CHANNELS; channel++) {
                result[channel] /= mixCount;
                result[channel] = range(-1, 1, result[channel]);
            }
        }

        return result;
    }

    @Override
    public void close() {
        // NOOP
    }

}
