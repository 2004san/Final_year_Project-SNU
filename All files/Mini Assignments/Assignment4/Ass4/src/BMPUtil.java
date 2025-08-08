import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BMPUtil {
    private BufferedImage image;

    public BMPUtil(String path) throws IOException {
        this.image = ImageIO.read(new File(path));
        if (image.getWidth() != 256 || image.getHeight() != 256) {
            throw new IllegalArgumentException("Image must be 256x256 pixels.");
        }
    }

    public int getTotalPixels() {
        return image.getWidth() * image.getHeight();
    }

    public int[] getPixel(int index) {
        int x = index % 256;
        int y = index / 256;
        int rgb = image.getRGB(x, y);
        int[] color = new int[3];
        color[0] = (rgb >> 16) & 0xFF; // Red
        color[1] = (rgb >> 8) & 0xFF; // Green
        color[2] = rgb & 0xFF; // Blue
        return color;
    }

    public void setPixel(int index, int r, int g, int b) {
        int x = index % 256;
        int y = index / 256;
        int rgb = (r << 16) | (g << 8) | b;
        image.setRGB(x, y, rgb);
    }

    public void saveImage(String outputPath) throws IOException {
        ImageIO.write(image, "bmp", new File(outputPath));
    }
}
