public class CRCUtil {

    private static final int POLYNOMIAL = 0x1021; // CRC-16-CCITT

    public static int computeCRCDecimal(byte[] data) {
        int crc = 0xFFFF;

        for (byte b : data) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= POLYNOMIAL;
                }
            }
        }

        crc &= 0xFFFF; // Ensure 16-bit result
        return crc % 256; // 🔁 Fit CRC decimal into [0, 255] range
    }
}
