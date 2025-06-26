package com.github.joshdevyn.mocaide.gui;

import com.github.mrglassdanny.mocalanguageserver.MocaLanguageServer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Panel for managing the Language Server Protocol functionality
 */
public class MocaLanguageServerPanel extends JPanel {
    
    private JButton startServerButton;
    private JButton stopServerButton;
    private JTextField portField;
    private JLabel serverStatusLabel;
    private JTextArea serverLogArea;
    private JCheckBox autoStartCheckBox;
    
    private Thread serverThread;
    private ServerSocket serverSocket;
    private boolean isServerRunning = false;
    private int serverPort = 4389; // Default LSP port
    
    public MocaLanguageServerPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Server controls
        startServerButton = new JButton("Start Language Server");
        stopServerButton = new JButton("Stop Language Server");
        stopServerButton.setEnabled(false);
        
        portField = new JTextField(String.valueOf(serverPort), 8);
        autoStartCheckBox = new JCheckBox("Auto-start on application launch", false);
        
        // Status
        serverStatusLabel = new JLabel("Stopped");
        serverStatusLabel.setForeground(Color.RED);
        
        // Log area
        serverLogArea = new JTextArea(15, 60);
        serverLogArea.setEditable(false);
        serverLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        serverLogArea.setBackground(new Color(248, 248, 248));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Server configuration panel
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("Language Server Configuration"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Port configuration
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        configPanel.add(portField, gbc);
        
        // Auto-start option
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        configPanel.add(autoStartCheckBox, gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(startServerButton);
        buttonsPanel.add(stopServerButton);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status:"));
        statusPanel.add(serverStatusLabel);
        
        // Control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(configPanel, BorderLayout.CENTER);
        controlPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("Server Log"));
        logPanel.add(new JScrollPane(serverLogArea), BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new TitledBorder("Information"));
        
        JTextArea infoArea = new JTextArea(4, 60);
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setText(
            "The Language Server provides MOCA language support for editors like VS Code.\n" +
            "Configure your editor to connect to: ws://localhost:" + serverPort + "\n" +
            "Supported features: syntax highlighting, code completion, error checking, go-to-definition."
        );
        infoPanel.add(infoArea, BorderLayout.CENTER);
        
        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(statusPanel, BorderLayout.CENTER);
        topPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        startServerButton.addActionListener(this::handleStartServer);
        stopServerButton.addActionListener(this::handleStopServer);
        
        // Update port when field changes
        portField.addActionListener(e -> {
            try {
                serverPort = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException ex) {
                portField.setText(String.valueOf(serverPort));
                JOptionPane.showMessageDialog(this, "Invalid port number. Using " + serverPort, 
                                            "Invalid Port", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
    
    private void handleStartServer(ActionEvent e) {
        try {
            serverPort = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid port number.", 
                                        "Invalid Port", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (isServerRunning) {
            logMessage("Server is already running!");
            return;
        }
        
        // Start server in background thread
        serverThread = new Thread(() -> {
            try {
                logMessage("Starting MOCA Language Server on port " + serverPort + "...");
                
                // Create server socket
                serverSocket = new ServerSocket(serverPort);
                isServerRunning = true;
                
                SwingUtilities.invokeLater(() -> {
                    updateServerStatus();
                    updateButtonStates();
                });
                
                logMessage("Language Server started successfully on port " + serverPort);
                logMessage("Waiting for client connections...");
                
                // Accept client connections
                while (isServerRunning && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        logMessage("Client connected: " + clientSocket.getRemoteSocketAddress());
                        
                        // Handle client in separate thread
                        Thread clientThread = new Thread(() -> handleClient(clientSocket));
                        clientThread.setDaemon(true);
                        clientThread.start();
                        
                    } catch (IOException ex) {
                        if (isServerRunning) {
                            logMessage("Error accepting client connection: " + ex.getMessage());
                        }
                    }
                }
                
            } catch (IOException ex) {
                logMessage("Failed to start server: " + ex.getMessage());
                isServerRunning = false;
                SwingUtilities.invokeLater(() -> {
                    updateServerStatus();
                    updateButtonStates();
                });
            }
        });
        
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    private void handleStopServer(ActionEvent e) {
        if (!isServerRunning) {
            logMessage("Server is not running!");
            return;
        }
        
        logMessage("Stopping Language Server...");
        isServerRunning = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            logMessage("Error stopping server: " + ex.getMessage());
        }
        
        if (serverThread != null) {
            serverThread.interrupt();
        }
        
        updateServerStatus();
        updateButtonStates();
        logMessage("Language Server stopped");
    }
    
    private void handleClient(Socket clientSocket) {
        try {
            logMessage("Handling client connection...");
            
            // Create input/output streams
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            
            // Start the MOCA Language Server with the client streams using LSP4J Launcher
            MocaLanguageServer languageServer = new MocaLanguageServer();
            org.eclipse.lsp4j.jsonrpc.Launcher<org.eclipse.lsp4j.services.LanguageClient> launcher = 
                org.eclipse.lsp4j.jsonrpc.Launcher.createLauncher(
                    languageServer, 
                    org.eclipse.lsp4j.services.LanguageClient.class, 
                    input, 
                    output
                );
            
            // Connect the language server to the client
            languageServer.connect(launcher.getRemoteProxy());
            
            // Start listening for client requests
            launcher.startListening();
            
            logMessage("Language Server session started for client");
            
        } catch (Exception ex) {
            logMessage("Error handling client: " + ex.getMessage());
        } finally {
            try {
                clientSocket.close();
                logMessage("Client disconnected");
            } catch (IOException ex) {
                logMessage("Error closing client connection: " + ex.getMessage());
            }
        }
    }
    
    private void updateServerStatus() {
        if (isServerRunning) {
            serverStatusLabel.setText("Running on port " + serverPort);
            serverStatusLabel.setForeground(Color.GREEN);
        } else {
            serverStatusLabel.setText("Stopped");
            serverStatusLabel.setForeground(Color.RED);
        }
    }
    
    private void updateButtonStates() {
        startServerButton.setEnabled(!isServerRunning);
        stopServerButton.setEnabled(isServerRunning);
        portField.setEnabled(!isServerRunning);
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            serverLogArea.append(java.time.LocalTime.now().toString() + " - " + message + "\n");
            serverLogArea.setCaretPosition(serverLogArea.getDocument().getLength());
        });
    }
    
    // Auto-start functionality
    public void autoStartIfEnabled() {
        if (autoStartCheckBox.isSelected()) {
            handleStartServer(null);
        }
    }
    
    public boolean isServerRunning() {
        return isServerRunning;
    }
    
    public int getServerPort() {
        return serverPort;
    }
}
