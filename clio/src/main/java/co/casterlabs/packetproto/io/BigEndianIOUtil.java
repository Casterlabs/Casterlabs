package co.casterlabs.packetproto.io;

public class BigEndianIOUtil {

    /* -------- */
    /* Long     */
    /* -------- */

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];

        for (int idx = Long.BYTES - 1; idx >= 0; idx--) {
            result[idx] = (byte) (l & 0xFF);
            l >>= Byte.SIZE;
        }

        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;

        for (int idx = 0; idx < Long.BYTES; idx++) {
            result <<= Byte.SIZE;
            result |= (b[idx] & 0xFF);
        }

        return result;
    }

    /* -------- */
    /* Int      */
    /* -------- */

    public static byte[] intToBytes(int i) {
        byte[] result = new byte[Integer.BYTES];

        for (int idx = Integer.BYTES - 1; idx >= 0; idx--) {
            result[idx] = (byte) (i & 0xFF);
            i >>= Byte.SIZE;
        }

        return result;
    }

    public static int bytesToInt(byte[] b) {
        int result = 0;

        for (int idx = 0; idx < Integer.BYTES; idx++) {
            result <<= Byte.SIZE;
            result |= (b[idx] & 0xFF);
        }

        return result;
    }

    /* -------- */
    /* Short    */
    /* -------- */

    public static byte[] shortToBytes(short s) {
        byte[] result = new byte[Short.BYTES];

        for (int idx = Short.BYTES - 1; idx >= 0; idx--) {
            result[idx] = (byte) (s & 0xFF);
            s >>= Byte.SIZE;
        }

        return result;
    }

    public static short bytesToShort(byte[] b) {
        short result = 0;

        for (int idx = 0; idx < Short.BYTES; idx++) {
            result <<= Byte.SIZE;
            result |= (b[idx] & 0xFF);
        }

        return result;
    }

    /* -------- */
    /* Double   */
    /* -------- */

    public static byte[] doubleToBytes(double d) {
        long bits = Double.doubleToLongBits(d);

        return longToBytes(bits);
    }

    public static double bytesToDouble(byte[] b) {
        long bits = bytesToLong(b);

        return Double.longBitsToDouble(bits);
    }

    /* -------- */
    /* Float    */
    /* -------- */

    public static byte[] floatToBytes(float f) {
        int bits = Float.floatToIntBits(f);

        return intToBytes(bits);
    }

    public static float bytesToFloat(byte[] b) {
        int bits = bytesToInt(b);

        return Float.intBitsToFloat(bits);
    }

}
