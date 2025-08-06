import java.awt.image.BufferedImage;

public class LSBEncoder {
    public static BufferedImage encodeText(BufferedImage image, String text) {
        byte[] msgBytes = text.getBytes();
        int msgLength = msgBytes.length;

        int width = image.getWidth();
        int height = image.getHeight();
        int msgIndex = 0;
        int bitIndex = 0;

        outerLoop: for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;

                if (msgIndex < msgLength) {
                    int bit = (msgBytes[msgIndex] >> (7 - bitIndex)) & 1;
                    red = (red & 0xFE) | bit;
                    bitIndex++;
                    if (bitIndex == 8) {
                        bitIndex = 0;
                        msgIndex++;
                    }
                } else {
                    break outerLoop;
                }

                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int newPixel = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }

        return image;
    }
}
