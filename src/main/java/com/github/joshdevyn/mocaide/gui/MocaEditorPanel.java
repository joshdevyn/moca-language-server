package com.github.joshdevyn.mocaide.gui;

import com.github.joshdevyn.mocaide.gui.editor.MocaEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Professional MOCA Script Editor with enhanced features
 */
public class MocaEditorPanel extends JPanel {
    
    private MocaEditor editor;
    private JLabel filePathLabel;
    private JButton newButton, openButton, saveButton, saveAsButton;
    private JButton formatButton, syntaxCheckButton;
    private String currentFilePath;
    private boolean hasUnsavedChanges;
    private MocaConnectionPanel connectionPanel; // Reference to connection panel
    
    public MocaEditorPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    public void setConnectionPanel(MocaConnectionPanel connectionPanel) {
        this.connectionPanel = connectionPanel;
        // Update editor completions when connection changes
        if (connectionPanel != null && connectionPanel.isConnected()) {
            updateEditorCompletions();
        }
    }
    
    private void initializeComponents() {
        editor = new MocaEditor();
        filePathLabel = new JLabel("Untitled.moca");
        filePathLabel.setFont(filePathLabel.getFont().deriveFont(Font.ITALIC));
        
        // Create toolbar buttons
        newButton = new JButton("New");
        openButton = new JButton("Open");
        saveButton = new JButton("Save");
        saveAsButton = new JButton("Save As");
        formatButton = new JButton("Format");
        syntaxCheckButton = new JButton("Check Syntax");
        
        // Style buttons
        styleButton(newButton);
        styleButton(openButton);
        styleButton(saveButton);
        styleButton(saveAsButton);
        styleButton(formatButton);
        styleButton(syntaxCheckButton);
        
        currentFilePath = null;
        hasUnsavedChanges = false;
    }
    
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create modern toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        toolbarPanel.setBackground(new Color(50, 50, 50));
        
        toolbarPanel.add(newButton);
        toolbarPanel.add(openButton);
        toolbarPanel.add(saveButton);
        toolbarPanel.add(saveAsButton);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(formatButton);
        toolbarPanel.add(syntaxCheckButton);
        
        // File info panel
        JPanel fileInfoPanel = new JPanel(new BorderLayout());
        fileInfoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        fileInfoPanel.setBackground(new Color(60, 60, 60));
        
        JLabel fileLabel = new JLabel("File: ");
        fileLabel.setForeground(Color.WHITE);
        filePathLabel.setForeground(Color.LIGHT_GRAY);
        
        fileInfoPanel.add(fileLabel, BorderLayout.WEST);
        fileInfoPanel.add(filePathLabel, BorderLayout.CENTER);
        
        // Combine toolbar and file info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbarPanel, BorderLayout.CENTER);
        topPanel.add(fileInfoPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(editor, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Button event handlers
        newButton.addActionListener(e -> newScript());
        openButton.addActionListener(e -> openScript());
        saveButton.addActionListener(e -> saveScript());
        saveAsButton.addActionListener(e -> saveAsScript());
        formatButton.addActionListener(e -> formatCode());
        syntaxCheckButton.addActionListener(e -> checkSyntax());
        
        // Track document changes
        editor.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                markAsModified();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                markAsModified();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                markAsModified();
            }
        });
    }
    
    private void updateEditorCompletions() {
        if (connectionPanel != null && connectionPanel.isConnected()) {
            // Get MOCA cache from connection and update editor completions
            try {
                // TODO: This would integrate with the MOCA cache system
                // editor.updateCompletions(connectionPanel.getMocaCache());
            } catch (Exception e) {
                // Handle any connection issues
                System.err.println("Failed to update completions: " + e.getMessage());
            }
        }
    }
    
    private void markAsModified() {
        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateFileLabel();
        }
    }
    
    private void markAsSaved() {
        hasUnsavedChanges = false;
        updateFileLabel();
    }
    
    private void updateFileLabel() {
        String fileName = currentFilePath != null ? 
            Paths.get(currentFilePath).getFileName().toString() : 
            "Untitled.moca";
        
        if (hasUnsavedChanges) {
            fileName += " *";
        }
        
        filePathLabel.setText(fileName);
    }
    
    public void newScript() {
        if (hasUnsavedChanges && !confirmDiscardChanges()) {
            return;
        }
        
        editor.clear();
        currentFilePath = null;
        markAsSaved();
        updateFileLabel();
    }
    
    public void openScript() {
        if (hasUnsavedChanges && !confirmDiscardChanges()) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MOCA Scripts (*.moca, *.mcmd)", "moca", "mcmd"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                editor.setText(content);
                currentFilePath = file.getAbsolutePath();
                markAsSaved();
                updateFileLabel();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error opening file: " + e.getMessage(), 
                    "Open Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void saveScript() {
        if (currentFilePath == null) {
            saveAsScript();
            return;
        }
        
        try {
            Files.write(Paths.get(currentFilePath), editor.getText().getBytes());
            markAsSaved();
            JOptionPane.showMessageDialog(this, 
                "File saved successfully", 
                "Save", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving file: " + e.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void saveAsScript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MOCA Scripts (*.moca)", "moca"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Add .moca extension if not present
            if (!file.getName().toLowerCase().endsWith(".moca")) {
                file = new File(file.getAbsolutePath() + ".moca");
            }
            
            try {
                Files.write(file.toPath(), editor.getText().getBytes());
                currentFilePath = file.getAbsolutePath();
                markAsSaved();
                updateFileLabel();
                JOptionPane.showMessageDialog(this, 
                    "File saved successfully", 
                    "Save", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving file: " + e.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean confirmDiscardChanges() {
        int result = JOptionPane.showConfirmDialog(this,
            "You have unsaved changes. Do you want to discard them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        return result == JOptionPane.YES_OPTION;
    }
    
    // Text editing operations
    public void undo() {
        // RSyntaxTextArea supports undo/redo
        if (editor.getTextArea().canUndo()) {
            editor.getTextArea().undoLastAction();
        }
    }
    
    public void redo() {
        if (editor.getTextArea().canRedo()) {
            editor.getTextArea().redoLastAction();
        }
    }
    
    public void cut() {
        editor.getTextArea().cut();
    }
    
    public void copy() {
        editor.getTextArea().copy();
    }
    
    public void paste() {
        editor.getTextArea().paste();
    }
    
    public String getSelectedText() {
        return editor.getSelectedText();
    }
    
    public void showFindDialog() {
        // Create a simple find dialog
        String searchText = JOptionPane.showInputDialog(this, "Find:", "Find", JOptionPane.PLAIN_MESSAGE);
        if (searchText != null && !searchText.isEmpty()) {
            String text = editor.getText();
            int index = text.indexOf(searchText);
            if (index >= 0) {
                editor.getTextArea().select(index, index + searchText.length());
                editor.getTextArea().requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Text not found", "Find", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    public void showReplaceDialog() {
        // Create a simple replace dialog
        String searchText = JOptionPane.showInputDialog(this, "Find:", "Replace", JOptionPane.PLAIN_MESSAGE);
        if (searchText != null && !searchText.isEmpty()) {
            String replaceText = JOptionPane.showInputDialog(this, "Replace with:", "Replace", JOptionPane.PLAIN_MESSAGE);
            if (replaceText != null) {
                String text = editor.getText();
                String newText = text.replace(searchText, replaceText);
                editor.setText(newText);
            }
        }
    }
    
    public void formatCode() {
        // Simple code formatting - add proper indentation
        String text = editor.getText();
        String[] lines = text.split("\\n");
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Decrease indent for closing statements
            if (trimmed.startsWith("endif") || trimmed.startsWith("endwhile") || 
                trimmed.startsWith("endfor") || trimmed.startsWith("endtry") ||
                trimmed.startsWith("catch") || trimmed.startsWith("else") ||
                trimmed.startsWith("elsif")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }
            
            // Add indentation
            for (int i = 0; i < indentLevel; i++) {
                formatted.append("    ");
            }
            formatted.append(trimmed).append("\\n");
            
            // Increase indent for opening statements
            if (trimmed.startsWith("if ") || trimmed.startsWith("while ") || 
                trimmed.startsWith("for ") || trimmed.startsWith("try") ||
                trimmed.startsWith("else") || trimmed.startsWith("elsif") ||
                trimmed.startsWith("catch")) {
                indentLevel++;
            }
        }
        
        editor.setText(formatted.toString());
    }
    
    public void checkSyntax() {
        // Enable real-time linting
        editor.setRealTimeLinting(true);
        JOptionPane.showMessageDialog(this, 
            "Syntax checking enabled. Errors will be shown in the status bar.", 
            "Syntax Check", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public String getCurrentFilePath() {
        return currentFilePath;
    }
}
