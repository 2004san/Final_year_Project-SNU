import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class WatermarkGUI extends JFrame {

    private BufferedImage loadedImage;
    private JLabel imageLabel;
    private JTextField watermarkField;
    private JPasswordField passwordField;

    public WatermarkGUI() {
        setTitle("Java Digital Watermarking (BMP Only)");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        imageLabel = new JLabel("No Image Loaded", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(256, 256));
        add(imageLabel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(6, 1, 5, 5));

        JButton loadButton = new JButton("Load BMP (256x256)");
        loadButton.addActionListener(e -> loadImage());
        controlPanel.add(loadButton);

        watermarkField = new JTextField();
        watermarkField.setToolTipText("Enter watermark (max 50 chars)");
        controlPanel.add(new JLabel("Watermark:"));
        controlPanel.add(watermarkField);

        passwordField = new JPasswordField();
        controlPanel.add(new JLabel("Password:"));
        controlPanel.add(passwordField);

        JPanel buttons = new JPanel(new FlowLayout());

        JButton embedButton = new JButton("Embed Watermark");
        embedButton.addActionListener(e -> embedWatermark());
        buttons.add(embedButton);

        JButton extractButton = new JButton("Fetch Watermark");
        extractButton.addActionListener(e -> extractWatermark());
        buttons.add(extractButton);

        controlPanel.add(buttons);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select 256x256 BMP Image");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(selected);
                if (img.getWidth() != 256 || img.getHeight() != 256) {
                    JOptionPane.showMessageDialog(this, "Image must be 256x256 pixels.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                loadedImage = img;
                imageLabel.setIcon(new ImageIcon(loadedImage));
                imageLabel.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void embedWatermark() {
        if (loadedImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String watermark = watermarkField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (watermark.length() == 0 || watermark.length() > 50) {
            JOptionPane.showMessageDialog(this, "Watermark must be 1–50 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() == 0) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BufferedImage result = StegoUtils.embedWatermark(loadedImage, watermark, password);

            JFileChooser saver = new JFileChooser();
            saver.setDialogTitle("Save Watermarked Image (BMP)");
            int resultChoice = saver.showSaveDialog(this);
            if (resultChoice == JFileChooser.APPROVE_OPTION) {
                File saveFile = saver.getSelectedFile();
                if (!saveFile.getName().toLowerCase().endsWith(".bmp")) {
                    saveFile = new File(saveFile.getAbsolutePath() + ".bmp");
                }

                boolean success = ImageIO.write(result, "bmp", saveFile);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Watermark embedded and saved as BMP.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save BMP image (writer missing).", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error embedding watermark.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extractWatermark() {
        if (loadedImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String password = new String(passwordField.getPassword()).trim();
        if (password.length() == 0) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String extracted = StegoUtils.extractWatermark(loadedImage, password);
            JOptionPane.showMessageDialog(this, "Extracted Watermark:\n" + extracted, "Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error extracting watermark.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WatermarkGUI().setVisible(true));
    }
}
