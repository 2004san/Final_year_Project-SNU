public class WatermarkExtractor {
    public static String extractWatermark(BMPUtil bmp, int jump) {
        StringBuilder result = new StringBuilder();
        int pixelIndex = jump;

        while (pixelIndex < bmp.getTotalPixels()) {
            int[] rgb = bmp.getPixel(pixelIndex);

            int redLSB = rgb[0] & 0b00000111;
            int greenLSB = rgb[1] & 0b00000111;
            int blueLSB = rgb[2] & 0b00000011;

            String binary = String.format("%3s", Integer.toBinaryString(redLSB)).replace(' ', '0') +
                    String.format("%3s", Integer.toBinaryString(greenLSB)).replace(' ', '0') +
                    String.format("%2s", Integer.toBinaryString(blueLSB)).replace(' ', '0');

            int ascii = Integer.parseInt(binary, 2);
            result.append((char) ascii);

            if (result.length() > 12 && result.toString().endsWith("##@@##")) {
                break;
            }

            pixelIndex += jump;
        }

        return result.toString();
    }
}
