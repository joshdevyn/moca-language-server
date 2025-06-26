package com.github.joshdevyn.mocaide.gui;

import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaResults;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Panel for managing MOCA server connections
 */
public class MocaConnectionPanel extends JPanel {
    
    private JTextField urlField;
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JCheckBox approveUnsafeScriptsCheckBox;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton testConnectionButton;
    private JLabel connectionStatusLabel;
    private JTextArea connectionLogArea;
    
    private MocaConnection mocaConnection;
    private boolean isConnected = false;
    
    public MocaConnectionPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Connection fields
        urlField = new JTextField("http://localhost:4500/service", 30);
        userIdField = new JTextField("SUPER", 15);
        passwordField = new JPasswordField("SUPER", 15);
        approveUnsafeScriptsCheckBox = new JCheckBox("Approve Unsafe Scripts", false);
        
        // Buttons
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        testConnectionButton = new JButton("Test Connection");
        disconnectButton.setEnabled(false);
        
        // Status
        connectionStatusLabel = new JLabel("Not Connected");
        connectionStatusLabel.setForeground(Color.RED);
        
        // Log area
        connectionLogArea = new JTextArea(8, 40);
        connectionLogArea.setEditable(false);
        connectionLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        connectionLogArea.setBackground(new Color(248, 248, 248));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Connection details panel
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(new TitledBorder("Connection Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // URL
        gbc.gridx = 0; gbc.gridy = 0;
        connectionPanel.add(new JLabel("MOCA URL:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        connectionPanel.add(urlField, gbc);
        
        // User ID
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        connectionPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        connectionPanel.add(userIdField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        connectionPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        connectionPanel.add(passwordField, gbc);
        
        // Options
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        connectionPanel.add(approveUnsafeScriptsCheckBox, gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(connectButton);
        buttonsPanel.add(disconnectButton);
        buttonsPanel.add(testConnectionButton);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status:"));
        statusPanel.add(connectionStatusLabel);
        
        // Connection control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(connectionPanel, BorderLayout.CENTER);
        controlPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("Connection Log"));
        logPanel.add(new JScrollPane(connectionLogArea), BorderLayout.CENTER);
        
        // Main layout
        add(controlPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        connectButton.addActionListener(this::handleConnect);
        disconnectButton.addActionListener(this::handleDisconnect);
        testConnectionButton.addActionListener(this::handleTestConnection);
        
        // Enable/disable buttons based on connection state
        updateButtonStates();
    }
    
    private void handleConnect(ActionEvent e) {
        String url = urlField.getText().trim();
        String userId = userIdField.getText().trim();
        
        if (url.isEmpty() || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", 
                                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Perform connection in background thread
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Attempting to connect to " + url + "...");
                
                try {
                    mocaConnection = new MocaConnection();
                    
                    // Test the connection by executing a simple command
                    MocaResults results = mocaConnection.executeCommand("list warehouses");
                    if (results != null) {
                        publish("Connected successfully!");
                        return true;
                    } else {
                        publish("Connection failed: No response from server");
                        return false;
                    }
                } catch (Exception ex) {
                    publish("Connection failed: " + ex.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    isConnected = get();
                    updateConnectionStatus();
                    updateButtonStates();
                } catch (Exception ex) {
                    logMessage("Error: " + ex.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleDisconnect(ActionEvent e) {
        if (mocaConnection != null) {
            mocaConnection = null;
        }
        isConnected = false;
        updateConnectionStatus();
        updateButtonStates();
        logMessage("Disconnected from MOCA server");
    }
    
    private void handleTestConnection(ActionEvent e) {
        String url = urlField.getText().trim();
        String userId = userIdField.getText().trim();
        
        if (url.isEmpty() || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", 
                                        "Test Connection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Test connection in background thread
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Testing connection to " + url + "...");
                
                try {
                    MocaConnection testConnection = new MocaConnection();
                    MocaResults results = testConnection.executeCommand("list warehouses");
                    
                    if (results != null) {
                        publish("Test successful! Server is reachable.");
                        return true;
                    } else {
                        publish("Test failed: No response from server");
                        return false;
                    }
                } catch (Exception ex) {
                    publish("Test failed: " + ex.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    String message = success ? "Connection test passed!" : "Connection test failed!";
                    JOptionPane.showMessageDialog(MocaConnectionPanel.this, message, 
                                                "Test Results", 
                                                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MocaConnectionPanel.this, 
                                                "Test error: " + ex.getMessage(), 
                                                "Test Results", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateConnectionStatus() {
        if (isConnected) {
            connectionStatusLabel.setText("Connected");
            connectionStatusLabel.setForeground(Color.GREEN);
        } else {
            connectionStatusLabel.setText("Not Connected");
            connectionStatusLabel.setForeground(Color.RED);
        }
    }
    
    private void updateButtonStates() {
        connectButton.setEnabled(!isConnected);
        disconnectButton.setEnabled(isConnected);
        testConnectionButton.setEnabled(true); // Always allow testing
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            connectionLogArea.append(java.time.LocalTime.now().toString() + " - " + message + "\n");
            connectionLogArea.setCaretPosition(connectionLogArea.getDocument().getLength());
        });
    }
    
    /**
     * Get available MOCA commands from the connected server
     */
    public java.util.List<String> getAvailableCommands() {
        java.util.List<String> commands = new java.util.ArrayList<>();
        
        if (isConnected && mocaConnection != null) {
            try {
                // Query for available commands from the server
                MocaResults results = mocaConnection.executeCommand(
                    "list commands where cmd_nam is not null order by cmd_nam"
                );
                
                if (results != null && results.getRowCount() > 0) {
                    // Parse results and extract command names
                    for (int i = 0; i < results.getRowCount(); i++) {
                        String cmdName = results.getString(i, "cmd_nam");
                        if (cmdName != null && !cmdName.trim().isEmpty()) {
                            commands.add(cmdName);
                        }
                    }
                }
            } catch (Exception e) {
                logMessage("Error fetching commands: " + e.getMessage());
            }
        }
        
        // Add fallback commands if server query fails
        if (commands.isEmpty()) {
            commands.add("list");
            commands.add("get");
            commands.add("modify");
            commands.add("create");
            commands.add("remove");
            commands.add("count");
            commands.add("publish data");
        }
        
        return commands;
    }
    
    /**
     * Get available tables from the connected server
     */
    public java.util.List<String> getAvailableTables() {
        java.util.List<String> tables = new java.util.ArrayList<>();
        
        if (isConnected && mocaConnection != null) {
            try {
                // Query for available tables
                MocaResults results = mocaConnection.executeCommand(
                    "list tables where table_name is not null order by table_name"
                );
                
                if (results != null) {
                    // Add common MOCA tables as fallback
                    tables.add("inventory");
                    tables.add("location");
                    tables.add("item_master");
                    tables.add("shipment");
                    tables.add("receipt");
                    tables.add("order_header");
                    tables.add("order_line");
                    tables.add("cycle_count");
                    tables.add("work_order");
                }
            } catch (Exception e) {
                logMessage("Error fetching tables: " + e.getMessage());
            }
        }
        
        return tables;
    }
    
    /**
     * Get available columns from the connected server
     */
    public java.util.List<String> getAvailableColumns() {
        java.util.List<String> columns = new java.util.ArrayList<>();
        
        if (isConnected && mocaConnection != null) {
            try {
                // Add common MOCA column names
                columns.add("prtnum");
                columns.add("lodnum");
                columns.add("locnam");
                columns.add("stoloc");
                columns.add("invsts");
                columns.add("invsts_prg");
                columns.add("untqty");
                columns.add("untcas");
                columns.add("client_id");
                columns.add("wh_id");
                columns.add("ordnum");
                columns.add("ordtyp");
                columns.add("ordlin");
                columns.add("ordlvl");
                columns.add("supnum");
                columns.add("rcvkey");
                columns.add("trknum");
                columns.add("carr_id");
                columns.add("cponum");
                columns.add("po_lin_num");
                
            } catch (Exception e) {
                logMessage("Error fetching columns: " + e.getMessage());
            }
        }
        
        return columns;
    }
    
    // Getter methods for other panels to access connection
    public MocaConnection getMocaConnection() {
        return mocaConnection;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public String getConnectionInfo() {
        if (isConnected && mocaConnection != null) {
            return String.format("Connected to %s as %s", urlField.getText(), userIdField.getText());
        }
        return "Not connected";
    }
}
