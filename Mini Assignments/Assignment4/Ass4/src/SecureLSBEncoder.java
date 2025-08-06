import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.*;

public class SecureLSBEncoder {
    public static BufferedImage encode(BufferedImage image, String message, String password) {
        byte[] msgBytes = message.getBytes();
        int totalBits = msgBytes.length * 8;

        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        if (totalBits > totalPixels) {
            throw new IllegalArgumentException("Message too long to hide in this image.");
        }

        // Generate pseudo-random pixel positions based on password
        List<Integer> pixelOrder = getShuffledPixelIndices(totalPixels, password);

        int bitIndex = 0;
        for (int i = 0; i < totalBits; i++) {
            int pixelPos = pixelOrder.get(i);
            int x = pixelPos % width;
            int y = pixelPos / width;

            int pixel = image.getRGB(x, y);
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;

            int byteIndex = i / 8;
            int bitPos = 7 - (i % 8);
            int bit = (msgBytes[byteIndex] >> bitPos) & 1;

            red = (red & 0xFE) | bit; // Replace LSB with message bit

            int newPixel = (red << 16) | (green << 8) | blue;
            image.setRGB(x, y, newPixel);
        }

        return image;
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
