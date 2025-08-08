import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BMPImageProcessor {

    public static void main(String[] args) {
        try {
            // ✅ Step 1: Load the image
            File file = new File(
                    "C:\\Users\\Akash Chakraborty\\OneDrive\\Desktop\\Final_year_Project-SNU\\Assests\\BMP Images\\Butterfly.bmp");
            System.out.println("Looking for file: " + file.getAbsolutePath());
            if (!file.exists()) {
                System.err.println("✘ File not found!");
                return;
            }

            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();

            // ✅ Step 2: Draw spiral in the center
            int centerX = width / 2;
            int centerY = height / 2;

            double angle = 0.0;
            double radius = 0.0;
            double angleStep = 0.1;
            double radiusStep = 0.5;

            while (radius < Math.min(width, height) / 4.0) {
                int x = (int) (centerX + radius * Math.cos(angle));
                int y = (int) (centerY + radius * Math.sin(angle));

                // Boundary check
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    image.setRGB(x, y, 0xFF000000); // Black pixel
                }

                angle += angleStep;
                radius += radiusStep * angleStep; // Increase radius gradually
            }

            // ✅ Step 3: Save the modified image
            File output = new File(
                    "C:\\Users\\Akash Chakraborty\\OneDrive\\Desktop\\Final_year_Project-SNU\\Mini Assignments\\Assignment 5\\image_spiral_black.bmp");
            ImageIO.write(image, "bmp", output);

            System.out.println("✔ Spiral added in the center and saved to image_spiral_black.bmp");

        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }
}
