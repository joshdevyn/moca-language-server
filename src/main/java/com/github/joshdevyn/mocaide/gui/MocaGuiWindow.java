package com.github.joshdevyn.mocaide.gui;

import com.github.joshdevyn.mocaide.gui.connection.ConnectionManagerPanel;
import com.github.joshdevyn.mocaide.gui.visualization.DataVisualizationPanel;
import com.github.joshdevyn.mocaide.gui.help.HelpSystem;
import com.github.joshdevyn.mocaide.util.LayoutManager;
import com.github.joshdevyn.mocaide.util.PerformanceMonitor;
import com.github.joshdevyn.mocaide.security.SecurityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main MOCA GUI Window - The primary application window containing all panels
 */
public class MocaGuiWindow extends JFrame {
    
    private static final String TITLE = "MOCA GUI";
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 1000;
    
    private JTabbedPane tabbedPane;
    private CommandExecutionPanel commandExecutionPanel;
    private ConnectionManagerPanel connectionManagerPanel;
    private DataVisualizationPanel dataVisualizationPanel;
    private HelpSystem helpSystem;
    
    public MocaGuiWindow() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setupEventHandlers();
        finalizeFrame();
    }
    
    private void initializeComponents() {
        // Create panels
        connectionManagerPanel = new ConnectionManagerPanel();
        commandExecutionPanel = new CommandExecutionPanel();
        dataVisualizationPanel = new DataVisualizationPanel();

        // Wire up connection changes
        connectionManagerPanel.addConnectionChangeListener(new ConnectionManagerPanel.ConnectionChangeListener() {
            @Override
            public void onConnectionChanged(com.github.joshdevyn.mocaide.connection.ConnectionProfile profile, 
                                          com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection connection) {
                commandExecutionPanel.setConnection(profile, connection);
            }
            
            @Override
            public void onConnectionDisconnected() {
                commandExecutionPanel.setConnection(null, null);
            }
        });

        // Create tabbed pane with modern styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);

        // Add tabs with icons and tooltips
        tabbedPane.addTab("Connections", createConnectionIcon(), connectionManagerPanel, "Manage MOCA server connections");
        tabbedPane.addTab("Command & Script", createEditorIcon(), commandExecutionPanel, "Edit and execute MOCA scripts and commands");
        tabbedPane.addTab("Performance", createPerformanceIcon(), PerformanceMonitor.createMonitoringPanel(), "Monitor performance and execution metrics");
        tabbedPane.addTab("Data Analysis", createAnalysisIcon(), dataVisualizationPanel, "Analyze and visualize data results");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Clean, professional styling without dark theme
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Clean status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        
        JLabel versionLabel = new JLabel("v1.12.25");
        versionLabel.setFont(versionLabel.getFont().deriveFont(10f));
        versionLabel.setForeground(Color.GRAY);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("New Script", "Ctrl+N", this::handleNewScript));
        fileMenu.add(createMenuItem("Open Script", "Ctrl+O", this::handleOpenScript));
        fileMenu.add(createMenuItem("Save Script", "Ctrl+S", this::handleSaveScript));
        fileMenu.add(createMenuItem("Save As...", "Ctrl+Shift+S", this::handleSaveAsScript));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Exit", "Ctrl+Q", this::handleExit));
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem("Undo", "Ctrl+Z", this::handleUndo));
        editMenu.add(createMenuItem("Redo", "Ctrl+Y", this::handleRedo));
        editMenu.addSeparator();
        editMenu.add(createMenuItem("Cut", "Ctrl+X", this::handleCut));
        editMenu.add(createMenuItem("Copy", "Ctrl+C", this::handleCopy));
        editMenu.add(createMenuItem("Paste", "Ctrl+V", this::handlePaste));
        editMenu.addSeparator();
        editMenu.add(createMenuItem("Find", "Ctrl+F", this::handleFind));
        editMenu.add(createMenuItem("Replace", "Ctrl+H", this::handleReplace));
        
        // MOCA menu
        JMenu mocaMenu = new JMenu("MOCA");
        mocaMenu.add(createMenuItem("Execute Command", "F5", this::handleExecuteCommand));
        mocaMenu.add(createMenuItem("Connect to Server", "F6", this::handleConnect));
        mocaMenu.addSeparator();
        mocaMenu.add(createMenuItem("Format Code", "Ctrl+Alt+L", this::handleFormatCode));
        mocaMenu.add(createMenuItem("Check Syntax", "F7", this::handleCheckSyntax));
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createMenuItem("About", null, this::handleAbout));
        helpMenu.add(createMenuItem("MOCA Documentation", "F1", this::handleHelp));
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(mocaMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JMenuItem createMenuItem(String text, String accelerator, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        item.addActionListener(action);
        return item;
    }
    
    private void setupEventHandlers() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Setup global keyboard shortcuts
        setupGlobalKeyboardShortcuts();
        
        // Tab change handler to update connections between panels
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) { // Command panel
                // Update command panel with current connection
                commandExecutionPanel.updateConnection();
            }
        });
    }
    
    private void setupGlobalKeyboardShortcuts() {
        // Get the root pane input and action maps for global shortcuts
        JRootPane rootPane = getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        // File operations
        inputMap.put(KeyStroke.getKeyStroke("ctrl N"), "newScript");
        actionMap.put("newScript", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleNewScript(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl O"), "openScript");
        actionMap.put("openScript", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleOpenScript(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "saveScript");
        actionMap.put("saveScript", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleSaveScript(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift S"), "saveAsScript");
        actionMap.put("saveAsScript", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleSaveAsScript(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl Q"), "exit");
        actionMap.put("exit", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleExit(e); } 
        });
        
        // Edit operations
        inputMap.put(KeyStroke.getKeyStroke("ctrl Z"), "undo");
        actionMap.put("undo", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleUndo(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl Y"), "redo");
        actionMap.put("redo", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleRedo(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl X"), "cut");
        actionMap.put("cut", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleCut(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy");
        actionMap.put("copy", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleCopy(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste");
        actionMap.put("paste", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handlePaste(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl A"), "selectAll");
        actionMap.put("selectAll", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                if (tabbedPane.getSelectedIndex() == 1) {
                    commandExecutionPanel.requestFocus();
                    // The editor will handle Ctrl+A internally
                }
            } 
        });
        
        // Search and navigation
        inputMap.put(KeyStroke.getKeyStroke("ctrl F"), "find");
        actionMap.put("find", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleFind(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl H"), "replace");
        actionMap.put("replace", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleReplace(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl G"), "gotoLine");
        actionMap.put("gotoLine", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                if (tabbedPane.getSelectedIndex() == 1) {
                    commandExecutionPanel.showGotoLineDialog();
                }
            } 
        });
        
        // Bookmarks
        inputMap.put(KeyStroke.getKeyStroke("F2"), "toggleBookmark");
        actionMap.put("toggleBookmark", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                if (tabbedPane.getSelectedIndex() == 1) {
                    commandExecutionPanel.toggleBookmark();
                }
            } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl F2"), "nextBookmark");
        actionMap.put("nextBookmark", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                if (tabbedPane.getSelectedIndex() == 1) {
                    commandExecutionPanel.nextBookmark();
                }
            } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("shift F2"), "previousBookmark");
        actionMap.put("previousBookmark", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                if (tabbedPane.getSelectedIndex() == 1) {
                    commandExecutionPanel.previousBookmark();
                }
            } 
        });
        
        // MOCA operations
        inputMap.put(KeyStroke.getKeyStroke("F5"), "executeCommand");
        actionMap.put("executeCommand", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleExecuteCommand(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("F6"), "connectToServer");
        actionMap.put("connectToServer", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleConnect(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("F7"), "checkSyntax");
        actionMap.put("checkSyntax", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleCheckSyntax(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl alt L"), "formatCode");
        actionMap.put("formatCode", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleFormatCode(e); } 
        });
        
        // Help and documentation
        inputMap.put(KeyStroke.getKeyStroke("F1"), "help");
        actionMap.put("help", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { handleHelp(e); } 
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl SLASH"), "keyboardShortcuts");
        actionMap.put("keyboardShortcuts", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                if (helpSystem == null) {
                    helpSystem = HelpSystem.getInstance(MocaGuiWindow.this);
                }
                helpSystem.showKeyboardShortcuts();
            } 
        });
    }
    
    private void finalizeFrame() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Initialize security manager
        SecurityManager.getInstance().logAuditEvent(
            SecurityManager.AuditEventType.LOGIN, 
            "Application started", 
            System.getProperty("user.name"), 
            "GUI Application Launch"
        );
        
        // Set application icon
        try {
            // You can add an icon here
            // setIconImage(ImageIO.read(getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Ignore if icon not found
        }
        
        // Apply look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Use default look and feel
        }
        
        // Setup layout persistence
        LayoutManager.setupAutoSave(this, null, null, tabbedPane);
        
        // Restore saved layout
        LayoutManager.restoreLayout(this, null, null, tabbedPane);
        
        // If no saved layout, use defaults
        if (getWidth() == 0 || getHeight() == 0) {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocationRelativeTo(null);
        }
        
        // Add shutdown hook for security cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SecurityManager.getInstance().shutdown();
        }));
    }
    
    private ImageIcon createConnectionIcon() {
        return createColoredIcon(new Color(70, 130, 180)); // Steel blue
    }
    
    private ImageIcon createEditorIcon() {
        return createColoredIcon(new Color(255, 140, 0)); // Dark orange
    }
    
    private ImageIcon createPerformanceIcon() {
        return createColoredIcon(new Color(220, 20, 60)); // Crimson
    }
    
    private ImageIcon createAnalysisIcon() {
        return createColoredIcon(new Color(128, 0, 128)); // Purple
    }
    
    private ImageIcon createColoredIcon(Color color) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillRoundRect(2, 2, 12, 12, 4, 4);
        g2d.setColor(color.brighter());
        g2d.drawRoundRect(2, 2, 12, 12, 4, 4);
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    // Menu action handlers
    private void handleNewScript(ActionEvent e) {
        commandExecutionPanel.newScript();
        tabbedPane.setSelectedIndex(1); // Switch to editor tab
    }
    
    private void handleOpenScript(ActionEvent e) {
        commandExecutionPanel.openScript();
        tabbedPane.setSelectedIndex(1);
    }
    
    private void handleSaveScript(ActionEvent e) {
        commandExecutionPanel.saveScript();
    }
    
    private void handleSaveAsScript(ActionEvent e) {
        commandExecutionPanel.saveAsScript();
    }
    
    private void handleExit(ActionEvent e) {
        System.exit(0);
    }
    
    private void handleUndo(ActionEvent e) {
        commandExecutionPanel.undo();
    }
    
    private void handleRedo(ActionEvent e) {
        commandExecutionPanel.redo();
    }
    
    private void handleCut(ActionEvent e) {
        commandExecutionPanel.cut();
    }
    
    private void handleCopy(ActionEvent e) {
        commandExecutionPanel.copy();
    }
    
    private void handlePaste(ActionEvent e) {
        commandExecutionPanel.paste();
    }
    
    private void handleFind(ActionEvent e) {
        commandExecutionPanel.showFindDialog();
    }
    
    private void handleReplace(ActionEvent e) {
        commandExecutionPanel.showReplaceDialog();
    }
    
    private void handleExecuteCommand(ActionEvent e) {
        // Switch to command & script tab first
        tabbedPane.setSelectedIndex(1); // Command & Script tab
        
        // Execute selected text if available, otherwise execute all
        String selectedText = commandExecutionPanel.getSelectedText();
        if (selectedText != null && !selectedText.trim().isEmpty()) {
            commandExecutionPanel.executeCommand(selectedText);
        } else {
            // Execute all commands in the editor
            commandExecutionPanel.executeCommand(commandExecutionPanel.getAllText());
        }
    }
    
    private void handleConnect(ActionEvent e) {
        tabbedPane.setSelectedIndex(0); // Switch to connection tab
    }
    
    private void handleFormatCode(ActionEvent e) {
        commandExecutionPanel.formatCode();
    }
    
    private void handleCheckSyntax(ActionEvent e) {
        commandExecutionPanel.checkSyntax();
    }
    
    private void handleAbout(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
            "MOCA GUI\n" +
            "Version 1.12.25\n" +
            "Built with Java and RSyntaxTextArea\n\n" +
            "Features:\n" +
            "• Syntax highlighting for MOCA\n" +
            "• Code completion and IntelliSense\n" +
            "• Real-time error checking\n" +
            "• MOCA server connectivity\n" +
            "• Professional script editing\n\n" +
            "© 2025 Joshua Devyn",
            "About MOCA GUI",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleHelp(ActionEvent e) {
        // Open comprehensive help system
        if (helpSystem == null) {
            helpSystem = HelpSystem.getInstance(this);
        }
        helpSystem.showHelp("general");
    }
}
