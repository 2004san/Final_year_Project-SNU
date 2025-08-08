import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class ImageComparisonGUI extends JFrame {
    private JLabel originalImageLabel, watermarkedImageLabel, differenceImageLabel;
    private JTextArea analysisArea;
    private JTable pixelTable;
    private DefaultTableModel tableModel;
    private JTextField coordinatesField;
    private JButton loadOriginalButton, loadWatermarkedButton, compareButton, exportButton;
    private JLabel statsLabel, mousePositionLabel;
    private JSlider magnificationSlider;
    private JSpinner thresholdSpinner;

    private BufferedImage originalImage, watermarkedImage, differenceImage;
    private int currentMouseX = -1, currentMouseY = -1;
    private List<PixelDifference> differences = new ArrayList<>();

    // Class to store pixel difference information
    private static class PixelDifference {
        int x, y;
        Color originalColor, watermarkedColor;
        int redDiff, greenDiff, blueDiff, totalDiff;

        PixelDifference(int x, int y, Color orig, Color water) {
            this.x = x;
            this.y = y;
            this.originalColor = orig;
            this.watermarkedColor = water;
            this.redDiff = Math.abs(water.getRed() - orig.getRed());
            this.greenDiff = Math.abs(water.getGreen() - orig.getGreen());
            this.blueDiff = Math.abs(water.getBlue() - orig.getBlue());
            this.totalDiff = redDiff + greenDiff + blueDiff;
        }
    }

    public ImageComparisonGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Pixel-by-Pixel Image Comparison Tool - Watermarking Analysis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        add(createControlPanel(), BorderLayout.NORTH);
        add(createImagePanel(), BorderLayout.CENTER);
        add(createAnalysisPanel(), BorderLayout.EAST);
        add(createStatusPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Image Loading & Controls"));

        loadOriginalButton = new JButton("Load Original Image");
        loadOriginalButton.addActionListener(this::loadOriginalImage);

        loadWatermarkedButton = new JButton("Load Watermarked Image");
        loadWatermarkedButton.addActionListener(this::loadWatermarkedImage);

        compareButton = new JButton("Compare Images");
        compareButton.addActionListener(this::compareImages);
        compareButton.setEnabled(false);

        exportButton = new JButton("Export Analysis Report");
        exportButton.addActionListener(this::exportAnalysis);
        exportButton.setEnabled(false);

        // Magnification control
        panel.add(new JLabel("Magnification:"));
        magnificationSlider = new JSlider(100, 400, 100);
        magnificationSlider.setMajorTickSpacing(100);
        magnificationSlider.setPaintTicks(true);
        magnificationSlider.setPaintLabels(true);
        magnificationSlider.addChangeListener(e -> updateImageDisplay());

        // Difference threshold
        panel.add(new JLabel("Diff Threshold:"));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        thresholdSpinner.addChangeListener(e -> updateDifferenceHighlight());

        panel.add(loadOriginalButton);
        panel.add(loadWatermarkedButton);
        panel.add(compareButton);
        panel.add(exportButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(magnificationSlider);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(thresholdSpinner);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Images display panel
        JPanel imagesPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Original Image Panel
        JPanel originalPanel = new JPanel(new BorderLayout());
        originalPanel.setBorder(new TitledBorder("Original Image"));
        originalImageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        originalImageLabel.setPreferredSize(new Dimension(300, 300));
        originalImageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        addMouseListeners(originalImageLabel);
        originalPanel.add(new JScrollPane(originalImageLabel), BorderLayout.CENTER);

        // Watermarked Image Panel
        JPanel watermarkedPanel = new JPanel(new BorderLayout());
        watermarkedPanel.setBorder(new TitledBorder("Watermarked Image"));
        watermarkedImageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        watermarkedImageLabel.setPreferredSize(new Dimension(300, 300));
        watermarkedImageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        addMouseListeners(watermarkedImageLabel);
        watermarkedPanel.add(new JScrollPane(watermarkedImageLabel), BorderLayout.CENTER);

        // Difference Image Panel
        JPanel differencePanel = new JPanel(new BorderLayout());
        differencePanel.setBorder(new TitledBorder("Difference Map (Enhanced)"));
        differenceImageLabel = new JLabel("No comparison done", SwingConstants.CENTER);
        differenceImageLabel.setPreferredSize(new Dimension(300, 300));
        differenceImageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        addMouseListeners(differenceImageLabel);
        differencePanel.add(new JScrollPane(differenceImageLabel), BorderLayout.CENTER);

        imagesPanel.add(originalPanel);
        imagesPanel.add(watermarkedPanel);
        imagesPanel.add(differencePanel);

        mainPanel.add(imagesPanel, BorderLayout.CENTER);

        // Pixel coordinates input
        JPanel coordPanel = new JPanel(new FlowLayout());
        coordPanel.add(new JLabel("Go to Pixel (x,y):"));
        coordinatesField = new JTextField(10);
        coordinatesField.setToolTipText("Enter coordinates as: x,y (e.g., 100,50)");
        JButton goButton = new JButton("Go");
        goButton.addActionListener(this::goToPixel);
        coordPanel.add(coordinatesField);
        coordPanel.add(goButton);

        mainPanel.add(coordPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(950, 700)); // Much larger panel
        panel.setBorder(new TitledBorder("Pixel-by-Pixel Analysis"));

        // Pixel details table (now the main component)
        String[] columnNames = {"Property", "Original", "Watermarked", "Difference"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pixelTable = new JTable(tableModel);
        pixelTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13)); // Slightly larger font
        pixelTable.setRowHeight(35); // Much taller rows
        pixelTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allow horizontal scrolling

        // Make table columns much wider for complete visibility
        pixelTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        pixelTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        pixelTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        pixelTable.getColumnModel().getColumn(3).setPreferredWidth(180);

        // Custom renderer for difference highlighting
        pixelTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (column == 3 && value != null && !value.toString().equals("0") &&
                        !value.toString().equals("N/A") && !value.toString().equals("Same") &&
                        !value.toString().equals("^ = Changed")) {
                    c.setBackground(new Color(255, 200, 200)); // Light red for differences
                } else if (row > 0 && table.getValueAt(row, 0).toString().contains("Embedded Data")) {
                    c.setBackground(new Color(200, 255, 200)); // Light green for embedded data
                } else if (row > 0 && table.getValueAt(row, 0).toString().contains("Bit Diff Pattern")) {
                    c.setBackground(new Color(200, 200, 255)); // Light blue for bit pattern
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(pixelTable);
        tableScroll.setBorder(new TitledBorder("Current Pixel Binary Analysis"));

        // Instructions panel - made more compact
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBorder(new TitledBorder("Instructions"));

        JTextArea instructions = new JTextArea(3, 40); // Reduced height
        instructions.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        instructions.setEditable(false);
        instructions.setBackground(getBackground());
        instructions.setText(
                "• Click pixels in images for binary analysis  • Red=Changes  • Green=Embedded data  • Blue=Bit patterns"
        );
        instructionsPanel.add(instructions, BorderLayout.CENTER);

        // Summary stats panel - made more compact
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(new TitledBorder("Summary"));

        analysisArea = new JTextArea(5, 40); // Reduced height
        analysisArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10)); // Smaller font
        analysisArea.setEditable(false);
        JScrollPane summaryScroll = new JScrollPane(analysisArea);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        // Layout: Instructions at top, main table in center, summary at bottom
        panel.add(instructionsPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        statsLabel = new JLabel("Ready - Load images to compare");
        mousePositionLabel = new JLabel("Mouse: (-, -)");

        panel.add(statsLabel, BorderLayout.CENTER);
        panel.add(mousePositionLabel, BorderLayout.EAST);

        return panel;
    }

    private void addMouseListeners(JLabel label) {
        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updatePixelInfo(e.getX(), e.getY());
            }

            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        };

        MouseMotionListener motionListener = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateMousePosition(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateMousePosition(e.getX(), e.getY());
            }
        };

        label.addMouseListener(mouseListener);
        label.addMouseMotionListener(motionListener);
    }

    private void loadOriginalImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files", "bmp", "png", "jpg", "jpeg"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(fileChooser.getSelectedFile());
                updateImageDisplay();
                checkCompareEnabled();
                analysisArea.setText("Original image loaded: " + fileChooser.getSelectedFile().getName() +
                        "\nDimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight() + "\n\n");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading original image: " + ex.getMessage());
            }
        }
    }

    private void loadWatermarkedImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files", "bmp", "png", "jpg", "jpeg"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                watermarkedImage = ImageIO.read(fileChooser.getSelectedFile());
                updateImageDisplay();
                checkCompareEnabled();
                analysisArea.append("Watermarked image loaded: " + fileChooser.getSelectedFile().getName() +
                        "\nDimensions: " + watermarkedImage.getWidth() + "x" + watermarkedImage.getHeight() + "\n\n");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading watermarked image: " + ex.getMessage());
            }
        }
    }

    private void checkCompareEnabled() {
        boolean canCompare = originalImage != null && watermarkedImage != null;
        compareButton.setEnabled(canCompare);

        if (canCompare && (originalImage.getWidth() != watermarkedImage.getWidth() ||
                originalImage.getHeight() != watermarkedImage.getHeight())) {
            JOptionPane.showMessageDialog(this,
                    "Warning: Images have different dimensions!\n" +
                            "Original: " + originalImage.getWidth() + "x" + originalImage.getHeight() + "\n" +
                            "Watermarked: " + watermarkedImage.getWidth() + "x" + watermarkedImage.getHeight(),
                    "Dimension Mismatch", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void compareImages(ActionEvent e) {
        if (originalImage == null || watermarkedImage == null) {
            return;
        }

        int width = Math.min(originalImage.getWidth(), watermarkedImage.getWidth());
        int height = Math.min(originalImage.getHeight(), watermarkedImage.getHeight());

        // Create difference image
        differenceImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        differences.clear();

        int totalPixels = width * height;
        int changedPixels = 0;
        int maxDifference = 0;
        long totalDifference = 0;

        analysisArea.setText("=== PIXEL-BY-PIXEL COMPARISON ANALYSIS ===\n\n");
        analysisArea.append("Image Dimensions: " + width + "x" + height + " (" + totalPixels + " total pixels)\n\n");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color originalColor = new Color(originalImage.getRGB(x, y));
                Color watermarkedColor = new Color(watermarkedImage.getRGB(x, y));

                int redDiff = Math.abs(watermarkedColor.getRed() - originalColor.getRed());
                int greenDiff = Math.abs(watermarkedColor.getGreen() - originalColor.getGreen());
                int blueDiff = Math.abs(watermarkedColor.getBlue() - originalColor.getBlue());
                int totalDiff = redDiff + greenDiff + blueDiff;

                if (totalDiff > 0) {
                    changedPixels++;
                    differences.add(new PixelDifference(x, y, originalColor, watermarkedColor));
                    maxDifference = Math.max(maxDifference, totalDiff);
                    totalDifference += totalDiff;
                }

                // Create enhanced difference visualization
                int enhancedDiff = Math.min(255, totalDiff * 10); // Amplify differences
                Color diffColor = totalDiff == 0 ? Color.BLACK :
                        new Color(enhancedDiff, enhancedDiff/2, enhancedDiff/2);
                differenceImage.setRGB(x, y, diffColor.getRGB());
            }
        }

        updateImageDisplay();

        // Calculate statistics
        double changePercentage = (changedPixels * 100.0) / totalPixels;
        double avgDifference = changedPixels > 0 ? (double)totalDifference / changedPixels : 0;

        // LSB Analysis
        int lsbChanges = 0;
        for (PixelDifference diff : differences) {
            if (diff.redDiff <= 7 && diff.greenDiff <= 7 && diff.blueDiff <= 3) {
                lsbChanges++;
            }
        }

        // Analysis report - now condensed for summary
        analysisArea.setText("COMPARISON SUMMARY:\n");
        analysisArea.append("Changed: " + changedPixels + "/" + totalPixels +
                " (" + String.format("%.4f", changePercentage) + "%)\n");
        analysisArea.append("Max Diff: " + maxDifference + " | Avg Diff: " + String.format("%.2f", avgDifference) + "\n");
        analysisArea.append("LSB Changes: " + lsbChanges +
                " (" + String.format("%.1f", (lsbChanges * 100.0) / Math.max(1, changedPixels)) + "% of changes)\n");
        analysisArea.append("\nClick on pixels in the images above to see detailed binary analysis.\n");

        exportButton.setEnabled(true);
        statsLabel.setText(String.format("Comparison Complete - %d pixels changed (%.4f%%) - Avg diff: %.2f",
                changedPixels, changePercentage, avgDifference));

        updateDifferenceHighlight();
    }

    private void updateImageDisplay() {
        double scale = magnificationSlider.getValue() / 100.0;

        if (originalImage != null) {
            int scaledWidth = (int)(originalImage.getWidth() * scale);
            int scaledHeight = (int)(originalImage.getHeight() * scale);
            Image scaled = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_FAST);
            originalImageLabel.setIcon(new ImageIcon(scaled));
            originalImageLabel.setText("");
        }

        if (watermarkedImage != null) {
            int scaledWidth = (int)(watermarkedImage.getWidth() * scale);
            int scaledHeight = (int)(watermarkedImage.getHeight() * scale);
            Image scaled = watermarkedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_FAST);
            watermarkedImageLabel.setIcon(new ImageIcon(scaled));
            watermarkedImageLabel.setText("");
        }

        if (differenceImage != null) {
            int scaledWidth = (int)(differenceImage.getWidth() * scale);
            int scaledHeight = (int)(differenceImage.getHeight() * scale);
            Image scaled = differenceImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_FAST);
            differenceImageLabel.setIcon(new ImageIcon(scaled));
            differenceImageLabel.setText("");
        }
    }

    private void updateDifferenceHighlight() {
        if (differenceImage == null || differences.isEmpty()) return;

        int threshold = (Integer)thresholdSpinner.getValue();

        // Recreate difference image with threshold
        BufferedImage newDiffImage = new BufferedImage(
                differenceImage.getWidth(), differenceImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        // First, fill with black (no difference)
        Graphics2D g2d = newDiffImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, newDiffImage.getWidth(), newDiffImage.getHeight());
        g2d.dispose();

        // Highlight pixels above threshold
        for (PixelDifference diff : differences) {
            if (diff.totalDiff >= threshold) {
                int enhancedDiff = Math.min(255, diff.totalDiff * 10);
                Color diffColor = new Color(enhancedDiff, enhancedDiff/2, enhancedDiff/2);
                newDiffImage.setRGB(diff.x, diff.y, diffColor.getRGB());
            }
        }

        differenceImage = newDiffImage;
        updateImageDisplay();

        long filteredCount = differences.stream().filter(d -> d.totalDiff >= threshold).count();
        statsLabel.setText(statsLabel.getText() + " | Showing " + filteredCount + " pixels above threshold " + threshold);
    }

    private void updateMousePosition(int mouseX, int mouseY) {
        if (originalImage == null) return;

        double scale = magnificationSlider.getValue() / 100.0;
        int imageX = (int)(mouseX / scale);
        int imageY = (int)(mouseY / scale);

        if (imageX >= 0 && imageX < originalImage.getWidth() &&
                imageY >= 0 && imageY < originalImage.getHeight()) {
            currentMouseX = imageX;
            currentMouseY = imageY;
            mousePositionLabel.setText(String.format("Mouse: (%d, %d)", imageX, imageY));
        }
    }

    private void updatePixelInfo(int mouseX, int mouseY) {
        if (originalImage == null || watermarkedImage == null) return;

        double scale = magnificationSlider.getValue() / 100.0;
        int imageX = (int)(mouseX / scale);
        int imageY = (int)(mouseY / scale);

        if (imageX >= 0 && imageX < originalImage.getWidth() &&
                imageY >= 0 && imageY < originalImage.getHeight()) {

            Color originalColor = new Color(originalImage.getRGB(imageX, imageY));
            Color watermarkedColor = new Color(watermarkedImage.getRGB(imageX, imageY));

            // Update table
            tableModel.setRowCount(0);

            tableModel.addRow(new Object[]{"Coordinates", "(" + imageX + ", " + imageY + ")", "(" + imageX + ", " + imageY + ")", "N/A"});
            tableModel.addRow(new Object[]{"Red", originalColor.getRed(), watermarkedColor.getRed(),
                    Math.abs(watermarkedColor.getRed() - originalColor.getRed())});
            tableModel.addRow(new Object[]{"Green", originalColor.getGreen(), watermarkedColor.getGreen(),
                    Math.abs(watermarkedColor.getGreen() - originalColor.getGreen())});
            tableModel.addRow(new Object[]{"Blue", originalColor.getBlue(), watermarkedColor.getBlue(),
                    Math.abs(watermarkedColor.getBlue() - originalColor.getBlue())});

            int totalDiff = Math.abs(watermarkedColor.getRed() - originalColor.getRed()) +
                    Math.abs(watermarkedColor.getGreen() - originalColor.getGreen()) +
                    Math.abs(watermarkedColor.getBlue() - originalColor.getBlue());
            tableModel.addRow(new Object[]{"Total Diff", "N/A", "N/A", totalDiff});

            // Binary representation with highlighting
            String origRedBin = String.format("%8s", Integer.toBinaryString(originalColor.getRed())).replace(' ', '0');
            String origGreenBin = String.format("%8s", Integer.toBinaryString(originalColor.getGreen())).replace(' ', '0');
            String origBlueBin = String.format("%8s", Integer.toBinaryString(originalColor.getBlue())).replace(' ', '0');

            String waterRedBin = String.format("%8s", Integer.toBinaryString(watermarkedColor.getRed())).replace(' ', '0');
            String waterGreenBin = String.format("%8s", Integer.toBinaryString(watermarkedColor.getGreen())).replace(' ', '0');
            String waterBlueBin = String.format("%8s", Integer.toBinaryString(watermarkedColor.getBlue())).replace(' ', '0');

            String origBinary = origRedBin + " " + origGreenBin + " " + origBlueBin;
            String waterBinary = waterRedBin + " " + waterGreenBin + " " + waterBlueBin;

            // Create difference indicator showing which bits changed
            StringBuilder diffIndicator = new StringBuilder();
            String[] origParts = {origRedBin, origGreenBin, origBlueBin};
            String[] waterParts = {waterRedBin, waterGreenBin, waterBlueBin};

            for (int part = 0; part < 3; part++) {
                if (part > 0) diffIndicator.append(" ");
                for (int bit = 0; bit < 8; bit++) {
                    if (origParts[part].charAt(bit) != waterParts[part].charAt(bit)) {
                        diffIndicator.append("^");
                    } else {
                        diffIndicator.append("-");
                    }
                }
            }

            tableModel.addRow(new Object[]{"Binary (R G B)", origBinary, waterBinary, "Bit Changes:"});
            tableModel.addRow(new Object[]{"Bit Diff Pattern", "--------", diffIndicator.toString(), "^ = Changed"});

            // Enhanced LSB analysis with binary display
            int redLSB = originalColor.getRed() & 0x07; // Last 3 bits
            int greenLSB = originalColor.getGreen() & 0x07; // Last 3 bits
            int blueLSB = originalColor.getBlue() & 0x03; // Last 2 bits

            int redLSBWater = watermarkedColor.getRed() & 0x07;
            int greenLSBWater = watermarkedColor.getGreen() & 0x07;
            int blueLSBWater = watermarkedColor.getBlue() & 0x03;

            String origLSB = String.format("%3s %3s %2s",
                            Integer.toBinaryString(redLSB), Integer.toBinaryString(greenLSB), Integer.toBinaryString(blueLSB))
                    .replace(' ', '0');
            String waterLSB = String.format("%3s %3s %2s",
                            Integer.toBinaryString(redLSBWater), Integer.toBinaryString(greenLSBWater), Integer.toBinaryString(blueLSBWater))
                    .replace(' ', '0');

            boolean lsbChanged = redLSB != redLSBWater || greenLSB != greenLSBWater || blueLSB != blueLSBWater;

            tableModel.addRow(new Object[]{"LSB Binary (3-3-2)", origLSB, waterLSB, lsbChanged ? "CHANGED" : "Same"});
            tableModel.addRow(new Object[]{"LSB Decimal (3-3-2)",
                    String.format("%d %d %d", redLSB, greenLSB, blueLSB),
                    String.format("%d %d %d", redLSBWater, greenLSBWater, blueLSBWater),
                    lsbChanged ? "CHANGED" : "Same"});

            // Show the embedded character if LSB changed
            if (lsbChanged) {
                // Reconstruct the 8-bit character from LSBs
                String embeddedBinary = String.format("%3s", Integer.toBinaryString(redLSBWater)).replace(' ', '0') +
                        String.format("%3s", Integer.toBinaryString(greenLSBWater)).replace(' ', '0') +
                        String.format("%2s", Integer.toBinaryString(blueLSBWater)).replace(' ', '0');
                int embeddedAscii = Integer.parseInt(embeddedBinary, 2);
                char embeddedChar = (char) embeddedAscii;

                tableModel.addRow(new Object[]{"Embedded Data", "N/A",
                        embeddedBinary + " (ASCII:" + embeddedAscii + ")",
                        embeddedAscii >= 32 && embeddedAscii <= 126 ? "'" + embeddedChar + "'" : "Non-printable"});
            }
        }
    }

    private void goToPixel(ActionEvent e) {
        String coords = coordinatesField.getText().trim();
        try {
            String[] parts = coords.split(",");
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());

            if (originalImage != null && x >= 0 && x < originalImage.getWidth() &&
                    y >= 0 && y < originalImage.getHeight()) {
                updatePixelInfo((int)(x * magnificationSlider.getValue() / 100.0),
                        (int)(y * magnificationSlider.getValue() / 100.0));
                currentMouseX = x;
                currentMouseY = y;
                mousePositionLabel.setText(String.format("Mouse: (%d, %d)", x, y));
            } else {
                JOptionPane.showMessageDialog(this, "Invalid coordinates or no image loaded!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid coordinate format! Use: x,y (e.g., 100,50)");
        }
    }

    private void exportAnalysis(ActionEvent e) {
        if (differences.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No comparison data to export!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("watermark_analysis_report.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write("=== WATERMARKING ANALYSIS REPORT ===\n");
                writer.write("Generated: " + new java.util.Date() + "\n\n");
                writer.write(analysisArea.getText());
                writer.write("\n\n=== ALL CHANGED PIXELS ===\n");

                for (PixelDifference diff : differences) {
                    writer.write(String.format("Pixel (%d,%d): RGB(%d,%d,%d) -> RGB(%d,%d,%d) [Diff: R:%d G:%d B:%d Total:%d]\n",
                            diff.x, diff.y,
                            diff.originalColor.getRed(), diff.originalColor.getGreen(), diff.originalColor.getBlue(),
                            diff.watermarkedColor.getRed(), diff.watermarkedColor.getGreen(), diff.watermarkedColor.getBlue(),
                            diff.redDiff, diff.greenDiff, diff.blueDiff, diff.totalDiff));
                }

                writer.close();
                JOptionPane.showMessageDialog(this, "Analysis report exported successfully!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageComparisonGUI().setVisible(true);
        });
    }
}