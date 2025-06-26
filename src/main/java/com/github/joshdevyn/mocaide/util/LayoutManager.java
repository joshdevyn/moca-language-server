package com.github.joshdevyn.mocaide.util;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Manages layout persistence for the MOCA GUI application
 * Saves and restores window positions, panel sizes, and UI state
 */
public class LayoutManager {
    
    private static final String PREFS_NODE = "com/github/joshdevyn/mocaide/layout";
    private static final Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
    
    // Window properties
    private static final String WINDOW_X = "window.x";
    private static final String WINDOW_Y = "window.y";
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";
    private static final String WINDOW_MAXIMIZED = "window.maximized";
    
    // Split pane properties
    private static final String MAIN_SPLIT_DIVIDER = "main.split.divider";
    private static final String SECONDARY_SPLIT_DIVIDER = "secondary.split.divider";
    
    // Tab selections
    private static final String SELECTED_RESULT_TAB = "selected.result.tab";
    
    /**
     * Save the current layout state
     */
    public static void saveLayout(JFrame mainWindow, JSplitPane mainSplitPane, 
                                  JSplitPane secondarySplitPane, JTabbedPane resultTabs) {
        try {
            // Save window state
            if (mainWindow.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                prefs.putBoolean(WINDOW_MAXIMIZED, true);
            } else {
                prefs.putBoolean(WINDOW_MAXIMIZED, false);
                prefs.putInt(WINDOW_X, mainWindow.getX());
                prefs.putInt(WINDOW_Y, mainWindow.getY());
                prefs.putInt(WINDOW_WIDTH, mainWindow.getWidth());
                prefs.putInt(WINDOW_HEIGHT, mainWindow.getHeight());
            }
            
            // Save split pane positions
            if (mainSplitPane != null) {
                prefs.putInt(MAIN_SPLIT_DIVIDER, mainSplitPane.getDividerLocation());
            }
            
            if (secondarySplitPane != null) {
                prefs.putInt(SECONDARY_SPLIT_DIVIDER, secondarySplitPane.getDividerLocation());
            }
            
            // Save tab selection
            if (resultTabs != null) {
                prefs.putInt(SELECTED_RESULT_TAB, resultTabs.getSelectedIndex());
            }
            
            // Flush preferences
            prefs.flush();
            
        } catch (Exception e) {
            System.err.println("Error saving layout: " + e.getMessage());
        }
    }
    
    /**
     * Restore the saved layout state
     */
    public static void restoreLayout(JFrame mainWindow, JSplitPane mainSplitPane,
                                     JSplitPane secondarySplitPane, JTabbedPane resultTabs) {
        try {
            // Restore window state
            boolean maximized = prefs.getBoolean(WINDOW_MAXIMIZED, false);
            if (maximized) {
                mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                int x = prefs.getInt(WINDOW_X, 100);
                int y = prefs.getInt(WINDOW_Y, 100);
                int width = prefs.getInt(WINDOW_WIDTH, 1200);
                int height = prefs.getInt(WINDOW_HEIGHT, 800);
                
                // Ensure window is visible on screen
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle screenBounds = ge.getMaximumWindowBounds();
                
                if (x + width > screenBounds.width || y + height > screenBounds.height ||
                    x < 0 || y < 0) {
                    // Reset to default position if saved position is invalid
                    x = 100;
                    y = 100;
                    width = Math.min(1200, screenBounds.width - 200);
                    height = Math.min(800, screenBounds.height - 200);
                }
                
                mainWindow.setBounds(x, y, width, height);
            }
            
            // Restore split pane positions (do this after window is visible)
            SwingUtilities.invokeLater(() -> {
                if (mainSplitPane != null) {
                    int dividerLocation = prefs.getInt(MAIN_SPLIT_DIVIDER, 400);
                    mainSplitPane.setDividerLocation(dividerLocation);
                }
                
                if (secondarySplitPane != null) {
                    int dividerLocation = prefs.getInt(SECONDARY_SPLIT_DIVIDER, 200);
                    secondarySplitPane.setDividerLocation(dividerLocation);
                }
                
                // Restore tab selection
                if (resultTabs != null) {
                    int selectedTab = prefs.getInt(SELECTED_RESULT_TAB, 0);
                    if (selectedTab >= 0 && selectedTab < resultTabs.getTabCount()) {
                        resultTabs.setSelectedIndex(selectedTab);
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error restoring layout: " + e.getMessage());
        }
    }
    
    /**
     * Save a simple key-value preference
     */
    public static void savePreference(String key, String value) {
        prefs.put(key, value);
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error saving preference " + key + ": " + e.getMessage());
        }
    }
    
    /**
     * Load a preference with default value
     */
    public static String loadPreference(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }
    
    /**
     * Save an integer preference
     */
    public static void savePreference(String key, int value) {
        prefs.putInt(key, value);
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error saving preference " + key + ": " + e.getMessage());
        }
    }
    
    /**
     * Load an integer preference with default value
     */
    public static int loadPreference(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
    
    /**
     * Save a boolean preference
     */
    public static void savePreference(String key, boolean value) {
        prefs.putBoolean(key, value);
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error saving preference " + key + ": " + e.getMessage());
        }
    }
    
    /**
     * Load a boolean preference with default value
     */
    public static boolean loadPreference(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
    
    /**
     * Clear all saved preferences
     */
    public static void clearAllPreferences() {
        try {
            prefs.clear();
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error clearing preferences: " + e.getMessage());
        }
    }
    
    /**
     * Export layout settings to a file
     */
    public static void exportLayout(File file) throws IOException {
        Properties props = new Properties();
        
        // Export all preferences
        try {
            String[] keys = prefs.keys();
            for (String key : keys) {
                String value = prefs.get(key, "");
                props.setProperty(key, value);
            }
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "MOCA GUI Layout Settings - " + new java.util.Date());
            }
            
        } catch (Exception e) {
            throw new IOException("Error exporting layout: " + e.getMessage());
        }
    }
    
    /**
     * Import layout settings from a file
     */
    public static void importLayout(File file) throws IOException {
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            
            // Import all properties
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                prefs.put(key, value);
            }
            
            prefs.flush();
            
        } catch (Exception e) {
            throw new IOException("Error importing layout: " + e.getMessage());
        }
    }
    
    /**
     * Setup automatic layout saving when the application closes
     */
    public static void setupAutoSave(JFrame mainWindow, JSplitPane mainSplitPane,
                                     JSplitPane secondarySplitPane, JTabbedPane resultTabs) {
        
        // Add window listener to save layout on close
        mainWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveLayout(mainWindow, mainSplitPane, secondarySplitPane, resultTabs);
            }
        });
        
        // Add component listener to save layout on resize/move
        mainWindow.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (mainWindow.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    saveLayout(mainWindow, mainSplitPane, secondarySplitPane, resultTabs);
                }
            }
            
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (mainWindow.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    saveLayout(mainWindow, mainSplitPane, secondarySplitPane, resultTabs);
                }
            }
        });
        
        // Add listeners to split panes
        if (mainSplitPane != null) {
            mainSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, 
                e -> saveLayout(mainWindow, mainSplitPane, secondarySplitPane, resultTabs));
        }
        
        if (secondarySplitPane != null) {
            secondarySplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, 
                e -> saveLayout(mainWindow, mainSplitPane, secondarySplitPane, resultTabs));
        }
        
        // Add listener to tab selection
        if (resultTabs != null) {
            resultTabs.addChangeListener(e -> 
                saveLayout(mainWindow, mainSplitPane, secondarySplitPane, resultTabs));
        }
    }
}
