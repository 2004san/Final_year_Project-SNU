package com.snu.project;



import javax.swing.*;

import java.awt.*;



public class Main extends JFrame {



    public Main() {

        setTitle("Application");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800, 300); // Adjust size as needed

        setLocationRelativeTo(null); // Center the window



        JTabbedPane tabbedPane = new JTabbedPane();



// Home Tab

        JPanel homePanel = new JPanel();

// Keep Home empty for now

        tabbedPane.addTab("Home", homePanel);



// Tools Tab

        JPanel toolsPanel = new JPanel(); // Now Tools is also empty

// Keep Tools empty for now

        tabbedPane.addTab("Tools", toolsPanel);



        add(tabbedPane);

    }



    public static void main(String[] args) {

// Ensure GUI updates are done on the Event Dispatch Thread

        SwingUtilities.invokeLater(() -> {

            new Main().setVisible(true);

        });

    }

}