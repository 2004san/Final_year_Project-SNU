import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GUI {
    JFrame frame;
    JTextField textField;
    JButton chooseBtn, encodeBtn, decodeBtn;
    JLabel statusLabel;
    BufferedImage selectedImage;

    public GUI() {
        frame = new JFrame("Secure LSB Watermarking Tool");
        frame.setSize(600, 200);
        frame.setLayout(new FlowLayout());

        chooseBtn = new JButton("Choose BMP Image");
        encodeBtn = new JButton("Encode Watermark");
        decodeBtn = new JButton("Decode Watermark");
        textField = new JTextField(30);
        statusLabel = new JLabel("");

        chooseBtn.addActionListener(e -> chooseImage());
        encodeBtn.addActionListener(e -> encodeWatermark());
        decodeBtn.addActionListener(e -> decodeWatermark());

        frame.add(chooseBtn);
        frame.add(textField);
        frame.add(encodeBtn);
        frame.add(decodeBtn);
        frame.add(statusLabel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                selectedImage = ImageIO.read(file);
                statusLabel.setText("Image loaded: " + file.getName());
            } catch (Exception ex) {
                statusLabel.setText("Failed to load image.");
            }
        }
    }

    private void encodeWatermark() {
        if (selectedImage == null) {
            statusLabel.setText("Please load an image first.");
            return;
        }

        String text = textField.getText();
        if (text.isEmpty()) {
            statusLabel.setText("Please enter watermark text.");
            return;
        }

        String password = JOptionPane.showInputDialog(frame, "Enter password to embed watermark:");
        if (password == null || password.isEmpty()) {
            statusLabel.setText("Password required.");
            return;
        }

        try {
            BufferedImage encodedImage = SecureLSBEncoder.encode(selectedImage, text, password);
            File output = new File("watermarked.bmp");
            ImageIO.write(encodedImage, "bmp", output);
            statusLabel.setText("Watermark embedded securely and saved as watermarked.bmp");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void decodeWatermark() {
        if (selectedImage == null) {
            statusLabel.setText("Please load the watermarked image first.");
            return;
        }

        String lengthStr = JOptionPane.showInputDialog(frame, "Enter length of watermark text:");
        String password = JOptionPane.showInputDialog(frame, "Enter password used during encoding:");

        if (lengthStr == null || password == null || lengthStr.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Operation cancelled.");
            return;
        }

        try {
            int length = Integer.parseInt(lengthStr);
            String message = SecureLSBDecoder.decode(selectedImage, length, password);
            JOptionPane.showMessageDialog(frame, "Decoded Watermark: " + message);
        } catch (Exception ex) {
            statusLabel.setText("Error decoding: " + ex.getMessage());
        }
    }
}
