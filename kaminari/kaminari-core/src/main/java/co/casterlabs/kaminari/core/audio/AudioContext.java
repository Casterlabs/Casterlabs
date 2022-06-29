package co.casterlabs.kaminari.core.audio;

import java.io.Closeable;

import org.jetbrains.annotations.Nullable;

public interface AudioContext extends Closeable {

    public @Nullable float[] read();

}
