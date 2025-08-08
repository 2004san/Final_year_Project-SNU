import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            // Step 1: Load BMP image
            System.out.print("Enter path to 256x256 BMP image: ");
            String imagePath = sc.nextLine().trim();
            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                System.err.println("❌ File not found!");
                return;
            }

            // Step 2: Get password & hash
            System.out.print("Enter password: ");
            String password = sc.nextLine().trim();
            String sha256Hash = HashUtil.getSHA256(password);
            int crcDecimal = CRCUtil.computeCRCDecimal(sha256Hash);
            System.out.println("🔑 CRC Decimal: " + crcDecimal);

            // Step 3: Get watermark
            System.out.print("Enter watermark (max 50 characters): ");
            String watermark = sc.nextLine();
            if (watermark.length() > 50) {
                System.err.println("❌ Watermark too long!");
                return;
            }

            String finalWatermark = "##@@##" + watermark + "##@@##";

            // Step 4: Load image & embed
            BMPUtil bmp = new BMPUtil(imagePath);
            boolean success = WatermarkEmbedder.embedWatermark(bmp, finalWatermark, crcDecimal);

            // Step 5: Save output
            if (success) {
                String outputPath = "watermarked_output.bmp";
                bmp.saveImage(outputPath);
                System.out.println("✅ Watermarked image saved to: " + outputPath);
            } else {
                System.err.println("❌ Embedding failed: Pixel index out of bounds.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}