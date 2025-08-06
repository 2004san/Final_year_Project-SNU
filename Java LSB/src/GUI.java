import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GUI {
    JFrame frame;
    JTextField textField;
    JButton chooseBtn, encodeBtn;
    JLabel statusLabel;
    BufferedImage selectedImage;

    public GUI() {
        frame = new JFrame("LSB Watermarker");
        frame.setSize(500, 300);
        frame.setLayout(new FlowLayout());

        chooseBtn = new JButton("Choose BMP Image");
        encodeBtn = new JButton("Encode Watermark");
        textField = new JTextField(30);
        statusLabel = new JLabel("");

        chooseBtn.addActionListener(e -> chooseImage());
        encodeBtn.addActionListener(e -> encodeWatermark());

        frame.add(chooseBtn);
        frame.add(textField);
        frame.add(encodeBtn);
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

        BufferedImage encoded = LSBEncoder.encodeText(selectedImage, text);

        try {
            File output = new File("watermarked.bmp");
            ImageIO.write(encoded, "bmp", output);
            statusLabel.setText("Watermark embedded! Saved as watermarked.bmp");
        } catch (Exception ex) {
            statusLabel.setText("Failed to save image.");
        }
    }
}
