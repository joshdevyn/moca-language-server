package com.github.joshdevyn.mocaide;

import com.github.joshdevyn.mocaide.gui.MocaGuiWindow;

import javax.swing.SwingUtilities;

/**
 * Main entry point for MOCA GUI Application
 * This class serves as the launcher for the MOCA GUI
 */
public class MocaGuiApplication {
    
    public static void main(String[] args) {
        // Set system properties for better cross-platform compatibility
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // For high DPI displays
        System.setProperty("sun.java2d.uiScale", "1.0");
        
        // Launch the MOCA GUI
        SwingUtilities.invokeLater(() -> {
            new MocaGuiWindow().setVisible(true);
        });
    }
}
