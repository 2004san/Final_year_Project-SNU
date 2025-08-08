import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class BMPComparatorGUI extends JFrame {
    private BufferedImage imageNormal;
    private BufferedImage imageWatermarked;
    private JLabel resultLabel;

    public BMPComparatorGUI() {
        setTitle("BMP Pixel Comparator");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        JTextField rowField = new JTextField(5);
        JTextField colField = new JTextField(5);
        JButton checkButton = new JButton("Compare");

        inputPanel.add(new JLabel("Row (0–255):"));
        inputPanel.add(rowField);
        inputPanel.add(new JLabel("Col (0–255):"));
        inputPanel.add(colField);
        inputPanel.add(checkButton);

        add(inputPanel, BorderLayout.NORTH);

        // Result label
        resultLabel = new JLabel("Load two images to start comparison.", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(resultLabel, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadNormal = new JMenuItem("Load Normal BMP");
        JMenuItem loadWatermarked = new JMenuItem("Load Watermarked BMP");
        fileMenu.add(loadNormal);
        fileMenu.add(loadWatermarked);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Load image events
        loadNormal.addActionListener(e -> imageNormal = loadImage("normal"));
        loadWatermarked.addActionListener(e -> imageWatermarked = loadImage("watermarked"));

        // Compare button
        checkButton.addActionListener(e -> {
            if (imageNormal == null || imageWatermarked == null) {
                JOptionPane.showMessageDialog(this, "Please load both images first.");
                return;
            }

            try {
                int row = Integer.parseInt(rowField.getText());
                int col = Integer.parseInt(colField.getText());

                if (row < 0 || row >= imageNormal.getHeight() || col < 0 || col >= imageNormal.getWidth()) {
                    JOptionPane.showMessageDialog(this, "Row and column must be in range 0–255.");
                    return;
                }

                String normalRGB = getRGBBinary(imageNormal, row, col);
                String wmRGB = getRGBBinary(imageWatermarked, row, col);
                String diff = highlightDifference(normalRGB, wmRGB);

                resultLabel.setText("<html><center>" +
                        "Pixel (" + row + ", " + col + ") Comparison<br><br>" +
                        "<b>Normal Image:</b><br>" + normalRGB + "<br><br>" +
                        "<b>Watermarked Image:</b><br>" + wmRGB + "<br><br>" +
                        "<b>Difference:</b><br>" + diff +
                        "</center></html>");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid integers.");
            }
        });
    }

    private BufferedImage loadImage(String label) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Loaded " + label + " image.");
                return img;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load image.");
            }
        }
        return null;
    }

    private String getRGBBinary(BufferedImage image, int row, int col) {
        int rgb = image.getRGB(col, row);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        String rBin = String.format("%8s", Integer.toBinaryString(r)).replace(' ', '0');
        String gBin = String.format("%8s", Integer.toBinaryString(g)).replace(' ', '0');
        String bBin = String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0');

        return "R: " + rBin + "<br>G: " + gBin + "<br>B: " + bBin;
    }

    private String highlightDifference(String normal, String wm) {
        String[] nLines = normal.split("<br>");
        String[] wLines = wm.split("<br>");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            String nVal = nLines[i].substring(3); // skip "R: ", etc.
            String wVal = wLines[i].substring(3);

            sb.append(nLines[i].charAt(0)).append(": ");
            for (int j = 0; j < 8; j++) {
                if (nVal.charAt(j) != wVal.charAt(j)) {
                    sb.append("<span style='color:red;'><b>").append(wVal.charAt(j)).append("</b></span>");
                } else {
                    sb.append(wVal.charAt(j));
                }
            }
            sb.append("<br>");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BMPComparatorGUI().setVisible(true));
    }
}
