package com.github.joshdevyn.mocaide.gui;

import com.github.joshdevyn.mocaide.gui.editor.MocaEditor;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaResults;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced MOCA command panel with syntax highlighting and IDE features
 */
public class MocaCommandPanel extends JPanel {
    
    private MocaConnectionPanel connectionPanel;
    private MocaEditor commandEditor;
    private JButton executeButton;
    private JButton clearButton;
    private JCheckBox traceEnabledCheckBox;
    private JTabbedPane resultsTabbedPane;
    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;
    private JTextArea traceTextArea;
    private JTextArea rawResultsTextArea;
    private JLabel executionStatusLabel;
    private JLabel executionTimeLabel;
    private JComboBox<String> commandHistoryComboBox;
    private List<String> commandHistory;
    
    public MocaCommandPanel(MocaConnectionPanel connectionPanel) {
        this.connectionPanel = connectionPanel;
        this.commandHistory = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Command input with enhanced editor
        commandEditor = new MocaEditor();
        commandEditor.setText("list warehouses");
        commandEditor.setPreferredSize(new Dimension(600, 200));
        
        // Buttons
        executeButton = new JButton("Execute Command");
        executeButton.setBackground(new Color(0, 120, 215));
        executeButton.setForeground(Color.WHITE);
        executeButton.setFocusPainted(false);
        
        clearButton = new JButton("Clear");
        
        // Options
        traceEnabledCheckBox = new JCheckBox("Enable Trace", false);
        
        // Command history
        commandHistoryComboBox = new JComboBox<>();
        commandHistoryComboBox.setPreferredSize(new Dimension(200, 25));
        
        // Status labels
        executionStatusLabel = new JLabel("Ready");
        executionTimeLabel = new JLabel("");
        
        // Results components
        setupResultsComponents();
    }
    
    private void setupResultsComponents() {
        resultsTabbedPane = new JTabbedPane();
        
        // Results table
        resultsTableModel = new DefaultTableModel();
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        resultsTabbedPane.addTab("Results", tableScrollPane);
        
        // Trace output
        traceTextArea = new JTextArea();
        traceTextArea.setEditable(false);
        traceTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        traceTextArea.setBackground(new Color(43, 43, 43));
        traceTextArea.setForeground(Color.WHITE);
        JScrollPane traceScrollPane = new JScrollPane(traceTextArea);
        resultsTabbedPane.addTab("Trace", traceScrollPane);
        
        // Raw results
        rawResultsTextArea = new JTextArea();
        rawResultsTextArea.setEditable(false);
        rawResultsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        rawResultsTextArea.setBackground(new Color(43, 43, 43));
        rawResultsTextArea.setForeground(Color.WHITE);
        JScrollPane rawScrollPane = new JScrollPane(rawResultsTextArea);
        resultsTabbedPane.addTab("Raw Output", rawScrollPane);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Command input panel
        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.setBorder(new TitledBorder("MOCA Command"));
        
        // Command history panel
        JPanel historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historyPanel.add(new JLabel("History:"));
        historyPanel.add(commandHistoryComboBox);
        
        JPanel commandInputPanel = new JPanel(new BorderLayout());
        commandInputPanel.add(historyPanel, BorderLayout.NORTH);
        commandInputPanel.add(commandEditor, BorderLayout.CENTER);
        
        // Options panel
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.add(traceEnabledCheckBox);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(executeButton);
        buttonsPanel.add(clearButton);
        
        JPanel commandControlPanel = new JPanel(new BorderLayout());
        commandControlPanel.add(optionsPanel, BorderLayout.WEST);
        commandControlPanel.add(buttonsPanel, BorderLayout.EAST);
        
        commandPanel.add(commandInputPanel, BorderLayout.CENTER);
        commandPanel.add(commandControlPanel, BorderLayout.SOUTH);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status:"));
        statusPanel.add(executionStatusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(executionTimeLabel);
        
        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(new TitledBorder("Results"));
        resultsPanel.add(resultsTabbedPane, BorderLayout.CENTER);
        resultsPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Main layout
        add(commandPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        executeButton.addActionListener(this::handleExecuteCommand);
        clearButton.addActionListener(this::handleClear);
        commandHistoryComboBox.addActionListener(this::handleHistorySelection);
        
        // Add keyboard shortcut for execute (Ctrl+Enter)
        InputMap inputMap = commandEditor.getTextArea().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = commandEditor.getTextArea().getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ctrl ENTER"), "execute");
        actionMap.put("execute", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExecuteCommand(e);
            }
        });
    }
    
    private void handleExecuteCommand(ActionEvent e) {
        String command = commandEditor.getText().trim();
        if (command.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a MOCA command", "No Command", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        MocaConnection connection = connectionPanel.getMocaConnection();
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Please establish a connection first", "No Connection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Add to history
        if (!commandHistory.contains(command)) {
            commandHistory.add(0, command);
            if (commandHistory.size() > 20) {
                commandHistory.remove(20);
            }
            updateHistoryComboBox();
        }
        
        // Execute command in background thread
        executeButton.setEnabled(false);
        executionStatusLabel.setText("Executing...");
        executionTimeLabel.setText("");
        
        SwingWorker<MocaResults, Void> worker = new SwingWorker<MocaResults, Void>() {
            private long startTime;
            
            @Override
            protected MocaResults doInBackground() throws Exception {
                startTime = System.currentTimeMillis();
                // Set trace flag based on checkbox state
                connection.setTraceRequested(traceEnabledCheckBox.isSelected());
                return connection.executeCommand(command);
            }
            
            @Override
            protected void done() {
                try {
                    long executionTime = System.currentTimeMillis() - startTime;
                    MocaResults results = get();
                    displayResults(results);
                    executionStatusLabel.setText("Completed");
                    executionTimeLabel.setText(String.format("(%d ms)", executionTime));
                } catch (Exception ex) {
                    executionStatusLabel.setText("Error");
                    JOptionPane.showMessageDialog(MocaCommandPanel.this, 
                        "Error executing command: " + ex.getMessage(), 
                        "Execution Error", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    executeButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayResults(MocaResults results) {
        // Display in results table
        if (results != null) {
            updateResultsTable(results.toStringTable());
        }
        
        // Display trace
        if (results != null && results.getTrace() != null) {
            traceTextArea.setText(results.getTrace());
        } else {
            traceTextArea.setText("No trace data available");
        }
        
        // Display raw results (convert results to string representation)
        if (results != null) {
            rawResultsTextArea.setText(results.toString());
        } else {
            rawResultsTextArea.setText("No raw output available");
        }
        
        // Switch to results tab
        resultsTabbedPane.setSelectedIndex(0);
    }
    
    private void updateResultsTable(java.util.ArrayList<String[]> data) {
        resultsTableModel.setRowCount(0);
        resultsTableModel.setColumnCount(0);
        
        if (data != null && !data.isEmpty()) {
            // Set column names from first row
            String[] headerRow = data.get(0);
            for (String columnName : headerRow) {
                resultsTableModel.addColumn(columnName);
            }
            
            // Add data rows (skip header row)
            for (int i = 1; i < data.size(); i++) {
                String[] row = data.get(i);
                resultsTableModel.addRow(row);
            }
        }
    }
    
    private void handleClear(ActionEvent e) {
        commandEditor.clear();
    }
    
    private void handleHistorySelection(ActionEvent e) {
        String selectedCommand = (String) commandHistoryComboBox.getSelectedItem();
        if (selectedCommand != null && !selectedCommand.isEmpty()) {
            commandEditor.setText(selectedCommand);
        }
    }
    
    private void updateHistoryComboBox() {
        commandHistoryComboBox.removeAllItems();
        for (String command : commandHistory) {
            commandHistoryComboBox.addItem(command);
        }
    }
    
    /**
     * Execute a command from external source (e.g., editor)
     */
    public void executeCommand(String command) {
        commandEditor.setText(command);
        handleExecuteCommand(null);
    }
    
    /**
     * Update connection reference when connection changes
     */
    public void updateConnection() {
        // This method can be called when switching tabs to ensure connection is current
    }
}
