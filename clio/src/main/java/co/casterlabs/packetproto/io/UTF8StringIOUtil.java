package co.casterlabs.packetproto.io;

import java.nio.charset.StandardCharsets;

import lombok.NonNull;

public class UTF8StringIOUtil {

    public static byte[] stringToBytes(@NonNull String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

}
