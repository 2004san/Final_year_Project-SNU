package com.snu.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.net.URI;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class GeminiLikeAppResizable extends JFrame {

    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // Define colors for the light theme
    private final Color SIDEBAR_DEFAULT_COLOR = new Color(245, 245, 245);
    private final Color SIDEBAR_HOVER_COLOR = new Color(230, 230, 230);
    private final Color MAIN_CONTENT_COLOR = Color.WHITE; // White for the middle screen
    // MODIFIED: Button colors now match sidebar colors
    private final Color BUTTON_DEFAULT_COLOR = SIDEBAR_DEFAULT_COLOR; // Light grey for tool buttons
    private final Color BUTTON_HOVER_COLOR = SIDEBAR_HOVER_COLOR;   // Slightly darker grey for tool button hover

    // Colors for neumorphic effect
    private final Color NEUMORPHIC_LIGHT_SHADOW = new Color(250, 250, 250, 150);
    private final Color NEUMORPHIC_DARK_SHADOW = new Color(0, 0, 0, 150);
    private final int NEUMORPHIC_SHADOW_OFFSET = 3;
    private final int NEUMORPHIC_CORNER_RADIUS = 15;

    // Animation duration and frame rate
    private final int ANIMATION_DURATION = 150;
    private final int FRAME_RATE = 30;

    public GeminiLikeAppResizable() {
        setTitle("Gemini-Like Application (Resizable Sidebar - Neumorphic Buttons)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        createSidebar();
        createMainContentArea();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainContentPanel);
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(splitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // --- Add SNU University Logo ---
        JLabel snuLogoLabel = new JLabel();
        try {
            BufferedImage originalImage = ImageIO.read(getClass().getResource("/icons/snu_logo.png"));

            if (originalImage != null) {
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();

                // Define a target height for the logo.
                // Since the sidebar width is 200, we want the logo to fit within that.
                // A height of around 90-100 pixels would make it "a little more big vertically"
                // while still leaving room for other elements.
                int targetHeight = 90; // Increased target height

                // Calculate the new width to maintain aspect ratio
                double aspectRatio = (double) originalWidth / originalHeight;
                int newWidth = (int) (targetHeight * aspectRatio);

                // Ensure the new width doesn't exceed the sidebar's typical width
                // For a 200px sidebar, give some padding, so max around 160-180px
                int maxWidth = 160; // Max width to ensure it fits comfortably with padding
                if (newWidth > maxWidth) {
                    newWidth = maxWidth;
                    targetHeight = (int) (newWidth / aspectRatio);
                }

                Image scaledImage = originalImage.getScaledInstance(newWidth, targetHeight, Image.SCALE_SMOOTH);
                ImageIcon snuLogoIcon = new ImageIcon(scaledImage);
                snuLogoLabel.setIcon(snuLogoIcon);
                snuLogoLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the logo horizontally
                snuLogoLabel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Add some vertical padding around the logo

                sidebarPanel.add(Box.createVerticalStrut(15)); // Space above the logo (reduced slightly for larger logo)
                sidebarPanel.add(snuLogoLabel);
                sidebarPanel.add(Box.createVerticalStrut(15)); // Space below the logo (reduced slightly)
            } else {
                System.err.println("Error: SNU logo image could not be loaded from /icons/snu_logo.png");
                sidebarPanel.add(Box.createVerticalStrut(100)); // Fallback space if logo fails
            }
        } catch (Exception e) {
            System.err.println("Exception loading SNU logo from resource: /icons/snu_logo.png - " +
                    "Please ensure the 'snu_logo.png' file exists in your src/main/resources/icons/ folder. Error: " + e.getMessage());
            sidebarPanel.add(Box.createVerticalStrut(100)); // Fallback space if logo fails
        }
        // --- End SNU University Logo ---

        JPanel navigationItemsPanel = new JPanel();
        navigationItemsPanel.setLayout(new BoxLayout(navigationItemsPanel, BoxLayout.Y_AXIS));
        navigationItemsPanel.setBackground(SIDEBAR_DEFAULT_COLOR);

        addSidebarItem(navigationItemsPanel, "Home", "/icons/home_icon.png");
        addSidebarItem(navigationItemsPanel, "Image", "/icons/image_icon.png");
        addSidebarItem(navigationItemsPanel, "Audio", "/icons/audio_icon.png");
        addSidebarItem(navigationItemsPanel, "Video", "/icons/video_icon.png");
        addSidebarItem(navigationItemsPanel, "Text", "/icons/text_icon.png");

        JScrollPane navigationScrollPane = new JScrollPane(navigationItemsPanel);
        navigationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        navigationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScrollPane.setBorder(BorderFactory.createEmptyBorder());
        navigationScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        sidebarPanel.add(navigationScrollPane);

        sidebarPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        addSidebarItem(bottomPanel, "Information", "/icons/info_icon.png");
        sidebarPanel.add(bottomPanel);
        sidebarPanel.add(Box.createVerticalStrut(10));
    }


    private void addSidebarItem(JPanel parentPanel, String text, String iconPath) {
        RoundedPanel itemPanel = new RoundedPanel(NEUMORPHIC_CORNER_RADIUS);
        itemPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        itemPanel.setOpaque(false);

        itemPanel.setLayout(new GridBagLayout());

        JPanel iconTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        iconTextPanel.setOpaque(false);

        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource(iconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                JLabel iconLabel = new JLabel(icon);
                iconTextPanel.add(iconLabel);
            } catch (Exception e) {
                System.err.println("Error loading icon from resource: " + iconPath +
                        " - Please ensure the 'icons' folder is located under src/main/resources/ in your project. Error: " + e.getMessage());
                iconTextPanel.add(Box.createHorizontalStrut(20));
            }
        } else {
            iconTextPanel.add(Box.createHorizontalStrut(20));
        }

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.BLACK);
        textLabel.setFont(new Font("Arial", Font.BOLD, 18));
        iconTextPanel.add(textLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        itemPanel.add(iconTextPanel, gbc);

        itemPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        final Color[] startColor = {SIDEBAR_DEFAULT_COLOR};
        final Color[] endColor = {SIDEBAR_HOVER_COLOR};
        final long[] startTime = {0};
        Timer timer = new Timer(1000 / FRAME_RATE, null);

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime[0];

                if (elapsed >= ANIMATION_DURATION) {
                    itemPanel.setBackground(endColor[0]);
                    ((Timer) e.getSource()).stop();
                } else {
                    float fraction = (float) elapsed / ANIMATION_DURATION;
                    Color interpolatedColor = interpolateColor(startColor[0], endColor[0], fraction);
                    itemPanel.setBackground(interpolatedColor);
                }
                itemPanel.repaint();
            }
        });

        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (timer.isRunning()) {
                    timer.stop();
                }
                startColor[0] = itemPanel.getBackground();
                endColor[0] = SIDEBAR_HOVER_COLOR;
                startTime[0] = System.currentTimeMillis();
                timer.start();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (timer.isRunning()) {
                    timer.stop();
                }
                startColor[0] = itemPanel.getBackground();
                endColor[0] = SIDEBAR_DEFAULT_COLOR;
                startTime[0] = System.currentTimeMillis();
                timer.start();
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                System.out.println("Clicked: " + text);
                cardLayout.show(mainContentPanel, text);
            }
        });

        parentPanel.add(itemPanel);
    }

    /**
     * Custom JPanel for drawing rounded rectangles and simulating neumorphic shadows.
     * Used for both sidebar items and the new Image Tool buttons.
     */
    class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int radius) {
            this.cornerRadius = radius;
            setOpaque(false); // Make the panel transparent so we can paint our own shape
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Draw dark shadow (bottom-right)
            g2.setColor(NEUMORPHIC_DARK_SHADOW);
            g2.fillRoundRect(NEUMORPHIC_SHADOW_OFFSET, NEUMORPHIC_SHADOW_OFFSET,
                    width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET,
                    cornerRadius, cornerRadius);

            // Draw light shadow (top-left)
            g2.setColor(NEUMORPHIC_LIGHT_SHADOW);
            g2.fillRoundRect(0, 0,
                    width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET,
                    cornerRadius, cornerRadius);

            // Draw the main rounded rectangle background
            g2.setColor(getBackground()); // Use the panel's current background color
            g2.fillRoundRect(0, 0, width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET, cornerRadius, cornerRadius);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            // Adjust preferred size to account for shadows
            Dimension superPref = super.getPreferredSize();
            return new Dimension(superPref.width + NEUMORPHIC_SHADOW_OFFSET, superPref.height + NEUMORPHIC_SHADOW_OFFSET);
        }
    }

    // Custom JPanel for background image
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = ImageIO.read(getClass().getResource(imagePath));
            } catch (Exception e) {
                System.err.println("Error loading background image: " + imagePath + " - " + e.getMessage());
                backgroundImage = null;
            }
            // Ensure child components are transparent so background shows through
            // setOpaque(false); // Not needed here as paintComponent handles the background
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private Color interpolateColor(Color color1, Color color2, float fraction) {
        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
        return new Color(r, g, b);
    }


    private void createMainContentArea() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(MAIN_CONTENT_COLOR);

        mainContentPanel.add(createHomePagePanel(), "Home");
        mainContentPanel.add(createImagePagePanel(), "Image");
        mainContentPanel.add(createContentPanel("Playing Audio Content."), "Audio");
        mainContentPanel.add(createContentPanel("Playing Video Content."), "Video");
        mainContentPanel.add(createContentPanel("Displaying Text Content."), "Text");
        mainContentPanel.add(createContentPanel("Information about the application."), "Information");

        // Add new content panels for Image Tool buttons
        mainContentPanel.add(createContentPanel("Content for Add Watermark."), "Add Watermark");
        mainContentPanel.add(createContentPanel("Content for Fetch Watermark."), "Fetch Watermark");
        mainContentPanel.add(createContentPanel("Content for Add Digital Signature."), "Add Digital Signature");
        mainContentPanel.add(createContentPanel("Content for Get Digital Signature."), "Get Digital Signature");
        mainContentPanel.add(createContentPanel("Content for Split Image."), "Split image");
        mainContentPanel.add(createContentPanel("Content for Reconstruct Image."), "Reconstruct image");
        mainContentPanel.add(createContentPanel("Content for Compress Image."), "Compress image");
    }

    private JPanel createHomePagePanel() {
        JPanel homePanel = new BackgroundPanel("/icons/homepage_background.jpg");
        homePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel combinedLogoLabel = new JLabel();
        try {
            ImageIcon combinedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/combined_university_logo.png")).getImage().getScaledInstance(250, 65, Image.SCALE_SMOOTH));
            combinedLogoLabel.setIcon(combinedIcon);
        } catch (Exception e) {
            System.err.println("Error loading combined logo from resource: /icons/combined_university_logo.png - " +
                    "Please ensure you have created this single image file and placed it in src/main/resources/icons/. Error: " + e.getMessage());
        }
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 5, 10, 5);
        homePanel.add(combinedLogoLabel, gbc);

        JLabel mainTitleLabel = new JLabel("MULTIMEDIA ENCRYPTION AND COPYRIGHT");
        mainTitleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitleLabel.setForeground(Color.BLACK);
        mainTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitleLabel.setOpaque(false); // Ensure transparent background
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 5, 5, 5);
        homePanel.add(mainTitleLabel, gbc);

        JLabel mainTitleLabel2 = new JLabel("PROTECTION");
        mainTitleLabel2.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitleLabel2.setForeground(Color.BLACK);
        mainTitleLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitleLabel2.setOpaque(false); // Ensure transparent background
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 30, 5);
        homePanel.add(mainTitleLabel2, gbc);

        JLabel deptLabel = new JLabel("DEPARTMENT OF COMPUTER SCIENCE");
        deptLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 20));
        deptLabel.setForeground(Color.BLACK);
        deptLabel.setHorizontalAlignment(SwingConstants.CENTER);
        deptLabel.setOpaque(false); // Ensure transparent background
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 5, 0, 5);
        homePanel.add(deptLabel, gbc);

        JLabel deptLabel2 = new JLabel("AND ENGINEERING");
        deptLabel2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 20));
        deptLabel2.setForeground(Color.BLACK);
        deptLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        deptLabel2.setOpaque(false); // Ensure transparent background
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 5, 40, 5);
        homePanel.add(deptLabel2, gbc);

        JLabel developedByLabel = new JLabel("DEVELOPED BY :");
        developedByLabel.setFont(new Font("Arial", Font.BOLD, 18));
        developedByLabel.setForeground(Color.BLACK);
        developedByLabel.setHorizontalAlignment(SwingConstants.CENTER);
        developedByLabel.setOpaque(false); // Ensure transparent background
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 5, 5, 5);
        homePanel.add(developedByLabel, gbc);

        String[] names = {
                "AKASH CHAKRABORTY",
                "SANJUKTA SARKAR",
                "ABHIRUP SAMADDER",
                "APARAJITO RAY CHAUDHURI"
        };
        String[] urls = {
                "https://www.linkedin.com/in/skyycodes/",
                "https://www.linkedin.com/in/sanjukta-sarkar-2a2533255/",
                "https://github.com/abhirup-coregit",
                "https://github.com/apara1807"
        };


        for (int i = 0; i < names.length; i++) {
            final String nameText = names[i];
            final String url = urls[i];

            JLabel nameLabel = new JLabel("<html><u>" + nameText + "</u></html>");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            nameLabel.setForeground(Color.BLUE);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            nameLabel.setOpaque(false); // Ensure transparent background

            nameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(url));
                        } else {
                            System.err.println("Desktop or BROWSE action not supported on this system.");
                        }
                    } catch (Exception ex) {
                        System.err.println("Error opening URL: " + url + " - " + ex.getMessage());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nameLabel.setForeground(new Color(0, 0, 150));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nameLabel.setForeground(Color.BLUE);
                }
            });

            gbc.gridy = 6 + i;
            gbc.insets = new Insets(2, 5, 2, 5);
            homePanel.add(nameLabel, gbc);
        }

        return homePanel;
    }

    /**
     * Creates a custom rounded button for the Image Tools page.
     * @param buttonText The text to display on the button.
     * @param cardName The name of the card to show when this button is clicked.
     * @return A JPanel (RoundedPanel) containing the button.
     */
    private JPanel createImageToolButton(String buttonText, String cardName) {
        RoundedPanel buttonPanel = new RoundedPanel(10); // Slightly smaller radius for these buttons
        buttonPanel.setBackground(BUTTON_DEFAULT_COLOR);
        buttonPanel.setOpaque(false); // Crucial for RoundedPanel to draw its background
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.setLayout(new GridBagLayout()); // Use GridBagLayout to center the label inside

        JLabel label = new JLabel(buttonText);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.BLACK);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(false); // Ensure label is transparent

        // Add label to the center of the RoundedPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        buttonPanel.add(label, gbc);

        // Add padding around the button
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Adjusted padding for button size

        // Animation and click logic for the tool buttons
        final Color[] startColor = {BUTTON_DEFAULT_COLOR};
        final Color[] endColor = {BUTTON_HOVER_COLOR};
        final long[] startTime = {0};
        Timer timer = new Timer(1000 / FRAME_RATE, null);

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime[0];

                if (elapsed >= ANIMATION_DURATION) {
                    buttonPanel.setBackground(endColor[0]);
                    ((Timer) e.getSource()).stop();
                } else {
                    float fraction = (float) elapsed / ANIMATION_DURATION;
                    Color interpolatedColor = interpolateColor(startColor[0], endColor[0], fraction);
                    buttonPanel.setBackground(interpolatedColor);
                }
                buttonPanel.repaint();
            }
        });

        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (timer.isRunning()) {
                    timer.stop();
                }
                startColor[0] = buttonPanel.getBackground();
                endColor[0] = BUTTON_HOVER_COLOR;
                startTime[0] = System.currentTimeMillis();
                timer.start();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (timer.isRunning()) {
                    timer.stop();
                }
                startColor[0] = buttonPanel.getBackground();
                endColor[0] = BUTTON_DEFAULT_COLOR;
                startTime[0] = System.currentTimeMillis();
                timer.start();
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                System.out.println("Clicked Image Tool: " + buttonText);
                cardLayout.show(mainContentPanel, cardName);
            }
        });

        return buttonPanel;
    }


    /**
     * Creates the Image page panel with a centered heading and a separator line,
     * followed by a "Digital Watermark" subheading and its related buttons.
     * @return A JPanel representing the Image Tools page with headings and buttons.
     */
    private JPanel createImagePagePanel() {
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(MAIN_CONTENT_COLOR); // Set background to main content color
        imagePanel.setLayout(new BorderLayout()); // Use BorderLayout for overall structure

        // --- Top Heading Area (Image Related Tools) ---
        // This panel naturally stretches horizontally due to BorderLayout.NORTH
        JPanel headingWrapper = new JPanel(new BorderLayout());
        headingWrapper.setBackground(SIDEBAR_DEFAULT_COLOR);
        headingWrapper.setOpaque(true);

        JPanel titleContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleContentPanel.setOpaque(false);
        titleContentPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel mainTitle = new JLabel("Image Related Tools");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 32));
        mainTitle.setForeground(Color.BLACK);
        titleContentPanel.add(mainTitle);

        headingWrapper.add(titleContentPanel, BorderLayout.CENTER);
        headingWrapper.setBorder(new MatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY)); // 2 pixel thick line at the bottom

        imagePanel.add(headingWrapper, BorderLayout.NORTH);


        // --- Content Area Below Top Heading (Now using GridBagLayout) ---
        // This panel holds all the content below the top heading and line
        JPanel contentBelowHeading = new JPanel(new GridBagLayout());
        contentBelowHeading.setBackground(MAIN_CONTENT_COLOR);
        // We no longer apply a border to contentBelowHeading itself.
        // Instead, we'll apply insets to individual components or their containers.

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Span full width of the contentBelowHeading panel
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
        gbc.anchor = GridBagConstraints.CENTER; // Center components horizontally
        gbc.insets = new Insets(0, 0, 0, 0); // Reset insets for general use


        // Add some vertical space
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 50, 0, 50); // Top padding + horizontal padding for content
        contentBelowHeading.add(Box.createVerticalStrut(1), gbc);

        // --- Digital Watermark Heading ---
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 50, 0, 50); // Only horizontal padding, top handled by strut
        JLabel digitalWatermarkHeading = new JLabel("Digital Watermark");
        digitalWatermarkHeading.setFont(new Font("Arial", Font.BOLD, 24));
        digitalWatermarkHeading.setForeground(Color.BLACK);
        digitalWatermarkHeading.setHorizontalAlignment(SwingConstants.CENTER);
        contentBelowHeading.add(digitalWatermarkHeading, gbc);

        // Add some vertical space
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 50, 0, 50); // Top padding + horizontal padding
        contentBelowHeading.add(Box.createVerticalStrut(1), gbc);

        // --- Digital Watermark Buttons ---
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 50, 0, 50); // Horizontal padding only
        JPanel watermarkButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        watermarkButtonsPanel.setOpaque(false);

        JPanel addWatermarkButton = createImageToolButton("Add Watermark", "Add Watermark");
        watermarkButtonsPanel.add(addWatermarkButton);

        JPanel fetchWatermarkButton = createImageToolButton("Fetch Watermark", "Fetch Watermark");
        watermarkButtonsPanel.add(fetchWatermarkButton);

        contentBelowHeading.add(watermarkButtonsPanel, gbc);

        // --- Add horizontal line below Digital Watermark buttons ---
        // This JSeparator will naturally span the full width of its parent (contentBelowHeading)
        // because gbc.fill is Horizontal and there are no left/right insets on its GBC.
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 0, 0, 0); // Top padding, NO horizontal padding for the line
        JSeparator bottomSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        bottomSeparator.setForeground(Color.LIGHT_GRAY); // Set the color of the separator line
        contentBelowHeading.add(bottomSeparator, gbc);


        // Add vertical glue to push content to the top
        gbc.gridy = 5;
        gbc.weighty = 1.0; // This makes this row take up all remaining vertical space
        contentBelowHeading.add(Box.createVerticalGlue(), gbc);


        // Add the content area to the CENTER of the imagePanel
        imagePanel.add(contentBelowHeading, BorderLayout.CENTER);

        return imagePanel;
    }


    /**
     * Helper method to create a simple content panel with centered text.
     * This is for other tabs and the new tool pages.
     * @param contentText The text to display in the content panel.
     * @return A JPanel representing a content view.
     */
    private JPanel createContentPanel(String contentText) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(MAIN_CONTENT_COLOR);
        JLabel label = new JLabel(contentText);
        label.setForeground(Color.DARK_GRAY);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GeminiLikeAppResizable());
    }
}