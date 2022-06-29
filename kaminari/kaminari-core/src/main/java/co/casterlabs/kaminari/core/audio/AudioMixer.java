package co.casterlabs.kaminari.core.audio;

import java.util.ArrayList;
import java.util.List;

public class AudioMixer extends AudioConstants implements AudioContext {
    public final List<AudioContext> contexts = new ArrayList<>();

    @Override
    public float[] read() {
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

    private static float range(float min, float max, float value) {
        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }

}
