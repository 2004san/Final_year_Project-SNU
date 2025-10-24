import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.imageio.ImageIO;

public class DWM2 extends JFrame {
    private JTextField watermarkField;
    private JPasswordField passwordField;
    private JTextArea logArea;
    private JButton embedButton, extractButton, loadImageButton, saveImageButton;
    private JLabel imageLabel, statusLabel, timestampLabel;
    private BufferedImage originalImage, watermarkedImage;
    private File currentImageFile;

    public DWM2() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("DWM2 - Digital Watermarking with Password-based MD5 & Dynamic Positioning");
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

        // Watermark input (16 chars max)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Watermark (max 16 chars):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        watermarkField = new JTextField(20);
        panel.add(watermarkField, gbc);

        // Password input (optional)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password (optional):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Current timestamp display
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Current Time:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        timestampLabel = new JLabel(getCurrentTimestamp());
        timestampLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(timestampLabel, gbc);

        // Update timestamp every second
        Timer timer = new Timer(1000, _ -> timestampLabel.setText(getCurrentTimestamp()));
        timer.start();

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
        panel.setBorder(new TitledBorder("Algorithm Log & MD5/ASCII Details"));

        logArea = new JTextArea(25, 40);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.addActionListener(_ -> logArea.setText(""));
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

        extractButton = new JButton("Extract & Verify Watermark");
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

    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy");
        return now.format(formatter);
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
                extractButton.setEnabled(true);
                statusLabel.setText("Status: Image loaded successfully");
                log("=== IMAGE LOADED ===");
                log("File: " + currentImageFile.getName());
                log("Dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
                log("");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String calculateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private int calculateXORFromHash(String hash) {
        int result = Integer.parseInt(hash.substring(0, 2), 16); // Start with first byte

        // XOR all subsequent byte pairs
        for (int i = 2; i < hash.length(); i += 2) {
            int nextByte = Integer.parseInt(hash.substring(i, i + 2), 16);
            result ^= nextByte;
        }

        return result; // Will be 0-255
    }

    private int calculateXORFirst12Bytes(String hash) {
        int result = Integer.parseInt(hash.substring(0, 2), 16); // Start with first byte

        // XOR first 12 bytes (24 hex chars)
        for (int i = 2; i < 24 && i < hash.length(); i += 2) {
            int nextByte = Integer.parseInt(hash.substring(i, i + 2), 16);
            result ^= nextByte;
        }

        return result; // Will be 0-255
    }

    private int calculateXORLast4Bytes(String hash) {
        int result = Integer.parseInt(hash.substring(24, 26), 16); // 13th byte

        // XOR last 4 bytes (starting from position 24)
        for (int i = 26; i < hash.length(); i += 2) {
            int nextByte = Integer.parseInt(hash.substring(i, i + 2), 16);
            result ^= nextByte;
        }

        return result; // Will be 0-255
    }

    private void embedWatermark(ActionEvent e) {
        String watermark = watermarkField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validation
        if (watermark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a watermark!");
            return;
        }
        if (watermark.length() > 16) {
            JOptionPane.showMessageDialog(this, "Watermark must be 16 characters or less!");
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password!");
            return;
        }
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first!");
            return;
        }

        try {
            log("=== WATERMARK EMBEDDING PROCESS (DWM2 - NEW FLOW) ===");
            log("Input watermark: \"" + watermark + "\"");
            log("Input password: \"" + password + "\"");

            // Step 1: Get current timestamp
            String timestamp = getCurrentTimestamp();
            log("Step 1 - Current timestamp: " + timestamp);

            // Step 2: Create full watermark string with delimiter
            String fullWatermark = watermark + "#" + timestamp + "#@";
            log("Step 2 - Full watermark with delimiter: \"" + fullWatermark + "\"");
            log("  Structure: watermark + '#' (delimiter) + timestamp + '#@' (end marker)");

            // Step 3: Calculate MD5 hash of PASSWORD and XOR calculations
            String passwordMD5Hash = calculateMD5Hash(password);
            int baseOffsetX = calculateXORFirst12Bytes(passwordMD5Hash);
            int hopDistanceY = calculateXORLast4Bytes(passwordMD5Hash);

            log("Step 3 - Password MD5 Hash and XOR calculations:");
            log("  Password: \"" + password + "\"");
            log("  Password MD5 Hash: " + passwordMD5Hash);
            log("  XOR calculation (first 12 bytes):");

            // Show XOR process for first 12 bytes step by step
            int xorResult = Integer.parseInt(passwordMD5Hash.substring(0, 2), 16);
            StringBuilder xorLog = new StringBuilder("    " + passwordMD5Hash.substring(0, 2));
            for (int i = 2; i < 24; i += 2) {
                int nextByte = Integer.parseInt(passwordMD5Hash.substring(i, i + 2), 16);
                xorResult ^= nextByte;
                xorLog.append(" XOR ").append(passwordMD5Hash.substring(i, i + 2)).append(" = ")
                        .append(String.format("%02X", xorResult));
                if ((i / 2) % 4 == 3) { // Line break every 4 operations
                    log(xorLog.toString());
                    xorLog = new StringBuilder("    ");
                }
            }
            log(xorLog.toString());
            log("  X (First 12 bytes XOR result): " + baseOffsetX + " (0x" + String.format("%02X", baseOffsetX) + ")");

            log("  XOR calculation (last 4 bytes):");
            int xorResultLast = Integer.parseInt(passwordMD5Hash.substring(24, 26), 16);
            StringBuilder xorLogLast = new StringBuilder("    " + passwordMD5Hash.substring(24, 26));
            for (int i = 26; i < 32; i += 2) {
                int nextByte = Integer.parseInt(passwordMD5Hash.substring(i, i + 2), 16);
                xorResultLast ^= nextByte;
                xorLogLast.append(" XOR ").append(passwordMD5Hash.substring(i, i + 2)).append(" = ")
                        .append(String.format("%02X", xorResultLast));
            }
            log(xorLogLast.toString());
            log("  Y (Last 4 bytes XOR result / hop distance): " + hopDistanceY + " (0x"
                    + String.format("%02X", hopDistanceY) + ")");

            // Step 4: Create watermarked image
            watermarkedImage = new BufferedImage(originalImage.getWidth(),
                    originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = watermarkedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            // Step 4: Check for collision at position X
            log("Step 4 - Check initial position X for collision:");
            int initialPixelX = baseOffsetX % (256 * 256);
            int xX = initialPixelX % 256;
            int yX = initialPixelX / 256;
            int existingRGB = originalImage.getRGB(xX, yX);
            int existingR = (existingRGB >> 16) & 0xFF;
            int existingG = (existingRGB >> 8) & 0xFF;
            int existingB = existingRGB & 0xFF;

            // Check if '*' marker already embedded (check LSBs for ASCII 42)
            String existingRedBits = String.format("%3s", Integer.toBinaryString(existingR & 0x07)).replace(' ', '0');
            String existingGreenBits = String.format("%3s", Integer.toBinaryString(existingG & 0x07)).replace(' ', '0');
            String existingBlueBits = String.format("%2s", Integer.toBinaryString(existingB & 0x03)).replace(' ', '0');
            String existingBinary = existingRedBits + existingGreenBits + existingBlueBits;
            int existingASCII = Integer.parseInt(existingBinary, 2);

            if (existingASCII == 42) { // ASCII for '*'
                log("  ERROR: Collision detected! Position " + initialPixelX + " already contains '*' marker");
                JOptionPane.showMessageDialog(this,
                        "Password collision detected!\n" +
                                "A watermark with this password already exists at position " + initialPixelX + ".\n" +
                                "Please use a different password.",
                        "Collision Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            log("  Position X = " + initialPixelX + " (x:" + xX + ", y:" + yX + ") - No collision detected");

            // Step 5: Embed characters using new flow
            log("Step 5 - Embedding with new position flow:");
            log("  Position flow: X -> X+Y -> prev+ASCII(char) -> ... -> last+X (for timestamp)");

            // Build array with '*' marker + fullWatermark
            char[] fullWatermarkChars = fullWatermark.toCharArray();
            char[] chars = new char[fullWatermarkChars.length + 1];
            chars[0] = '*'; // Marker
            System.arraycopy(fullWatermarkChars, 0, chars, 1, fullWatermarkChars.length);

            int currentPosition = baseOffsetX;

            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                int ascii = (int) c;

                // Calculate current pixel position based on new flow
                int currentPixel;

                if (i == 0) {
                    // Position 0: Embed '*' marker at X
                    currentPixel = currentPosition % (256 * 256);
                    log("  Pos 0: '*' (marker) -> Pixel " + currentPixel + " (X position)");
                } else if (i == 1) {
                    // Position 1: X + Y, embed first watermark char
                    currentPosition += hopDistanceY;
                    currentPixel = currentPosition % (256 * 256);
                    log("  Pos 1: '" + c + "' (ASCII " + ascii + ") -> Pixel " + currentPixel + " (X + Y = "
                            + baseOffsetX + " + " + hopDistanceY + " = " + currentPosition + ")");
                } else if (i <= watermark.length()) {
                    // Positions 2 to watermark.length: prev + ASCII of previous char
                    int prevChar = (int) chars[i - 1];
                    currentPosition += prevChar;
                    currentPixel = currentPosition % (256 * 256);
                    log("  Pos " + i + ": '" + c + "' (ASCII " + ascii + ") -> Pixel " + currentPixel
                            + " (prev + ASCII(" + chars[i - 1] + ") = " + (currentPosition - prevChar) + " + "
                            + prevChar + " = " + currentPosition + ")");
                } else if (i == watermark.length() + 1) {
                    // Position watermark.length + 1: Embed delimiter '#', still use ASCII-based
                    // positioning
                    int prevChar = (int) chars[i - 1];
                    currentPosition += prevChar;
                    currentPixel = currentPosition % (256 * 256);
                    log("  Pos " + i + ": '" + c + "' (delimiter '#', ASCII " + ascii + ") -> Pixel " + currentPixel
                            + " (prev + ASCII(" + chars[i - 1] + ") = " + (currentPosition - prevChar) + " + "
                            + prevChar + " = " + currentPosition + ")");
                } else {
                    // For timestamp and end marker (after '#'): hop by X
                    currentPosition += baseOffsetX;
                    currentPixel = currentPosition % (256 * 256);
                    log("  Pos " + i + ": '" + c + "' (ASCII " + ascii + ") -> Pixel " + currentPixel + " (prev + X = "
                            + (currentPosition - baseOffsetX) + " + " + baseOffsetX + " = " + currentPosition + ")");
                }

                int x = currentPixel % 256;
                int y = currentPixel / 256;

                // Get original pixel
                int originalRGB = watermarkedImage.getRGB(x, y);
                int originalR = (originalRGB >> 16) & 0xFF;
                int originalG = (originalRGB >> 8) & 0xFF;
                int originalB = originalRGB & 0xFF;

                // Embed character in LSBs (using 8 bits across RGB: R=3, G=3, B=2)
                String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');

                int redBits = Integer.parseInt(binary.substring(0, 3), 2);
                int greenBits = Integer.parseInt(binary.substring(3, 6), 2);
                int blueBits = Integer.parseInt(binary.substring(6, 8), 2);

                int newR = (originalR & 0xF8) | redBits;
                int newG = (originalG & 0xF8) | greenBits;
                int newB = (originalB & 0xFC) | blueBits;

                int newRGB = (newR << 16) | (newG << 8) | newB;
                watermarkedImage.setRGB(x, y, newRGB);

                log(String.format("    Binary: %s -> R:%s G:%s B:%s",
                        binary, binary.substring(0, 3), binary.substring(3, 6), binary.substring(6, 8)));
                log(String.format("    RGB: (%d,%d,%d) -> (%d,%d,%d)",
                        originalR, originalG, originalB, newR, newG, newB));
            }

            log("  Total characters embedded: " + chars.length);
            log("  Embedded string: \"" + new String(chars) + "\"");

            // Update display
            ImageIcon icon = new ImageIcon(watermarkedImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);

            extractButton.setEnabled(true);
            saveImageButton.setEnabled(true);
            statusLabel.setText("Status: Watermark embedded successfully");
            log("=== EMBEDDING COMPLETED ===\n");

            JOptionPane.showMessageDialog(this,
                    "Watermark embedded successfully!\n" +
                            "Characters embedded: " + chars.length + " (including '*' marker)\n" +
                            "X (First 12 bytes XOR): " + baseOffsetX + "\n" +
                            "Y (Last 4 bytes XOR / hop): " + hopDistanceY + "\n" +
                            "Embedded string: \"" + new String(chars) + "\"",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error embedding watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void extractWatermark(ActionEvent e) {
        String password = new String(passwordField.getPassword()).trim();

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the password!");
            return;
        }
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first!");
            return;
        }

        // Use watermarked image if available, otherwise use original (for
        // pre-watermarked images)
        BufferedImage imageToExtractFrom = (watermarkedImage != null) ? watermarkedImage : originalImage;

        try {
            log("=== WATERMARK EXTRACTION PROCESS (DWM2 - NEW FLOW) ===");
            log("Password: \"" + password + "\"");

            // Step 1: Calculate MD5 hash of password and XOR calculations
            String passwordMD5Hash = calculateMD5Hash(password);
            int baseOffsetX = calculateXORFirst12Bytes(passwordMD5Hash);
            int hopDistanceY = calculateXORLast4Bytes(passwordMD5Hash);

            log("Step 1 - Password MD5 Hash and XOR calculations:");
            log("  Password MD5 Hash: " + passwordMD5Hash);
            log("  X (First 12 bytes XOR): " + baseOffsetX);
            log("  Y (Last 4 bytes XOR / hop): " + hopDistanceY);

            // Step 2: Extract characters following the new position flow
            log("Step 2 - Character extraction with new position flow:");

            StringBuilder extractedText = new StringBuilder();
            int currentPosition = baseOffsetX;
            int charIndex = 0;

            boolean foundDelimiter = false;

            while (extractedText.length() < 100) { // Safety limit
                int ascii;
                int currentPixel;

                if (charIndex == 0) {
                    // Position 0: Extract '*' marker at X
                    currentPixel = currentPosition % (256 * 256);
                } else if (charIndex == 1) {
                    // Position 1: X + Y, extract first watermark char
                    currentPosition += hopDistanceY;
                    currentPixel = currentPosition % (256 * 256);
                } else if (!foundDelimiter) {
                    // Before delimiter: prev + ASCII of previous extracted char
                    int prevChar = (int) extractedText.charAt(charIndex - 1);
                    currentPosition += prevChar;
                    currentPixel = currentPosition % (256 * 256);
                } else {
                    // After '#' delimiter: hop by X for timestamp and end marker
                    currentPosition += baseOffsetX;
                    currentPixel = currentPosition % (256 * 256);
                }

                int x = currentPixel % 256;
                int y = currentPixel / 256;

                int rgb = imageToExtractFrom.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Extract LSBs
                String redBits = String.format("%3s", Integer.toBinaryString(r & 0x07)).replace(' ', '0');
                String greenBits = String.format("%3s", Integer.toBinaryString(g & 0x07)).replace(' ', '0');
                String blueBits = String.format("%2s", Integer.toBinaryString(b & 0x03)).replace(' ', '0');

                String binaryChar = redBits + greenBits + blueBits;
                ascii = Integer.parseInt(binaryChar, 2);
                char extractedChar = (char) ascii;

                log(String.format("  Pos %d: Pixel %d (x:%d, y:%d): RGB(%d,%d,%d) -> LSBs: %s -> ASCII: %d -> '%c'",
                        charIndex, currentPixel, x, y, r, g, b, binaryChar, ascii,
                        (ascii >= 32 && ascii <= 126) ? extractedChar : '?'));

                extractedText.append(extractedChar);

                // Check if we just extracted the '#' delimiter
                if (extractedChar == '#' && !foundDelimiter && charIndex > 1) {
                    foundDelimiter = true;
                    log("  -> Found delimiter '#', switching to X-hop positioning");
                }

                // Check for end marker
                if (extractedText.toString().endsWith("#@")) {
                    log("  -> Found END marker '#@'");
                    break;
                }

                charIndex++;

                // Safety check
                if (charIndex > 50) {
                    log("  -> Safety limit reached, stopping extraction");
                    break;
                }
            }

            // Step 3: Validate extracted watermark
            String fullExtracted = extractedText.toString();
            log("Step 3 - Full extracted string: \"" + fullExtracted + "\"");

            if (!fullExtracted.endsWith("#@")) {
                JOptionPane.showMessageDialog(this, "No valid watermark found with the given input!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
                log("ERROR: End marker '#@' not found!");
                return;
            }

            // Remove end marker
            String extractedContent = fullExtracted.substring(0, fullExtracted.length() - 2);

            // Remove the '*' marker from the beginning
            if (extractedContent.length() > 0 && extractedContent.charAt(0) == '*') {
                extractedContent = extractedContent.substring(1);
            }

            // Split using '#' delimiter
            String[] parts = extractedContent.split("#", 2);
            if (parts.length < 2) {
                log("ERROR: Delimiter '#' not found in extracted content!");
                JOptionPane.showMessageDialog(this, "Invalid watermark structure - delimiter not found!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String extractedWatermark = parts[0];
            String extractedTimestamp = parts[1];

            log("Step 4 - Extracted components:");
            log("  Watermark: \"" + extractedWatermark + "\"");
            log("  Timestamp: \"" + extractedTimestamp + "\"");
            log("=== EXTRACTION COMPLETED ===\n");

            String message = "Extraction Results:\n\n" +
                    "Extracted Watermark: \"" + extractedWatermark + "\"\n" +
                    "Extracted Timestamp: \"" + extractedTimestamp + "\"\n\n" +
                    "X (First 12 bytes XOR): " + baseOffsetX + "\n" +
                    "Y (Last 4 bytes XOR / hop): " + hopDistanceY;

            JOptionPane.showMessageDialog(this, message, "Extraction Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            statusLabel.setText("Status: Watermark extracted successfully!");

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
        fileChooser.setSelectedFile(new File("watermarked_image_dwm2.bmp"));

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DWM2().setVisible(true);
        });
    }
}
