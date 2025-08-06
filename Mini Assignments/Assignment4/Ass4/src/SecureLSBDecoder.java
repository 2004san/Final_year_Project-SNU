import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.*;

public class SecureLSBDecoder {
    public static String decode(BufferedImage image, int messageLength, String password) {
        int totalBits = messageLength * 8;
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        List<Integer> pixelOrder = getShuffledPixelIndices(totalPixels, password);
        byte[] msgBytes = new byte[messageLength];

        int bitIndex = 0;
        int currentByte = 0;

        for (int i = 0; i < totalBits; i++) {
            int pixelPos = pixelOrder.get(i);
            int x = pixelPos % width;
            int y = pixelPos / width;

            int pixel = image.getRGB(x, y);
            int red = (pixel >> 16) & 0xFF;
            int lsb = red & 1;

            currentByte = (currentByte << 1) | lsb;
            bitIndex++;

            if (bitIndex == 8) {
                msgBytes[i / 8] = (byte) currentByte;
                bitIndex = 0;
                currentByte = 0;
            }
        }

        return new String(msgBytes);
    }

    private static List<Integer> getShuffledPixelIndices(int totalPixels, String password) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < totalPixels; i++)
            indices.add(i);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            long seed = ByteBuffer.wrap(hash).getLong(); // use first 8 bytes of hash as seed
            Collections.shuffle(indices, new Random(seed));
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password");
        }

        return indices;
    }
}
