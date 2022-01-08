package co.casterlabs.caffeinated.bootstrap.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class LoopbackExemption {
    // Java's Exec feature runs as CMD, NetIsolation is only accessible from
    // PowerShell.
    private static final String checkExemption = "powershell -Command \"CheckNetIsolation LoopbackExempt -s\"";
    private static final String loopbackCommand = "powershell -Command \"CheckNetIsolation LoopbackExempt -a -n='Microsoft.Win32WebViewHost_cw5n1h2txyewy'\"";
    private static final String startScript = "powershell -Command \"Start-Process 'cmd' -ArgumentList /c,%temp%/loopback_exempt_edge.bat -Verb runAs\"";

    public static void checkLoopback() {
        try {
            Process process = Runtime.getRuntime().exec(checkExemption);
            InputStream in = process.getInputStream();

            String result = IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);

            if (!result.contains("Win32WebViewHost_cw5n1h2txyewy")) {
                Files.write(Paths.get(System.getenv("temp") + "/loopback_exempt_edge.bat"), loopbackCommand.getBytes());
                process = Runtime.getRuntime().exec(startScript);

                FastLogger.logStatic("Added loopback exemption to Windows!");
            }
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Couldn't auto add loopback exemption to Windows!");
            FastLogger.logException(e);
        }
    }

}
