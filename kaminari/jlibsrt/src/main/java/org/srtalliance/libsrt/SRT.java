package org.srtalliance.libsrt;

import com.sun.jna.Native;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

/*
 * All pointer variables start with $.
 */
public class SRT {
    public static final Platform PLATFORM = Platform.get();
    public static final String SRT_VERSION;

    @Deprecated
    public static final SRTNative NATIVE;

    private static final FastLogger logger = new FastLogger("SRT");

    static {
        // Setup.
        NATIVE = Native.load("srt", SRTNative.class);

        // Version parsing
        {
            // https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getversion
            // @formatter:off
            int v = NATIVE.srt_getversion();
            int patch = (v >> 0)  & 0xFF;
            int minor = (v >> 8)  & 0xFF;
            int major = (v >> 16) & 0xFF;
            // @formatter:off
            
            SRT_VERSION = String.format("v%d.%d.%d", major, minor, patch);
        }
    }

    private int $socket;

}
