// WatermarkApp.java
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class WatermarkApp extends JFrame {
    private BufferedImage originalImage;
    private JLabel imageLabel;

    public WatermarkApp() {
        setTitle("Digital Watermarking Tool");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        JButton loadButton = new JButton("Load BMP Image");
        JButton embedButton = new JButton("Add Watermark");
        JButton extractButton = new JButton("Fetch Watermark");

        panel.add(loadButton);
        panel.add(embedButton);
        panel.add(extractButton);
        add(panel, BorderLayout.SOUTH);

        imageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        add(imageLabel, BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadImage());
        embedButton.addActionListener(e -> embedWatermark());
        extractButton.addActionListener(e -> extractWatermark());
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(file);
                imageLabel.setIcon(new ImageIcon(originalImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH)));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image");
            }
        }
    }

    private void embedWatermark() {
        if (originalImage == null) return;

        String watermark = JOptionPane.showInputDialog("Enter watermark text (max 50 chars):");
        if (watermark == null || watermark.length() > 50) return;

        String password = JOptionPane.showInputDialog("Enter password:");
        if (password == null || password.isEmpty()) return;

        byte[] hash = Utils.getSHA256Hash(password);
        System.out.print("[1] SHA-256 Hash of Password:\n");
        for (byte b : hash) {
            System.out.printf("%02x ", b);
        }
        System.out.println();

        int crc = Utils.calculateCRC8(hash);
        System.out.println("[2] CRC-8 of SHA256: " + crc);

        String fullWatermark = "@@" + watermark + "#@";
        List<Point> positions = Utils.getPixelPositions(crc, fullWatermark.length());

        System.out.println("[3] Pixel positions for embedding:");
        for (Point p : positions) {
            System.out.println("(" + p.x + "," + p.y + ")");
        }

        BufferedImage watermarked = LSBSteganography.embedText(originalImage, fullWatermark, positions);
        try {
            File output = new File("watermarked_output.bmp");
            ImageIO.write(watermarked, "bmp", output);
            JOptionPane.showMessageDialog(this, "Watermark embedded and saved as watermarked_output.bmp");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving watermarked image");
        }
    }

    private void extractWatermark() {
        if (originalImage == null) return;

        String password = JOptionPane.showInputDialog("Enter password:");
        if (password == null || password.isEmpty()) return;

        byte[] hash = Utils.getSHA256Hash(password);
        int crc = Utils.calculateCRC8(hash);
        List<Point> positions = Utils.getPixelPositions(crc, 100);

        String watermark = LSBSteganography.extractText(originalImage, positions);
        JOptionPane.showMessageDialog(this, "Extracted Watermark: " + watermark);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WatermarkApp().setVisible(true));
    }
}
