package co.casterlabs.kaminari.core.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioConstants {
    public static final int AUDIO_BYTES_PER_SAMPLE = Float.BYTES;
    public static final int AUDIO_CHANNELS = 2;
    public static final int AUDIO_RATE = 48000;

    public static final String AUDIO_FORMAT = "f32le";

    public static float makeSample(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb.getFloat(0);
    }

    public static byte[] destructSample(float sample) {
        ByteBuffer bb = ByteBuffer.allocate(Float.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(sample);

        return bb.array();
    }

}
