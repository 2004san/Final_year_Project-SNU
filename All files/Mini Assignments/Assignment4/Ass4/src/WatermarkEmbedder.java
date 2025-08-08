public class WatermarkEmbedder {
    public static boolean embedWatermark(BMPUtil bmp, String watermark, int jump) {
        int pixelIndex = jump;

        for (char ch : watermark.toCharArray()) {
            if (pixelIndex >= bmp.getTotalPixels()) {
                return false; // Out of bounds
            }

            int ascii = (int) ch;
            String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');

            // Split into 3-3-2
            String redBits = binary.substring(0, 3);
            String greenBits = binary.substring(3, 6);
            String blueBits = binary.substring(6, 8);

            int[] rgb = bmp.getPixel(pixelIndex);

            // Embed LSBs
            rgb[0] = (rgb[0] & 0b11111000) | Integer.parseInt(redBits, 2); // Red
            rgb[1] = (rgb[1] & 0b11111000) | Integer.parseInt(greenBits, 2); // Green
            rgb[2] = (rgb[2] & 0b11111100) | Integer.parseInt(blueBits, 2); // Blue

            bmp.setPixel(pixelIndex, rgb[0], rgb[1], rgb[2]);

            pixelIndex += jump;
        }

        return true;
    }
}
