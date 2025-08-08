import java.awt.Point;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Utils {
    public static byte[] getSHA256Hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static int calculateCRC8(byte[] data) {
        int crc = 0x00;
        for (byte b : data) {
            crc ^= b;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ 0x07;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFF;
            }
        }
        return crc;
    }

    public static List<Point> getPixelPositions(int crcValue, int length) {
        List<Point> points = new ArrayList<>();
        int step = crcValue + 1;
        int width = 256;
        for (int i = 0; i < length; i++) {
            int index = i * step;
            if (index >= 256 * 256) break;
            int x = index % width;
            int y = index / width;
            points.add(new Point(x, y));
        }
        return points;
    }
}
