import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class LSBSteganography {
    public static BufferedImage embedText(BufferedImage image, String text, List<Point> pixelLocations) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length && i < pixelLocations.size(); i++) {
            Point p = pixelLocations.get(i);
            int rgb = image.getRGB(p.x, p.y);
            Color color = new Color(rgb);

            int ascii = chars[i];
            String binary = String.format("%08d", Integer.parseInt(Integer.toBinaryString(ascii)));
            int r = (color.getRed() & 0xF8) | Integer.parseInt(binary.substring(0, 3), 2);
            int g = (color.getGreen() & 0xF8) | Integer.parseInt(binary.substring(3, 6), 2);
            int b = (color.getBlue() & 0xFC) | Integer.parseInt(binary.substring(6), 2);

            Color newColor = new Color(r, g, b);
            image.setRGB(p.x, p.y, newColor.getRGB());
        }
        return image;
    }

    public static String extractText(BufferedImage image, List<Point> pixelLocations) {
        StringBuilder sb = new StringBuilder();
        for (Point p : pixelLocations) {
            Color color = new Color(image.getRGB(p.x, p.y));
            int r = color.getRed() & 0x07;
            int g = color.getGreen() & 0x07;
            int b = color.getBlue() & 0x03;

            String binary = String.format("%3s%3s%2s",
                    Integer.toBinaryString(r),
                    Integer.toBinaryString(g),
                    Integer.toBinaryString(b)).replace(' ', '0');

            int ascii = Integer.parseInt(binary, 2);
            sb.append((char) ascii);

            if (sb.length() >= 2 && sb.substring(sb.length() - 2).equals("#@")) {
                break;
            }
        }

        String result = sb.toString();
        if (result.startsWith("@@") && result.endsWith("#@")) {
            return result.substring(2, result.length() - 2);
        } else {
            return "Wrong password or corrupted data.";
        }
    }
}
