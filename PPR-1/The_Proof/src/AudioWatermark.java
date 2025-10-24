import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AudioWatermark extends JFrame {
    private JTextField watermarkField;
    private JTextArea logArea;
    private JButton embedButton, extractButton, loadAudioButton, saveAudioButton, playButton;
    private JLabel statusLabel, timestampLabel, audioInfoLabel;
    private File currentAudioFile;
    private byte[] originalAudioData, watermarkedAudioData;
    private AudioInfo audioInfo;

    // Audio file format information
    private static class AudioInfo {
        int sampleRate;
        int channels;
        int bitsPerSample;
        int dataSize;
        int totalSamples;
        
        @Override
        public String toString() {
            return String.format("Sample Rate: %d Hz, Channels: %d, Bits: %d, Samples: %d", 
                               sampleRate, channels, bitsPerSample, totalSamples);
        }
    }

    public AudioWatermark() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Audio Watermarking - MD5 & Cumulative ASCII LSB Embedding");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        JPanel topPanel = createInputPanel();
        JPanel centerPanel = createAudioPanel();
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

        // Current timestamp display
        gbc.gridx = 0;
        gbc.gridy = 1;
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

    private JPanel createAudioPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Audio File Information"));

        audioInfoLabel = new JLabel("No audio file loaded", SwingConstants.CENTER);
        audioInfoLabel.setPreferredSize(new Dimension(400, 120));
        audioInfoLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        panel.add(audioInfoLabel, BorderLayout.CENTER);

        statusLabel = new JLabel("Status: Ready");
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Algorithm Log & MD5/ASCII Details"));

        logArea = new JTextArea(25, 45);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
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

        loadAudioButton = new JButton("Load WAV Audio");
        loadAudioButton.addActionListener(this::loadAudio);

        embedButton = new JButton("Embed Watermark");
        embedButton.addActionListener(this::embedWatermark);
        embedButton.setEnabled(false);

        extractButton = new JButton("Extract & Verify Watermark");
        extractButton.addActionListener(this::extractWatermark);
        extractButton.setEnabled(false);

        saveAudioButton = new JButton("Save Watermarked Audio");
        saveAudioButton.addActionListener(this::saveAudio);
        saveAudioButton.setEnabled(false);

        playButton = new JButton("Play Audio");
        playButton.addActionListener(this::playAudio);
        playButton.setEnabled(false);

        panel.add(loadAudioButton);
        panel.add(embedButton);
        panel.add(extractButton);
        panel.add(saveAudioButton);
        panel.add(playButton);

        return panel;
    }

    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy");
        return now.format(formatter);
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

    private void loadAudio(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV Audio Files", "wav"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentAudioFile = fileChooser.getSelectedFile();
                originalAudioData = loadWAVFile(currentAudioFile);
                
                if (originalAudioData == null) {
                    JOptionPane.showMessageDialog(this, "Invalid or unsupported WAV file!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Display audio info
                String infoText = "<html><center>" +
                    "<b>File:</b> " + currentAudioFile.getName() + "<br>" +
                    "<b>Size:</b> " + (originalAudioData.length / 1024) + " KB<br>" +
                    audioInfo.toString() + "<br>" +
                    "<b>Duration:</b> " + String.format("%.2f", (double)audioInfo.totalSamples / audioInfo.sampleRate) + " seconds" +
                    "</center></html>";
                audioInfoLabel.setText(infoText);

                embedButton.setEnabled(true);
                extractButton.setEnabled(true);
                playButton.setEnabled(true);
                statusLabel.setText("Status: Audio file loaded successfully");
                
                log("=== AUDIO FILE LOADED ===");
                log("File: " + currentAudioFile.getName());
                log("File size: " + originalAudioData.length + " bytes");
                log(audioInfo.toString());
                log("Duration: " + String.format("%.2f", (double)audioInfo.totalSamples / audioInfo.sampleRate) + " seconds");
                log("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading audio: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private byte[] loadWAVFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = fis.readAllBytes();
            
            // Parse WAV header
            if (data.length < 44 || !new String(data, 0, 4).equals("RIFF") || 
                !new String(data, 8, 4).equals("WAVE")) {
                return null;
            }

            // Extract audio format information
            audioInfo = new AudioInfo();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            buffer.position(22);
            audioInfo.channels = buffer.getShort();
            audioInfo.sampleRate = buffer.getInt();
            buffer.position(34);
            audioInfo.bitsPerSample = buffer.getShort();
            
            // Find data chunk
            buffer.position(36);
            if (!new String(data, 36, 4).equals("data")) {
                // Look for data chunk if not at standard position
                for (int i = 36; i < data.length - 4; i++) {
                    if (new String(data, i, 4).equals("data")) {
                        buffer.position(i + 4);
                        break;
                    }
                }
            } else {
                buffer.position(40);
            }
            
            audioInfo.dataSize = buffer.getInt();
            audioInfo.totalSamples = audioInfo.dataSize / (audioInfo.channels * audioInfo.bitsPerSample / 8);
            
            return data;
        }
    }

    private void embedWatermark(ActionEvent e) {
        String watermark = watermarkField.getText().trim();

        // Validation
        if (watermark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a watermark!");
            return;
        }
        if (watermark.length() > 16) {
            JOptionPane.showMessageDialog(this, "Watermark must be 16 characters or less!");
            return;
        }
        if (originalAudioData == null) {
            JOptionPane.showMessageDialog(this, "Please load an audio file first!");
            return;
        }

        try {
            log("=== AUDIO WATERMARK EMBEDDING PROCESS ===");
            log("Input watermark: \"" + watermark + "\"");

            // Step 1: Get current timestamp
            String timestamp = getCurrentTimestamp();
            log("Step 1 - Current timestamp: " + timestamp);

            // Step 2: Create full watermark string
            String fullWatermark = watermark + timestamp + "#@";
            log("Step 2 - Full watermark: \"" + fullWatermark + "\"");

            // Step 3: Calculate MD5 hash and XOR to get base offset
            String md5Hash = calculateMD5Hash(watermark);
            int baseOffset = calculateXORFromHash(md5Hash);
            log("Step 3 - MD5 Hash and XOR calculation:");
            log("  Input: \"" + watermark + "\"");
            log("  MD5 Hash: " + md5Hash);
            log("  XOR calculation:");
            
            // Show XOR process step by step
            int xorResult = Integer.parseInt(md5Hash.substring(0, 2), 16);
            StringBuilder xorLog = new StringBuilder("    " + md5Hash.substring(0, 2));
            for (int i = 2; i < md5Hash.length(); i += 2) {
                int nextByte = Integer.parseInt(md5Hash.substring(i, i + 2), 16);
                xorResult ^= nextByte;
                xorLog.append(" XOR ").append(md5Hash.substring(i, i + 2)).append(" = ").append(String.format("%02X", xorResult));
                if ((i / 2) % 4 == 3) { // Line break every 4 operations
                    log(xorLog.toString());
                    xorLog = new StringBuilder("    ");
                }
            }
            log(xorLog.toString());
            log("  Final XOR result (base offset): " + baseOffset + " (0x" + String.format("%02X", baseOffset) + ")");

            // Step 4: Create watermarked audio data
            watermarkedAudioData = originalAudioData.clone();

            // Step 5: Find audio data start position
            int dataStartIndex = findDataChunkStart(originalAudioData);
            if (dataStartIndex == -1) {
                throw new RuntimeException("Could not find audio data chunk");
            }

            // Step 6: Embed characters using cumulative ASCII positioning
            log("Step 4 - Cumulative ASCII-based sample positioning:");
            char[] chars = fullWatermark.toCharArray();
            int cumulativePosition = baseOffset;
            int lastHopDistance = 0;
            int bytesPerSample = audioInfo.bitsPerSample / 8;
            
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                int ascii = (int) c;
                
                // Calculate current sample position
                int currentSample;
                if (i < watermark.length()) {
                    // For watermark characters: cumulative ASCII + base offset
                    cumulativePosition += ascii;
                    currentSample = cumulativePosition % audioInfo.totalSamples;
                    if (i == watermark.length() - 1) {
                        // Calculate hop distance for timestamp/delimiter
                        lastHopDistance = ascii;
                    }
                } else {
                    // For timestamp and delimiter: use last character's ASCII as hop distance
                    cumulativePosition += lastHopDistance;
                    currentSample = cumulativePosition % audioInfo.totalSamples;
                }
                
                // Calculate byte position in audio data
                int sampleByteIndex = dataStartIndex + (currentSample * audioInfo.channels * bytesPerSample);
                
                // Ensure we don't go beyond audio data
                if (sampleByteIndex + bytesPerSample > watermarkedAudioData.length) {
                    currentSample = currentSample % (audioInfo.totalSamples / 2);
                    sampleByteIndex = dataStartIndex + (currentSample * audioInfo.channels * bytesPerSample);
                }

                // Embed character in LSB of audio sample
                if (audioInfo.bitsPerSample == 16) {
                    // 16-bit audio: modify LSB of the sample
                    // Clear LSB and set new bit from character
                    String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');
                    
                    // Use different bits for stereo channels
                    for (int channel = 0; channel < audioInfo.channels && channel < 2; channel++) {
                        int channelByteIndex = sampleByteIndex + (channel * 2);
                        int channelSample = ByteBuffer.wrap(watermarkedAudioData, channelByteIndex, 2)
                                                     .order(ByteOrder.LITTLE_ENDIAN).getShort();
                        
                        // Extract 4 bits for each channel from the 8-bit character
                        int bitsToEmbed = Integer.parseInt(binary.substring(channel * 4, (channel + 1) * 4), 2);
                        
                        // Clear last 4 bits and embed new ones
                        int newSample = (channelSample & 0xFFF0) | bitsToEmbed;
                        
                        ByteBuffer.wrap(watermarkedAudioData, channelByteIndex, 2)
                                  .order(ByteOrder.LITTLE_ENDIAN).putShort((short) newSample);
                    }
                    
                    log(String.format("  Char %d: '%c' (ASCII %d) -> Sample %d [Byte pos: %d]", 
                          i, c, ascii, currentSample, sampleByteIndex));
                    log(String.format("    Binary: %s -> Embedded in LSBs", binary));
                    
                    if (i < watermark.length()) {
                        log(String.format("    Cumulative position: %d + %d = %d", 
                              cumulativePosition - ascii, ascii, cumulativePosition));
                    } else {
                        log(String.format("    Using hop distance: +%d", lastHopDistance));
                    }
                } else {
                    log("  Warning: Only 16-bit audio supported for embedding");
                }
            }

            saveAudioButton.setEnabled(true);
            statusLabel.setText("Status: Watermark embedded successfully");
            log("=== EMBEDDING COMPLETED ===\n");

            JOptionPane.showMessageDialog(this, 
                "Watermark embedded successfully!\n" +
                "Characters embedded: " + chars.length + "\n" +
                "Base offset (MD5 XOR): " + baseOffset + "\n" +
                "Full watermark: \"" + fullWatermark + "\"", 
                "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error embedding watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void extractWatermark(ActionEvent e) {
        String expectedWatermark = watermarkField.getText().trim();

        if (expectedWatermark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the expected watermark!");
            return;
        }
        if (expectedWatermark.length() > 16) {
            JOptionPane.showMessageDialog(this, "Watermark must be 16 characters or less!");
            return;
        }
        if (originalAudioData == null) {
            JOptionPane.showMessageDialog(this, "Please load an audio file first!");
            return;
        }

        // Use watermarked audio if available, otherwise use original
        byte[] audioToExtractFrom = (watermarkedAudioData != null) ? watermarkedAudioData : originalAudioData;

        try {
            log("=== AUDIO WATERMARK EXTRACTION PROCESS ===");
            log("Expected watermark: \"" + expectedWatermark + "\"");

            // Step 1: Calculate MD5 hash and XOR to get base offset
            String md5Hash = calculateMD5Hash(expectedWatermark);
            int baseOffset = calculateXORFromHash(md5Hash);
            log("Step 1 - MD5 Hash and XOR verification:");
            log("  Input: \"" + expectedWatermark + "\"");
            log("  MD5 Hash: " + md5Hash);
            log("  Base offset: " + baseOffset);

            // Step 2: Find audio data start
            int dataStartIndex = findDataChunkStart(audioToExtractFrom);
            int bytesPerSample = audioInfo.bitsPerSample / 8;

            // Step 3: Extract characters following the same cumulative ASCII pattern
            log("Step 2 - Character extraction with cumulative ASCII positioning:");
            StringBuilder extractedText = new StringBuilder();
            int cumulativePosition = baseOffset;
            int lastHopDistance = 0;
            int charIndex = 0;

            while (extractedText.length() < 100) { // Safety limit
                int currentSample;
                
                if (charIndex < expectedWatermark.length()) {
                    // For expected watermark characters: calculate based on expected ASCII
                    char expectedChar = expectedWatermark.charAt(charIndex);
                    int ascii = (int) expectedChar;
                    cumulativePosition += ascii;
                    currentSample = cumulativePosition % audioInfo.totalSamples;
                    if (charIndex == expectedWatermark.length() - 1) {
                        lastHopDistance = ascii;
                    }
                } else {
                    // For timestamp and delimiter: use last hop distance
                    cumulativePosition += lastHopDistance;
                    currentSample = cumulativePosition % audioInfo.totalSamples;
                }
                
                // Calculate byte position in audio data
                int sampleByteIndex = dataStartIndex + (currentSample * audioInfo.channels * bytesPerSample);
                
                // Ensure we don't go beyond audio data
                if (sampleByteIndex + bytesPerSample > audioToExtractFrom.length) {
                    currentSample = currentSample % (audioInfo.totalSamples / 2);
                    sampleByteIndex = dataStartIndex + (currentSample * audioInfo.channels * bytesPerSample);
                }

                // Extract character from LSBs of audio sample
                int extractedAscii = 0;
                if (audioInfo.bitsPerSample == 16) {
                    StringBuilder binaryChar = new StringBuilder();
                    
                    // Extract 4 bits from each channel (up to 2 channels)
                    for (int channel = 0; channel < audioInfo.channels && channel < 2; channel++) {
                        int channelByteIndex = sampleByteIndex + (channel * 2);
                        if (channelByteIndex + 1 < audioToExtractFrom.length) {
                            int channelSample = ByteBuffer.wrap(audioToExtractFrom, channelByteIndex, 2)
                                                         .order(ByteOrder.LITTLE_ENDIAN).getShort();
                            
                            // Extract last 4 bits
                            int extractedBits = channelSample & 0x0F;
                            binaryChar.append(String.format("%4s", Integer.toBinaryString(extractedBits)).replace(' ', '0'));
                        }
                    }
                    
                    // Pad to 8 bits if mono
                    while (binaryChar.length() < 8) {
                        binaryChar.append("0000");
                    }
                    
                    String finalBinary = binaryChar.toString().substring(0, 8);
                    extractedAscii = Integer.parseInt(finalBinary, 2);
                }
                
                char extractedChar = (char) extractedAscii;

                log(String.format("  Sample %d [Byte %d]: ASCII %d -> '%c'",
                        currentSample, sampleByteIndex, extractedAscii,
                        (extractedAscii >= 32 && extractedAscii <= 126) ? extractedChar : '?'));

                extractedText.append(extractedChar);

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

            // Step 4: Validate extracted watermark
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
            
            // Split into watermark and timestamp
            if (extractedContent.length() >= expectedWatermark.length()) {
                String extractedWatermark = extractedContent.substring(0, expectedWatermark.length());
                String extractedTimestamp = extractedContent.substring(expectedWatermark.length());
                
                log("Step 4 - Extracted components:");
                log("  Watermark: \"" + extractedWatermark + "\"");
                log("  Timestamp: \"" + extractedTimestamp + "\"");
                
                boolean matches = extractedWatermark.equals(expectedWatermark);
                log("  Match result: " + (matches ? "SUCCESS" : "FAILED"));
                log("=== EXTRACTION COMPLETED ===\n");

                String message = "Extraction Results:\n\n" +
                        "Expected Watermark: \"" + expectedWatermark + "\"\n" +
                        "Extracted Watermark: \"" + extractedWatermark + "\"\n" +
                        "Extracted Timestamp: \"" + extractedTimestamp + "\"\n" +
                        "Match: " + (matches ? "YES ✓" : "NO ✗") + "\n\n" +
                        "Base Offset (MD5 XOR): " + baseOffset;

                JOptionPane.showMessageDialog(this, message,
                        matches ? "Extraction Successful" : "Extraction Failed",
                        matches ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                statusLabel.setText("Status: Watermark extracted - " + (matches ? "Match!" : "No match"));
            } else {
                log("ERROR: Extracted content too short!");
                JOptionPane.showMessageDialog(this, "Extracted content is too short!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error extracting watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private int findDataChunkStart(byte[] wavData) {
        // Look for "data" chunk marker
        for (int i = 0; i < wavData.length - 8; i++) {
            if (new String(wavData, i, 4).equals("data")) {
                // Return position after the 4-byte size field
                return i + 8;
            }
        }
        return -1;
    }

    private void saveAudio(ActionEvent e) {
        if (watermarkedAudioData == null) {
            JOptionPane.showMessageDialog(this, "No watermarked audio to save!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV Audio Files", "wav"));
        fileChooser.setSelectedFile(new File("watermarked_audio.wav"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".wav")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".wav");
                }

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(watermarkedAudioData);
                }

                statusLabel.setText("Status: Watermarked audio saved");
                log("Audio saved: " + outputFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Watermarked audio saved successfully!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving audio: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void playAudio(ActionEvent e) {
        // Simple audio playback notification
        if (currentAudioFile != null) {
            JOptionPane.showMessageDialog(this, 
                "Audio playback would be implemented here.\n" +
                "File: " + currentAudioFile.getName() + "\n" +
                "Use your system's default audio player to listen to the file.",
                "Audio Playback", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AudioWatermark().setVisible(true);
        });
    }
}