import javax.swing.*;
import java.awt.*;
import java.io.File;

public class WatermarkGUI extends JFrame {
    private File selectedImage;
    private JTextField passwordField;
    private JTextField watermarkField;
    private JTextArea outputArea;

    public WatermarkGUI() {
        setTitle("Watermark Tool");
        setSize(700, 450);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Components
        JButton chooseButton = new JButton("Choose BMP Image");
        passwordField = new JTextField(20);
        watermarkField = new JTextField(50);
        JButton embedButton = new JButton("Embed Watermark");
        JButton fetchButton = new JButton("Fetch Watermark");
        outputArea = new JTextArea(10, 50);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);

        // Top Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(3, 2, 10, 5));
        topPanel.add(chooseButton);
        topPanel.add(new JLabel("")); // spacer
        topPanel.add(new JLabel("Password:"));
        topPanel.add(passwordField);
        topPanel.add(new JLabel("Watermark (max 50 chars):"));
        topPanel.add(watermarkField);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(embedButton);
        bottomPanel.add(fetchButton);

        // Layout
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        chooseButton.addActionListener(e -> chooseImage());
        embedButton.addActionListener(e -> embedWatermark());
        fetchButton.addActionListener(e -> fetchWatermark());
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImage = chooser.getSelectedFile();
            outputArea.setText("✅ Selected: " + selectedImage.getAbsolutePath());
        }
    }

    private void embedWatermark() {
        try {
            if (!validateInputs(true))
                return;

            String password = passwordField.getText();
            String watermark = watermarkField.getText();
            String fullWatermark = "##@@##" + watermark + "##@@##";

            String hash = HashUtil.getSHA256(password);
            int crcDecimal = CRCUtil.computeCRCDecimal(hash);

            BMPUtil bmp = new BMPUtil(selectedImage.getAbsolutePath());
            boolean success = WatermarkEmbedder.embedWatermark(bmp, fullWatermark, crcDecimal);

            if (success) {
                String outputPath = selectedImage.getParent() + File.separator + "watermarked_output.bmp";
                bmp.saveImage(outputPath);
                outputArea.setText("✅ Watermark embedded successfully!\nSaved to:\n" + outputPath);
            } else {
                outputArea.setText("❌ Embedding failed: Pixel index out of bounds.");
            }
        } catch (Exception ex) {
            outputArea.setText("❌ Error: " + ex.getMessage());
        }
    }

    private void fetchWatermark() {
        try {
            if (!validateInputs(false))
                return;

            String password = passwordField.getText();
            String hash = HashUtil.getSHA256(password);
            int crcDecimal = CRCUtil.computeCRCDecimal(hash);

            BMPUtil bmp = new BMPUtil(selectedImage.getAbsolutePath());
            String extracted = WatermarkExtractor.extractWatermark(bmp, crcDecimal);

            if (!extracted.startsWith("##@@##") || !extracted.endsWith("##@@##")) {
                outputArea.setText("❌ Wrong password or no watermark found!");
            } else {
                String message = extracted.substring(6, extracted.length() - 6);
                outputArea.setText("✅ Watermark Found:\n" + message);
            }
        } catch (Exception ex) {
            outputArea.setText("❌ Error: " + ex.getMessage());
        }
    }

    private boolean validateInputs(boolean embedding) {
        if (selectedImage == null) {
            outputArea.setText("⚠️ Please choose a BMP image first.");
            return false;
        }
        if (passwordField.getText().isEmpty()) {
            outputArea.setText("⚠️ Password cannot be empty.");
            return false;
        }
        if (embedding && watermarkField.getText().length() > 50) {
            outputArea.setText("⚠️ Watermark must be 50 characters or less.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WatermarkGUI().setVisible(true);
        });
    }
}