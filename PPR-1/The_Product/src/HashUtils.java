import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    // Generate SHA-256 hash of a password
    public static byte[] getSHA256Hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Compute CRC-8 from a byte array
    public static int getCRC8(byte[] data) {
        int crc = 0x00;
        int polynomial = 0x07;

        for (byte b : data) {
            crc ^= b & 0xFF;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFF;
            }
        }
        return crc;
    }
}
