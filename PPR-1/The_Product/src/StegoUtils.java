import java.awt.image.BufferedImage;

public class StegoUtils {

    public static BufferedImage embedWatermark(BufferedImage image, String watermark, String password) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        byte[] hash = HashUtils.getSHA256Hash(password);
        int crc = HashUtils.getCRC8(hash);
        int step = crc + 1;

        String fullWatermark = "@@" + watermark + "#@";

        int pixelIndex = 0;
        for (int i = 0; i < fullWatermark.length(); i++) {
            char c = fullWatermark.charAt(i);
            int ascii = (int) c;
            String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');

            if (pixelIndex >= totalPixels) {
                throw new RuntimeException("Watermark too long for image.");
            }

            int x = pixelIndex % width;
            int y = pixelIndex / width;

            int rgb = image.getRGB(x, y);
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            red = (red & 0b11111000) | Integer.parseInt(binary.substring(0, 3), 2);
            green = (green & 0b11111000) | Integer.parseInt(binary.substring(3, 6), 2);
            blue = (blue & 0b11111100) | Integer.parseInt(binary.substring(6, 8), 2);

            int newRgb = (red << 16) | (green << 8) | blue;
            image.setRGB(x, y, (0xFF << 24) | newRgb);

            pixelIndex += step;
        }

        return image;
    }

    public static String extractWatermark(BufferedImage image, String password) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        byte[] hash = HashUtils.getSHA256Hash(password);
        int crc = HashUtils.getCRC8(hash);
        int step = crc + 1;

        StringBuilder watermarkBuilder = new StringBuilder();
        int pixelIndex = 0;

        while (pixelIndex < totalPixels) {
            int x = pixelIndex % width;
            int y = pixelIndex / width;

            int rgb = image.getRGB(x, y);
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            String binary =
                    String.format("%3s", Integer.toBinaryString(red & 0b00000111)).replace(' ', '0') +
                            String.format("%3s", Integer.toBinaryString(green & 0b00000111)).replace(' ', '0') +
                            String.format("%2s", Integer.toBinaryString(blue & 0b00000011)).replace(' ', '0');

            int ascii = Integer.parseInt(binary, 2);
            char c = (char) ascii;
            watermarkBuilder.append(c);

            String result = watermarkBuilder.toString();

            if (result.startsWith("@@") && result.contains("#@")) {
                int start = result.indexOf("@@") + 2;
                int end = result.indexOf("#@");
                return result.substring(start, end);
            }

            if (watermarkBuilder.length() > 4 && !result.startsWith("@@")) {
                return "Wrong password or watermark not found.";
            }

            pixelIndex += step;
        }

        return "Watermark not found.";
    }
}
