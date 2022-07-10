package org.srtalliance.libsrt;

import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class TestJavaServer {

    public static void main(String[] args) {
        FastLoggingFramework.setColorEnabled(false);
        FastLoggingFramework.setDefaultLevel(LogLevel.ALL);

        FastLogger.logStatic(SRT.SRT_VERSION);
    }

}
