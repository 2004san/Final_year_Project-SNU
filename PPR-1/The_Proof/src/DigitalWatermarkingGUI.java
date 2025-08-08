import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;

public class DigitalWatermarkingGUI extends JFrame {
    private JTextField watermarkField;
    private JPasswordField passwordField;
    private JTextArea logArea;
    private JButton embedButton, extractButton, loadImageButton, saveImageButton;
    private JLabel imageLabel, statusLabel;
    private BufferedImage originalImage, watermarkedImage;
    private File currentImageFile;

    public DigitalWatermarkingGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Digital Watermarking Tool - LSB Embedding with SHA-256 & CRC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        JPanel topPanel = createInputPanel();
        JPanel centerPanel = createImagePanel();
        JPanel rightPanel = createLogPanel();
        JPanel bottomPanel = createButtonPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Input Parameters"));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Watermark input
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Watermark (max 50 chars):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        watermarkField = new JTextField(30);
        panel.add(watermarkField, gbc);

        // Password input
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(30);
        panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Image Preview (256x256 BMP)"));

        imageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(280, 280));
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        panel.add(imageLabel, BorderLayout.CENTER);

        statusLabel = new JLabel("Status: Ready");
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Algorithm Proof & Step-by-Step Log"));

        logArea = new JTextArea(25, 40);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        loadImageButton = new JButton("Load 256x256 BMP Image");
        loadImageButton.addActionListener(this::loadImage);

        embedButton = new JButton("Embed Watermark");
        embedButton.addActionListener(this::embedWatermark);
        embedButton.setEnabled(false);

        extractButton = new JButton("Extract Watermark");
        extractButton.addActionListener(this::extractWatermark);
        extractButton.setEnabled(false);

        saveImageButton = new JButton("Save Watermarked Image");
        saveImageButton.addActionListener(this::saveImage);
        saveImageButton.setEnabled(false);

        panel.add(loadImageButton);
        panel.add(embedButton);
        panel.add(extractButton);
        panel.add(saveImageButton);

        return panel;
    }

    private void loadImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "BMP Images", "bmp"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentImageFile = fileChooser.getSelectedFile();
                originalImage = ImageIO.read(currentImageFile);

                if (originalImage.getWidth() != 256 || originalImage.getHeight() != 256) {
                    JOptionPane.showMessageDialog(this,
                            "Image must be exactly 256x256 pixels!", "Invalid Image Size",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Display image
                ImageIcon icon = new ImageIcon(originalImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                imageLabel.setText("");

                embedButton.setEnabled(true);
                statusLabel.setText("Status: Image loaded successfully");
                log("=== IMAGE LOADED ===");
                log("File: " + currentImageFile.getName());
                log("Dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
                log("Type: " + getImageTypeString(originalImage.getType()));
                log("");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void embedWatermark(ActionEvent e) {
        String watermark = watermarkField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validation
        if (watermark.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both watermark and password!");
            return;
        }
        if (watermark.length() > 50) {
            JOptionPane.showMessageDialog(this, "Watermark must be 50 characters or less!");
            return;
        }
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first!");
            return;
        }

        try {
            log("=== WATERMARK EMBEDDING PROCESS ===");
            log("Original Watermark: \"" + watermark + "\"");

            // Step 1: Add delimiters
            String delimitedWatermark = "@@" + watermark + "#@";
            log("Step 1 - Added Delimiters: \"" + delimitedWatermark + "\"");

            // Step 2: SHA-256 hash of password
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashHex = bytesToHex(hashBytes);
            log("Step 2 - SHA-256 Hash of Password:");
            log("  Hex: " + hashHex);
            log("  Bytes: " + java.util.Arrays.toString(hashBytes));

            // Step 3: CRC-32 of hash
            CRC32 crc32 = new CRC32();
            crc32.update(hashBytes);
            long crcValue = crc32.getValue();
            int stepSize = (int)(crcValue & 0xFF); // Take lower 8 bits (0-255)
            log("Step 3 - CRC-32 Calculation:");
            log("  Full CRC-32: " + crcValue + " (0x" + Long.toHexString(crcValue) + ")");
            log("  Lower 8 bits (Step Size): " + stepSize + " (0x" + Integer.toHexString(stepSize) + ")");

            // Step 4: Convert to character array and show ASCII values
            char[] chars = delimitedWatermark.toCharArray();
            log("Step 4 - Character Analysis:");
            for (int i = 0; i < chars.length; i++) {
                int ascii = (int) chars[i];
                String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');
                String redBits = binary.substring(0, 3);
                String greenBits = binary.substring(3, 6);
                String blueBits = binary.substring(6, 8);
                log(String.format("  '%c' -> ASCII: %d -> Binary: %s -> R:%s G:%s B:%s",
                        chars[i], ascii, binary, redBits, greenBits, blueBits));
            }

            // Step 5: Calculate pixel positions
            List<Integer> pixelPositions = new ArrayList<>();
            int currentPos = 0;
            log("Step 5 - Pixel Position Calculation:");
            for (int i = 0; i < chars.length; i++) {
                pixelPositions.add(currentPos);
                int x = currentPos % 256;
                int y = currentPos / 256;
                log(String.format("  Char %d ('%c'): Pixel %d (x:%d, y:%d)",
                        i, chars[i], currentPos, x, y));
                if (i < chars.length - 1) {
                    currentPos = (currentPos + stepSize) % (256 * 256);
                }
            }

            // Step 6: Create watermarked image and embed
            watermarkedImage = new BufferedImage(originalImage.getWidth(),
                    originalImage.getHeight(), originalImage.getType());
            Graphics2D g2d = watermarkedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            log("Step 6 - LSB Embedding Process:");
            for (int i = 0; i < chars.length; i++) {
                int pixelPos = pixelPositions.get(i);
                int x = pixelPos % 256;
                int y = pixelPos / 256;

                int originalRGB = watermarkedImage.getRGB(x, y);
                int originalR = (originalRGB >> 16) & 0xFF;
                int originalG = (originalRGB >> 8) & 0xFF;
                int originalB = originalRGB & 0xFF;

                int ascii = (int) chars[i];
                String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');

                // Extract LSB bits for embedding
                int redBits = Integer.parseInt(binary.substring(0, 3), 2);
                int greenBits = Integer.parseInt(binary.substring(3, 6), 2);
                int blueBits = Integer.parseInt(binary.substring(6, 8), 2);

                // Modify LSBs
                int newR = (originalR & 0xF8) | redBits;   // Clear last 3 bits, set new ones
                int newG = (originalG & 0xF8) | greenBits; // Clear last 3 bits, set new ones
                int newB = (originalB & 0xFC) | blueBits;  // Clear last 2 bits, set new ones

                int newRGB = (newR << 16) | (newG << 8) | newB;
                watermarkedImage.setRGB(x, y, newRGB);

                log(String.format("  Pixel %d: RGB(%d,%d,%d) -> RGB(%d,%d,%d) [Changed: R:%s G:%s B:%s]",
                        pixelPos, originalR, originalG, originalB, newR, newG, newB,
                        binary.substring(0, 3), binary.substring(3, 6), binary.substring(6, 8)));
            }

            // Update display
            ImageIcon icon = new ImageIcon(watermarkedImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);

            extractButton.setEnabled(true);
            saveImageButton.setEnabled(true);
            statusLabel.setText("Status: Watermark embedded successfully");
            log("=== EMBEDDING COMPLETED ===\n");

            JOptionPane.showMessageDialog(this, "Watermark embedded successfully!\n" +
                    "Total characters embedded: " + chars.length + "\n" +
                    "Step size (CRC): " + stepSize, "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error embedding watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void extractWatermark(ActionEvent e) {
        String password = new String(passwordField.getPassword());

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the password!");
            return;
        }
        if (watermarkedImage == null) {
            JOptionPane.showMessageDialog(this, "Please embed a watermark first!");
            return;
        }

        try {
            log("=== WATERMARK EXTRACTION PROCESS ===");

            // Step 1: Generate same SHA-256 hash
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashHex = bytesToHex(hashBytes);
            log("Step 1 - SHA-256 Hash Verification:");
            log("  Hex: " + hashHex);

            // Step 2: Generate same CRC step size
            CRC32 crc32 = new CRC32();
            crc32.update(hashBytes);
            long crcValue = crc32.getValue();
            int stepSize = (int)(crcValue & 0xFF);
            log("Step 2 - CRC-32 Step Size: " + stepSize);

            // Step 3: Extract characters
            StringBuilder extractedText = new StringBuilder();
            int currentPos = 0;
            boolean foundStart = false;
            int charIndex = 0;

            log("Step 3 - Character Extraction:");

            // Extract until we find the end marker or reach reasonable limit
            while (extractedText.length() < 100) { // Safety limit
                int x = currentPos % 256;
                int y = currentPos / 256;

                int rgb = watermarkedImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Extract LSBs
                String redBits = String.format("%3s", Integer.toBinaryString(r & 0x07)).replace(' ', '0');
                String greenBits = String.format("%3s", Integer.toBinaryString(g & 0x07)).replace(' ', '0');
                String blueBits = String.format("%2s", Integer.toBinaryString(b & 0x03)).replace(' ', '0');

                String binaryChar = redBits + greenBits + blueBits;
                int ascii = Integer.parseInt(binaryChar, 2);
                char extractedChar = (char) ascii;

                log(String.format("  Pixel %d (x:%d, y:%d): RGB(%d,%d,%d) -> LSBs: %s -> ASCII: %d -> '%c'",
                        currentPos, x, y, r, g, b, binaryChar, ascii,
                        (ascii >= 32 && ascii <= 126) ? extractedChar : '?'));

                extractedText.append(extractedChar);

                // Check for start and end markers
                String current = extractedText.toString();
                if (!foundStart && current.contains("@@")) {
                    foundStart = true;
                    log("  -> Found START marker '@@'");
                }

                if (foundStart && current.contains("#@")) {
                    log("  -> Found END marker '#@'");
                    break;
                }

                currentPos = (currentPos + stepSize) % (256 * 256);
                charIndex++;

                // Safety check
                if (charIndex > 60) { // More than reasonable for 50 char + delimiters
                    log("  -> Safety limit reached, stopping extraction");
                    break;
                }
            }

            // Step 4: Validate and extract watermark
            String fullExtracted = extractedText.toString();
            log("Step 4 - Full Extracted String: \"" + fullExtracted + "\"");

            if (!fullExtracted.startsWith("@@")) {
                JOptionPane.showMessageDialog(this, "Wrong password or no watermark found!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
                log("ERROR: Start marker '@@' not found!");
                return;
            }

            int startIndex = fullExtracted.indexOf("@@") + 2;
            int endIndex = fullExtracted.indexOf("#@");

            if (endIndex == -1 || endIndex < startIndex) {
                JOptionPane.showMessageDialog(this, "Wrong password or corrupted watermark!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
                log("ERROR: End marker '#@' not found or invalid position!");
                return;
            }

            String extractedWatermark = fullExtracted.substring(startIndex, endIndex);
            log("Step 5 - Extracted Watermark: \"" + extractedWatermark + "\"");
            log("=== EXTRACTION COMPLETED ===\n");

            // Compare with original
            String originalWatermark = watermarkField.getText().trim();
            boolean matches = extractedWatermark.equals(originalWatermark);

            String message = "Extraction Results:\n\n" +
                    "Extracted Watermark: \"" + extractedWatermark + "\"\n" +
                    "Original Watermark: \"" + originalWatermark + "\"\n" +
                    "Match: " + (matches ? "YES ✓" : "NO ✗") + "\n\n" +
                    "Step Size Used: " + stepSize;

            JOptionPane.showMessageDialog(this, message,
                    matches ? "Extraction Successful" : "Extraction Warning",
                    matches ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

            statusLabel.setText("Status: Watermark extracted - " + (matches ? "Match!" : "No match"));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error extracting watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveImage(ActionEvent e) {
        if (watermarkedImage == null) {
            JOptionPane.showMessageDialog(this, "No watermarked image to save!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "BMP Images", "bmp"));
        fileChooser.setSelectedFile(new File("watermarked_image.bmp"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".bmp")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".bmp");
                }

                ImageIO.write(watermarkedImage, "BMP", outputFile);
                statusLabel.setText("Status: Watermarked image saved");
                log("Image saved: " + outputFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Watermarked image saved successfully!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    private String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_BYTE_GRAY: return "BYTE_GRAY";
            case BufferedImage.TYPE_USHORT_GRAY: return "USHORT_GRAY";
            case BufferedImage.TYPE_BYTE_BINARY: return "BYTE_BINARY";
            case BufferedImage.TYPE_BYTE_INDEXED: return "BYTE_INDEXED";
            case BufferedImage.TYPE_USHORT_565_RGB: return "USHORT_565_RGB";
            case BufferedImage.TYPE_USHORT_555_RGB: return "USHORT_555_RGB";
            default: return "CUSTOM";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DigitalWatermarkingGUI().setVisible(true);
        });
    }
}