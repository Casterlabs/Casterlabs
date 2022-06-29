package co.casterlabs.kaminari.core.audio;

public class AudioVolumeController extends AudioConstants implements AudioContext {
    public AudioContext context;
    public float volume = 1;

    @Override
    public float[] read() {
        if (this.context == null) {
            return null;
        }

        float[] chunk = this.context.read();
        if (chunk == null) return null;

        for (int channel = 0; channel < AUDIO_CHANNELS; channel++) {
            float sample = chunk[channel] * this.volume;
            sample = range(-1, 1, sample);

            chunk[channel] = sample;
        }

        return chunk;
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
