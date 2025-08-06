import java.io.*;

public class BMPImageProcessor {
    public static void main(String[] args) {
        String inputFilePath = "input.bmp";

        try (FileInputStream fis = new FileInputStream(inputFilePath)) {
            // Step 1: Read the header
            byte[] header = new byte[54];
            if (fis.read(header) != 54) {
                throw new IOException("Not a valid BMP file or header too short.");
            }

            // Step 2: Prepare the RGB array
            int width = 256;
            int height = 256;
            int padding = (4 - (width * 3) % 4) % 4; // BMP rows are padded to multiples of 4 bytes
            byte[] pixelArray = new byte[width * height * 3];

            int index = 0;

            // BMP pixel data is stored bottom-up
            for (int row = height - 1; row >= 0; row--) {
                for (int col = 0; col < width; col++) {
                    int blue = fis.read();
                    int green = fis.read();
                    int red = fis.read();

                    // Store as RGB in the output array
                    int pixelIndex = (row * width + col) * 3;
                    pixelArray[pixelIndex] = (byte) red;
                    pixelArray[pixelIndex + 1] = (byte) green;
                    pixelArray[pixelIndex + 2] = (byte) blue;
                }
                // Skip padding bytes
                fis.skip(padding);
            }

            // Example: Print first 10 RGB values
            System.out.println("First 10 RGB values:");
            for (int i = 0; i < 30; i++) {
                System.out.print((pixelArray[i] & 0xFF) + " ");
            }

        } catch (IOException e) {
            System.err.println("Error reading BMP: " + e.getMessage());
        }
    }
}
