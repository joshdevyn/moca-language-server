package com.github.joshdevyn.mocaide.gui;

import com.github.joshdevyn.mocaide.connection.ConnectionProfile;
import com.github.joshdevyn.mocaide.gui.editor.FindReplaceDialog;
import com.github.joshdevyn.mocaide.gui.editor.AdvancedScriptEditor;
import com.github.joshdevyn.mocaide.gui.results.ResultsPanel;
import com.github.joshdevyn.mocaide.util.PerformanceMonitor;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaResults;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Unified command execution panel that combines script editing and command execution
 * with advanced result management and multi-command support
 */
public class CommandExecutionPanel extends JPanel {
    
    private AdvancedScriptEditor editor;
    private ResultsPanel resultsPanel;
    private JButton executeButton, executeSelectedButton, clearResultsButton;
    private JButton newButton, openButton, saveButton, saveAsButton;
    private JToggleButton autoExecuteToggle, syntaxCheckToggle;
    private JLabel connectionStatusLabel, executionStatusLabel;
    private JProgressBar executionProgress;
    
    private MocaConnection mocaConnection;
    private String currentFilePath;
    private boolean hasUnsavedChanges;
    
    // Find/Replace dialogs
    private FindReplaceDialog findDialog;
    private FindReplaceDialog replaceDialog;
    
    // Command separation patterns
    private static final Pattern COMMAND_SEPARATOR = Pattern.compile(";\\s*(?=\\w)");
    private static final Pattern GO_SEPARATOR = Pattern.compile("(?i)^\\s*go\\s*$", Pattern.MULTILINE);
    
    private JToolBar toolbar;
    private JToggleButton syntaxCheckToggleButton;
    private JToggleButton autoExecuteToggleButton;
    private JLabel connectionStatusIndicator;
    
    public CommandExecutionPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Editor component
        editor = new AdvancedScriptEditor();
        
        // Results panel
        resultsPanel = new ResultsPanel();
        
        // Execution buttons
        executeButton = new JButton("Execute All");
        executeButton.setToolTipText("Execute all commands in the editor (F5)");
        executeButton.setEnabled(false); // Will be enabled when connection is established
        
        executeSelectedButton = new JButton("Execute Selected");
        executeSelectedButton.setToolTipText("Execute only the selected text (Ctrl+F5)");
        executeSelectedButton.setEnabled(false); // Will be enabled when connection is established
        
        clearResultsButton = new JButton("Clear Results");
        clearResultsButton.setToolTipText("Clear all result tables");
        
        // File operation buttons
        newButton = new JButton("New");
        newButton.setToolTipText("Create new script (Ctrl+N)");
        openButton = new JButton("Open");
        openButton.setToolTipText("Open script file (Ctrl+O)");
        saveButton = new JButton("Save");
        saveButton.setToolTipText("Save current script (Ctrl+S)");
        saveAsButton = new JButton("Save As");
        saveAsButton.setToolTipText("Save script with new name (Ctrl+Shift+S)");
        
        // Toggle buttons with visual indicators
        autoExecuteToggle = new JToggleButton("Auto Execute");
        autoExecuteToggle.setToolTipText("Automatically execute commands as you type");
        syntaxCheckToggle = new JToggleButton("Syntax Check", true); // On by default
        syntaxCheckToggle.setToolTipText("Enable real-time syntax checking");
        
        // Status components
        connectionStatusLabel = new JLabel("No Connection");
        connectionStatusLabel.setForeground(Color.RED);
        executionStatusLabel = new JLabel("Ready");
        executionProgress = new JProgressBar();
        executionProgress.setVisible(false);
        
        // Toolbar with visual indicators
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        syntaxCheckToggleButton = new JToggleButton("Syntax Check");
        syntaxCheckToggleButton.setSelected(true);
        syntaxCheckToggleButton.setToolTipText("Toggle real-time syntax checking");
        syntaxCheckToggleButton.setIcon(createIndicatorIcon(Color.GREEN));
        
        autoExecuteToggleButton = new JToggleButton("Auto-Execute");
        autoExecuteToggleButton.setSelected(false);
        autoExecuteToggleButton.setToolTipText("Toggle auto-execute on script change");
        autoExecuteToggleButton.setIcon(createIndicatorIcon(Color.GRAY));
        
        connectionStatusIndicator = new JLabel("Disconnected");
        connectionStatusIndicator.setOpaque(true);
        connectionStatusIndicator.setBackground(Color.RED);
        connectionStatusIndicator.setForeground(Color.WHITE);
        connectionStatusIndicator.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        
        // Add components to toolbar
        toolbar.add(executeButton);
        toolbar.add(executeSelectedButton);
        toolbar.addSeparator();
        toolbar.add(clearResultsButton);
        toolbar.addSeparator();
        toolbar.add(syntaxCheckToggleButton);
        toolbar.addSeparator();
        toolbar.add(autoExecuteToggleButton);
        toolbar.addSeparator();
        toolbar.add(connectionStatusIndicator);
        
        // Style toggle buttons
        styleToggleButton(autoExecuteToggle);
        styleToggleButton(syntaxCheckToggle);
        
        currentFilePath = null;
        hasUnsavedChanges = false;
        
        // Set default command for new users
        editor.setText("list warehouses");
        markAsSaved(); // Don't mark as modified for the default content
    }
    
    private Icon createIndicatorIcon(Color color) {
        int size = 12;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(color);
        g2.fillOval(0, 0, size - 1, size - 1);
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval(0, 0, size - 1, size - 1);
        g2.dispose();
        return new ImageIcon(image);
    }
    
    private void styleToggleButton(JToggleButton button) {
        button.setFocusPainted(false);
        button.addActionListener(e -> updateToggleButtonAppearance(button));
        updateToggleButtonAppearance(button);
    }
    
    private void updateToggleButtonAppearance(JToggleButton button) {
        if (button.isSelected()) {
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
        } else {
            button.setBackground(UIManager.getColor("Button.background"));
            button.setForeground(UIManager.getColor("Button.foreground"));
            button.setOpaque(false);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        
        // Create main content area with split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setTopComponent(editor);
        mainSplitPane.setBottomComponent(resultsPanel);
        mainSplitPane.setDividerLocation(400);
        mainSplitPane.setResizeWeight(0.6);
        
        add(mainSplitPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        // Left side - connection status
        JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftStatus.add(new JLabel("Connection:"));
        leftStatus.add(connectionStatusLabel);
        
        // Center - execution status
        JPanel centerStatus = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerStatus.add(executionStatusLabel);
        centerStatus.add(executionProgress);
        
        statusBar.add(leftStatus, BorderLayout.WEST);
        statusBar.add(centerStatus, BorderLayout.CENTER);
        
        return statusBar;
    }
    
    private void setupEventHandlers() {
        // File operations
        newButton.addActionListener(this::handleNew);
        openButton.addActionListener(this::handleOpen);
        saveButton.addActionListener(this::handleSave);
        saveAsButton.addActionListener(this::handleSaveAs);
        
        // Execution operations
        executeButton.addActionListener(this::handleExecuteAll);
        executeSelectedButton.addActionListener(this::handleExecuteSelected);
        clearResultsButton.addActionListener(this::handleClearResults);
        
        // Toggle operations
        autoExecuteToggle.addActionListener(this::handleAutoExecuteToggle);
        syntaxCheckToggle.addActionListener(this::handleSyntaxCheckToggle);
        
        syntaxCheckToggleButton.addActionListener(e -> {
            boolean enabled = syntaxCheckToggleButton.isSelected();
            syntaxCheckToggleButton.setIcon(createIndicatorIcon(enabled ? Color.GREEN : Color.GRAY));
            // Syntax checking is always enabled in the advanced editor
            // Visual feedback is provided through the toggle state
        });
        autoExecuteToggleButton.addActionListener(e -> {
            boolean enabled = autoExecuteToggleButton.isSelected();
            autoExecuteToggleButton.setIcon(createIndicatorIcon(enabled ? Color.GREEN : Color.GRAY));
            // Implement auto-execute logic if needed
        });
        
        // Enable syntax checking by default in the advanced editor
        syntaxCheckToggle.setSelected(true);
        
        // Track document changes
        editor.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { markAsModified(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { markAsModified(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { markAsModified(); }
        });
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void setupKeyboardShortcuts() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        // F5 - Execute All
        inputMap.put(KeyStroke.getKeyStroke("F5"), "executeAll");
        actionMap.put("executeAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { handleExecuteAll(e); }
        });
        
        // Ctrl+F5 - Execute Selected
        inputMap.put(KeyStroke.getKeyStroke("ctrl F5"), "executeSelected");
        actionMap.put("executeSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { handleExecuteSelected(e); }
        });
        
        // File shortcuts
        inputMap.put(KeyStroke.getKeyStroke("ctrl N"), "new");
        actionMap.put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { handleNew(e); }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl O"), "open");
        actionMap.put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { handleOpen(e); }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { handleSave(e); }
        });
    }
    
    // Event handlers
    private void handleNew(ActionEvent e) {
        if (hasUnsavedChanges && !confirmDiscardChanges()) {
            return;
        }
        editor.setText("");
        resultsPanel.clearResults();
        currentFilePath = null;
        markAsSaved();
    }
    
    private void handleOpen(ActionEvent e) {
        if (hasUnsavedChanges && !confirmDiscardChanges()) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MOCA Scripts (*.moca, *.mcmd)", "moca", "mcmd"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                editor.setText(content);
                currentFilePath = file.getAbsolutePath();
                markAsSaved();
                resultsPanel.clearResults();
            } catch (IOException ex) {
                showError("Error opening file: " + ex.getMessage());
            }
        }
    }
    
    private void handleSave(ActionEvent e) {
        if (currentFilePath == null) {
            handleSaveAs(e);
            return;
        }
        
        try {
            Files.write(new File(currentFilePath).toPath(), editor.getText().getBytes());
            markAsSaved();
        } catch (IOException ex) {
            showError("Error saving file: " + ex.getMessage());
        }
    }
    
    private void handleSaveAs(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MOCA Scripts (*.moca)", "moca"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".moca")) {
                file = new File(file.getAbsolutePath() + ".moca");
            }
            
            try {
                Files.write(file.toPath(), editor.getText().getBytes());
                currentFilePath = file.getAbsolutePath();
                markAsSaved();
            } catch (IOException ex) {
                showError("Error saving file: " + ex.getMessage());
            }
        }
    }
    
    private void handleExecuteAll(ActionEvent e) {
        String script = editor.getText().trim();
        if (script.isEmpty()) {
            showError("No commands to execute");
            return;
        }
        executeCommands(script);
    }
    
    private void handleExecuteSelected(ActionEvent e) {
        String selected = editor.getSelectedText();
        if (selected == null || selected.trim().isEmpty()) {
            showError("No text selected");
            return;
        }
        executeCommands(selected.trim());
    }
    
    private void handleClearResults(ActionEvent e) {
        resultsPanel.clearResults();
    }
    
    private void handleAutoExecuteToggle(ActionEvent e) {
        updateToggleButtonAppearance(autoExecuteToggle);
        // TODO: Implement auto-execution logic
    }
    
    private void handleSyntaxCheckToggle(ActionEvent e) {
        updateToggleButtonAppearance(syntaxCheckToggle);
        // Syntax checking is always enabled in the advanced editor
    }
    
    /**
     * Execute multiple commands and display results
     */
    private void executeCommands(String script) {
        if (mocaConnection == null) {
            showError("No active connection. Please connect to a MOCA server first.");
            return;
        }
        
        // Parse script into individual commands
        List<String> commands = parseCommands(script);
        if (commands.isEmpty()) {
            showError("No valid commands found");
            return;
        }
        
        // Execute commands in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                executionProgress.setVisible(true);
                executionProgress.setIndeterminate(true);
                
                for (int i = 0; i < commands.size(); i++) {
                    final int commandIndex = i + 1;
                    String command = commands.get(i);
                    publish("Executing command " + commandIndex + " of " + commands.size());
                    
                    // Start performance tracking
                    PerformanceMonitor.PerformanceTracker tracker = 
                        PerformanceMonitor.startTracking(command);
                    
                    try {
                        MocaResults results = mocaConnection.executeCommand(command);
                        
                        // Finish tracking with success
                        PerformanceMonitor.ExecutionMetrics metrics = 
                            tracker.finish(true, results.getRowCount());
                        
                        SwingUtilities.invokeLater(() -> {
                            resultsPanel.addResult(command, results, metrics.getExecutionTimeSeconds());
                        });
                    } catch (Exception e) {
                        // Finish tracking with error
                        tracker.finish(false, e.getMessage(), 0);
                        
                        SwingUtilities.invokeLater(() -> {
                            resultsPanel.addError("Command " + commandIndex, command, e.getMessage());
                        });
                    }
                }
                
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    executionStatusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                executionProgress.setVisible(false);
                executionStatusLabel.setText("Execution completed");
            }
        };
        
        worker.execute();
    }
    
    /**
     * Parse script into individual commands
     */
    private List<String> parseCommands(String script) {
        List<String> commands = new ArrayList<>();
        
        // Split by semicolon or GO statement
        String[] parts = GO_SEPARATOR.split(script);
        for (String part : parts) {
            String[] subParts = COMMAND_SEPARATOR.split(part);
            for (String subPart : subParts) {
                String trimmed = subPart.trim();
                if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("go")) {
                    commands.add(trimmed);
                }
            }
        }
        
        return commands;
    }
    
    // Utility methods
    private void markAsModified() {
        hasUnsavedChanges = true;
        // Update UI to show unsaved changes
    }
    
    private void markAsSaved() {
        hasUnsavedChanges = false;
        // Update UI to show saved state
    }
    
    private boolean confirmDiscardChanges() {
        int result = JOptionPane.showConfirmDialog(this,
            "You have unsaved changes. Do you want to discard them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Set the current connection
     */
    public void setConnection(ConnectionProfile profile, MocaConnection connection) {
        this.mocaConnection = connection;
        
        if (profile != null && connection != null) {
            connectionStatusLabel.setText("Connected to " + profile.getName());
            connectionStatusLabel.setForeground(new Color(0, 128, 0));
            executeButton.setEnabled(true);
            executeSelectedButton.setEnabled(true);
        } else {
            connectionStatusLabel.setText("No Connection");
            connectionStatusLabel.setForeground(Color.RED);
            executeButton.setEnabled(false);
            executeSelectedButton.setEnabled(false);
        }
    }
    
    // Call this method when connection status changes
    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusIndicator.setText("Connected");
            connectionStatusIndicator.setBackground(new Color(0, 153, 51));
        } else {
            connectionStatusIndicator.setText("Disconnected");
            connectionStatusIndicator.setBackground(Color.RED);
        }
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public String getCurrentFilePath() {
        return currentFilePath;
    }
    
    // Public methods expected by MocaGuiWindow
    public void updateConnection() {
        // Update connection status based on current connection
        if (mocaConnection != null) {
            setConnectionStatus(true);
        } else {
            setConnectionStatus(false);
        }
    }
    
    public void newScript() {
        handleNew(null);
    }
    
    public void openScript() {
        handleOpen(null);
    }
    
    public void saveScript() {
        handleSave(null);
    }
    
    public void saveAsScript() {
        handleSaveAs(null);
    }
    
    public void undo() {
        editor.undo();
    }
    
    public void redo() {
        editor.redo();
    }
    
    public void cut() {
        editor.cut();
    }
    
    public void copy() {
        editor.copy();
    }
    
    public void paste() {
        editor.paste();
    }
    
    public void showFindDialog() {
        if (findDialog == null) {
            Window window = SwingUtilities.getWindowAncestor(this);
            Frame frame = (window instanceof Frame) ? (Frame) window : null;
            findDialog = new FindReplaceDialog(frame, editor.getTextArea(), false);
        }
        findDialog.setVisible(true);
    }
    
    public void showReplaceDialog() {
        if (replaceDialog == null) {
            Window window = SwingUtilities.getWindowAncestor(this);
            Frame frame = (window instanceof Frame) ? (Frame) window : null;
            replaceDialog = new FindReplaceDialog(frame, editor.getTextArea(), true);
        }
        replaceDialog.setVisible(true);
    }
      public String getSelectedText() {
        return editor.getSelectedText();
    }
    
    public String getAllText() {
        return editor.getText();
    }

    public void executeCommand(String command) {
        executeCommands(command);
    }
    
    public void formatCode() {
        // TODO: Implement code formatting
        JOptionPane.showMessageDialog(this, "Code formatting coming soon!", "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void checkSyntax() {
        // TODO: Implement manual syntax checking
        JOptionPane.showMessageDialog(this, "Manual syntax checking coming soon!", "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Delegate methods for advanced editor features
    public void showGotoLineDialog() {
        editor.showGotoLineDialog();
    }
    
    public void toggleBookmark() {
        editor.toggleBookmark();
    }
    
    public void nextBookmark() {
        editor.goToNextBookmark();
    }
    
    public void previousBookmark() {
        editor.goToPreviousBookmark();
    }
}
