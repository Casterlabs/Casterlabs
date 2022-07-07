package co.casterlabs.kaminari.core.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class AudioConstants {
    // @formatter:off
    public static final ByteOrder  AUDIO_ENDIANNESS        = ByteOrder.LITTLE_ENDIAN;
    public static final int        AUDIO_BYTES_PER_SAMPLE  = Float.BYTES;
    public static final int        AUDIO_CHANNELS          = 2;
    public static final int        AUDIO_RATE              = 44100;
    public static final int        AUDIO_BUFFER_TIME       = 10; // Milliseconds
    public static final int        AUDIO_BUFFER_SIZE       = (int) (AUDIO_RATE * (AUDIO_BUFFER_TIME / 1000d)); 
    public static final String     AUDIO_FORMAT            = AUDIO_ENDIANNESS == ByteOrder.LITTLE_ENDIAN ? "f32le" : "f32be";
    // @formatter:on

    static {
        double floatBufferSize = AUDIO_RATE * (AUDIO_BUFFER_TIME / 1000d);
        assert AUDIO_BUFFER_SIZE == floatBufferSize : "AUDIO_BUFFER_TIME must contain an even number of samples.";
    }

    public static float makeSample(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(AUDIO_ENDIANNESS);

        return bb.getFloat(0);
    }

    public static byte[] destructSample(float sample) {
        ByteBuffer bb = ByteBuffer.allocate(AUDIO_BYTES_PER_SAMPLE);
        bb.order(AUDIO_ENDIANNESS);
        bb.putFloat(sample);

        return bb.array();
    }

    public static float[] makeSamples(byte[] bytes) {
        assert bytes.length == AUDIO_BUFFER_SIZE * AUDIO_BYTES_PER_SAMPLE;

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(AUDIO_ENDIANNESS);

        FloatBuffer fb = bb.asFloatBuffer();

        float[] chunk = new float[AUDIO_BUFFER_SIZE];
        fb.get(chunk);

        return chunk;
    }

    public static byte[] destructSamples(float[] chunk) {
        assert chunk.length == AUDIO_BUFFER_SIZE;

        ByteBuffer bb = ByteBuffer.allocate(AUDIO_BUFFER_SIZE * AUDIO_BYTES_PER_SAMPLE);
        bb.order(AUDIO_ENDIANNESS);

        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(chunk);

        return bb.array();
    }

    public static float range(float min, float max, float value) {
        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }

}
