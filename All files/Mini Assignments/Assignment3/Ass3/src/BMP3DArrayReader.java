import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class BMP3DArrayReader {

    public static void main(String[] args) {
        String inputFilePath = "D:\\Final year project\\Assignment3\\Ass3\\src\\input.bmp.bmp"; // Make sure this exists in your directory
        final int WIDTH = 256;
        final int HEIGHT = 256;
        final int HEADER_SIZE = 54;
        final int PIXEL_SIZE = 3;
        final int PIXEL_DATA_SIZE = WIDTH * HEIGHT * PIXEL_SIZE;

        byte[] header = new byte[HEADER_SIZE];
        byte[] pixelBytes = new byte[PIXEL_DATA_SIZE];

        // 3D array to hold pixel data: [row][column][channel]
        int[][][] pixelArray = new int[HEIGHT][WIDTH][3]; // channel 0=Blue, 1=Green, 2=Red

        try (FileInputStream fis = new FileInputStream(inputFilePath)) {

            // Read header
            if (fis.read(header) != HEADER_SIZE) {
                System.out.println("Error: Could not read BMP header.");
                return;
            }

            // Read pixel data
            if (fis.read(pixelBytes) != PIXEL_DATA_SIZE) {
                System.out.println("Error: Could not read complete pixel data.");
                return;
            }

            // Fill 3D array (remember BMP is bottom-up)
            int index = 0;
            for (int row = HEIGHT - 1; row >= 0; row--) {
                for (int col = 0; col < WIDTH; col++) {
                    pixelArray[row][col][0] = Byte.toUnsignedInt(pixelBytes[index++]); // Blue
                    pixelArray[row][col][1] = Byte.toUnsignedInt(pixelBytes[index++]); // Green
                    pixelArray[row][col][2] = Byte.toUnsignedInt(pixelBytes[index++]); // Red
                }
            }

            // Ask user for row and column
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter row (0–255): ");
            int userRow = scanner.nextInt();

            System.out.print("Enter column (0–255): ");
            int userCol = scanner.nextInt();

            // Validate and show pixel RGB
            if (userRow >= 0 && userRow < HEIGHT && userCol >= 0 && userCol < WIDTH) {
                System.out.println("Pixel at (" + userRow + ", " + userCol + ") RGB values:");
                System.out.println("R: " + pixelArray[userRow][userCol][2]);
                System.out.println("G: " + pixelArray[userRow][userCol][1]);
                System.out.println("B: " + pixelArray[userRow][userCol][0]);
            } else {
                System.out.println("Invalid input. Row and column must be in range 0–255.");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
